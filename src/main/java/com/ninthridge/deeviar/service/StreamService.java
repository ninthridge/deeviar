package com.ninthridge.deeviar.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutorFactory;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.model.Device;
import com.ninthridge.deeviar.model.Device.DeviceStatus;
import com.ninthridge.deeviar.model.DeviceStation;
import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.ProfileStation;
import com.ninthridge.deeviar.model.Stream;
import com.ninthridge.deeviar.model.Tuner;
import com.ninthridge.deeviar.repository.StreamRepository;
import com.ninthridge.deeviar.transcoder.Transcoder;
import com.ninthridge.deeviar.util.FileUtil;

@Service("streamService")
public class StreamService {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private StreamRepository streamRepository;

  @Autowired
  private LineupService lineupService;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private Transcoder transcoder;

  @Autowired
  private OperatingSystemCmdExecutorFactory operatingSystemCmdExecutorFactory;

  private final Lock lock = new ReentrantLock();

  public List<Stream> findActive() {
    List<Stream> activeStreams = new ArrayList<>();
    Set<Stream> streams = streamRepository.getAll();
    if(streams != null) {
      for (Stream stream : streams) {
        if (stream.getStopped() == null) {
          activeStreams.add(stream);
        }
      }
    }
    return activeStreams;
  }

  public List<Stream> findInactive() {
    List<Stream> inactiveStreams = new ArrayList<>();
    Set<Stream> streams = streamRepository.getAll();
    if(streams != null) {
      for (Stream stream : streams) {
        if (stream.getStopped() != null) {
          inactiveStreams.add(stream);
        }
      }
    }
    return inactiveStreams;
  }

  private Stream findActiveByStationId(String stationId) {
    for (Stream stream : findActive()) {
      if (stream.getStationId().equals(stationId)) {
        return stream;
      }
    }
    return null;
  }

  /*
   * Called by the client to start watching or recording a channel
   */
  public Stream stream(String profileTitle, String stationId, String client, Date expires) {
    log.info("Stream " + stationId + " for client " + client);

    Stream stream = null;
    try {
      lock.lock();
      // clear out any previous expirations on other streams for this client
      Set<Stream> streams = streamRepository.getAll();
      if(streams != null) {
        for (Stream s : streamRepository.getAll()) {
          if (!s.getStationId().equals(stationId) && s.getExpires().containsKey(client)) {
            untune(client, s);
          }
        }
      }
      
      stream = findActiveByStationId(stationId);
      if (stream == null) {
        // This is so that we don't tune to a channel that isn't available in the profile
        ProfileStation profileStation = lineupService.findProfileStationById(profileTitle, stationId);
        if (profileStation == null) {
          // TODO: better exception
          log.error("Bad channel " + stationId);
          throw new RuntimeException("Bad channel " + stationId);
        }
        log.info(profileStation);

        Set<GrabberStation> grabberStations = lineupService.findGrabberStationsByStationId(stationId);
        if (grabberStations == null || grabberStations.isEmpty()) {
          // TODO: better exception
          log.error("Bad channel " + stationId);
          throw new RuntimeException("Bad channel " + stationId);
        }
        log.info(grabberStations);

        // find a tuner
        final Tuner tuner = findTuner(grabberStations);
        if (tuner == null) {
          // TODO: better exception
          log.info("Unable to acquire a tuner");
          throw new RuntimeException("Unable to acquire a tuner");
        }
        log.info(tuner);

        final DeviceStation deviceStation = getDeviceStation(grabberStations, tuner.getDevice());
        if (deviceStation == null) {
          log.error("Error looking up the device station");
          throw new RuntimeException("Error looking up the device station");
        }
        log.info(deviceStation);

        // TODO: ffmpeg can do multiple output streams with different settings from a single input
        // TODO: no need to provide a stream where we are setting the bitrate higher than current
        // start transcoder
        log.info("Starting transcoder for " + stationId);
        stream = transcoder.start(tuner, stationId);
        if (stream == null) {
          // TODO: better exception
          log.error("Unable to start transcoder");
          throw new RuntimeException("Unable to start transcoder");
        }
        log.info(stream);
        streamRepository.save(stream);

        deviceService.tune(tuner, deviceStation);
      }
    } finally {
      lock.unlock();
    }

    // update the client's expiration
    if (expires == null) {
      Calendar cal = new GregorianCalendar();
      cal.add(Calendar.MINUTE, 5);
      expires = cal.getTime();
    }
    stream.getExpires().put(client, expires);

    return stream;
  }

  protected Tuner findTuner(Set<GrabberStation> grabberStations) {
    Tuner tuner = findUnusedTuner(grabberStations);
    if (tuner == null) {
      Stream killableStream = findKillableStream(grabberStations);
      if (killableStream != null) {
        tuner = killableStream.getTuner();
        log.info("Stopping stream to free up its tuner: " + killableStream);
        untuneAllClients(killableStream);
      }
    }
    return tuner;
  }

  protected Tuner findUnusedTuner(Set<GrabberStation> grabberStations) {
    List<Tuner> tuners = deviceService.findAllAvailableTuners();
    for (Stream stream : findActive()) {
      tuners.remove(stream.getTuner());
    }
    if (!tuners.isEmpty()) {
      Tuner tuner = null;
      for (Tuner t : tuners) {
        DeviceStation deviceStation = getDeviceStation(grabberStations, t.getDevice());
        if (deviceStation != null) {
          if (t.getLastTuned() == null) {
            return t;
          }
          if (tuner == null || t.getLastTuned().before(t.getLastTuned())) {
            tuner = t;
          }
        }
      }
      return tuner;
    }

    return null;
  }

  protected Stream findKillableStream(Set<GrabberStation> grabberStations) {
    // TODO: should be priority based. intrapriority conflicts should be resolve as follows:
    // timer-based should only be killed within the last 2 minutes
    // non-timer based start with anything by the same client and then fall back to sooner expires
    // TODO: add logic for timer vs non-timer conflict resolutions
    Set<Stream> streams = streamRepository.getAll();
    if(streams != null) {
      for (Iterator<Stream> it = new ArrayList<>(streams).iterator(); it.hasNext();) {
        Stream s = it.next();
        if (s.getTuner() != null && s.getTuner().getDevice().getStatus().equals(DeviceStatus.Available)) {
          DeviceStation deviceStation = getDeviceStation(grabberStations, s.getTuner().getDevice());
          if (deviceStation == null) {
            it.remove();
          } else {
            for (String c : s.getExpires().keySet()) {
              // TODO: add a priority instead of using a magic string
              if (c.startsWith("TIMER-")) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(s.getExpires().get(c));
                cal.add(Calendar.MINUTE, -2);
                if (new Date().before(cal.getTime())) {
                  it.remove();
                }
              }
            }
          }
        }
      }
    }

    // use the remaining stream that started first
    Stream stream = null;
    if(streams != null) {
      for (Stream s : streams) {
        if (stream == null || s.getStarted().before(stream.getStarted())) {
          stream = s;
        }
      }
    }
    return stream;
  }

  protected DeviceStation getDeviceStation(Set<GrabberStation> grabberStations, Device device) {
    for (GrabberStation grabberStation : grabberStations) {
      if (grabberStation.getLineupId().equals(device.getLineupId())
          || grabberStation.getLineupId().equals(device.getId())) {
        for (DeviceStation ds : device.getStations()) {
          if (ds.getChannel().equals(grabberStation.getChannel())) {
            return ds;
          }
        }
      }
    }
    return null;
  }

  /*
   * Called by a client shortly after calling startStream. This is useful since
   * the startup transcoding time is slow. This allows us to pre-start a stream
   * by calling startStream() which returns immediately, and then shortly after
   * calling getStream which will wait until the stream has started
   */
  public Stream getStream(String profileTitle, String stationId) {
    log.info("getStationStream " + stationId);

    Stream stream = findActiveByStationId(stationId);
    if (stream != null) {
      if (waitForStreamToStart(new File(getDir(stream), stream.getId() + "1.ts"), 20)) {
        return stream;
      } else {
        try {
          lock.lock();
          untuneAllClients(stream);
          log.error("Stream failed to start");
        } finally {
          lock.unlock();
        }
      }
    } else {
      // throw exception here?
    }
    return null;
  }

  private Boolean waitForStreamToStart(File file, int seconds) {
    for (int j = 0; j < seconds * 2; j++) {
      if (file.exists()) {
        log.info("Stream started successfully in " + j * 500 + " milliseconds");
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return true;
      }

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    log.error("Stream failed to start " + file.getName());
    return false;
  }

  /*
   * Called by the client when leaving the current channel
   */
  public void untune(String client) {
    Set<Stream> streams = streamRepository.getAll();
    if(streams != null) {
      for (Stream stream : streams) {
        if (stream.getExpires().containsKey(client)) {
          untune(client, stream);
        }
      }
    }
  }

  public void untune(String client, Stream stream) {
    log.info("Untuning " + client + " " + stream);
    stream.getExpires().remove(client);

    if (stream.getExpires().isEmpty()) {
      log.info("All clients have been untuned from " + stream + " .  Stopping the stream");
      stop(stream);
    }
  }

  public void untuneAllClients(Stream stream) {
    for (String client : stream.getExpires().keySet()) {
      untune(client, stream);
    }
  }

  public File getDir(Stream stream) {
    return new File(config.getStreamsDir(), stream.getId());
  }

  public File getFile(Stream stream) {
    return new File(getDir(stream), stream.getId() + ".m3u8");
  }

  /*
   * responsible for freeing up the tuner
   */
  protected void stop(Stream stream) {
    deviceService.unTune(stream.getTuner());
    transcoder.stop(stream);
    stream.setTuner(null);
    stream.setStopped(new Date());
    stream.getExpires().clear();
  }

  public void delete(Stream stream) {
    streamRepository.delete(stream.getId());
    FileUtil.deleteRecursively(getDir(stream));
  }

  public boolean verify(Stream stream) {
    return verify(stream, 1);
  }

  protected boolean verify(Stream stream, int tries) {
    for (int i = 0; i < tries; i++) {
      try {
        if (!operatingSystemCmdExecutorFactory.getExecutor().getProcessIds(stream.getId()).isEmpty()) {
          log.debug("Verified that stream " + stream.getId() + " is active");
          return true;
        } else {
          log.info("Stream " + stream.getId() + " failed verification. " + (tries - (i + 1)) + " attempts remaining");
          if ((tries - (i + 1)) > 0) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.error(e, e);
            }
          }
        }
      } catch (IOException e) {
        log.error(e, e);
        return true;
      }
    }
    return false;
  }
}
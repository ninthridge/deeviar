package com.ninthridge.deeviar.manager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.model.Airing;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.ProfileStation;
import com.ninthridge.deeviar.model.Recording;
import com.ninthridge.deeviar.model.Stream;
import com.ninthridge.deeviar.model.TimerOccurrence;
import com.ninthridge.deeviar.model.Airing.AiringType;
import com.ninthridge.deeviar.model.Recording.RecordingStatus;
import com.ninthridge.deeviar.service.LineupService;
import com.ninthridge.deeviar.service.ProfileService;
import com.ninthridge.deeviar.service.StreamService;
import com.ninthridge.deeviar.service.TimerService;
import com.ninthridge.deeviar.util.TitleUtil;

@Component("streamManager")
public class StreamManager implements Runnable {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private TimerService timerService;

  @Autowired
  private StreamService streamService;

  @Autowired
  private LineupService lineupService;

  @Autowired
  private MediaProcessingManager mediaProcessingManager;

  @Autowired
  private ProfileService profileService;

  @Override
  public void run() {
    Set<Recording> recordings = new HashSet<>();
    while (true) {

      try {
        verifyActiveStreams();
      } catch (Exception e) {
        log.error(e, e);
      }

      try {
        startNewRecordings(recordings);
      } catch (Exception e) {
        log.error(e, e);
      }

      try {
        untuneExpiredClients();
      } catch (Exception e) {
        log.error(e, e);
      }

      try {
        manageRecordings(recordings);
      } catch (Exception e) {
        log.error(e, e);
      }

      try {
        deleteUnusedStreams(recordings);
      } catch (Exception e) {
        log.error(e, e);
      }

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
  }

  protected Map<String, Set<TimerOccurrence>> getActiveTimers() {
    Map<String, Set<TimerOccurrence>> activeTimers = new HashMap<>();
    Date date = new Date();
    for (Profile profile : profileService.findAll()) {
      Collection<TimerOccurrence> timerOccurrences = timerService.getTimerOccurrences(profile.getTitle(), null, null);
      Set<TimerOccurrence> activeProfileTimers = new HashSet<>();
      for (TimerOccurrence timerOccurrence : timerOccurrences) {
        Date start = getPaddedStart(timerOccurrence);
        Date end = getPaddedEnd(timerOccurrence);

        if (!date.before(start) && !date.after(end)) {
          activeProfileTimers.add(timerOccurrence);
        }
      }

      if (!activeProfileTimers.isEmpty()) {
        activeTimers.put(profile.getTitle(), activeProfileTimers);
      }
    }

    return activeTimers;
  }

  protected void startNewRecordings(Set<Recording> recordings) {
    Map<String, Set<TimerOccurrence>> activeTimers = getActiveTimers();
    for (String profileTitle : activeTimers.keySet()) {
      for (TimerOccurrence timerOccurrence : activeTimers.get(profileTitle)) {
        Recording recording = new Recording();
        recording.setStationId(timerOccurrence.getStationId());
        recording.setProfileTitle(profileTitle);
        recording.setStartDate(getPaddedStart(timerOccurrence));
        recording.setEndDate(getPaddedEnd(timerOccurrence));
        recording.setDeleteSource(false);

        if (!recordings.contains(recording)) {
          // start recording
          String client = "TIMER-" + timerOccurrence.getId();
          try {
            Stream stream = streamService.stream(profileTitle, timerOccurrence.getStationId(), client,
                recording.getEndDate());
            if (stream != null) {
              // TODO: verify that the recording started before adding to activeRecordings
              recording.setStream(stream);
              recording.setRecordingStatus(RecordingStatus.RECORDING);

              log.info("Started recording: " + recording);
              recordings.add(recording);
            }
          } catch (Exception e) {
            log.error(e, e);
          }
        }
      }
    }
  }

  protected void untuneExpiredClients() {
    for (Stream stream : streamService.findActive()) {
      for (String client : new HashSet<String>(stream.getExpires().keySet())) {
        if (new Date().after(stream.getExpires().get(client))) {
          streamService.untune(client, stream);
        }
      }
    }
  }

  protected void manageRecordings(Set<Recording> recordings) {
    for (Iterator<Recording> it = recordings.iterator(); it.hasNext();) {
      Recording recording = it.next();
      if (recording.getRecordingStatus().equals(RecordingStatus.RECORDING)) {
        Stream stream = recording.getStream();
        Date date = new Date();
        if (date.after(recording.getEndDate()) || stream.getStopped() != null) {

          Date streamEndDate = null;
          if (stream.getStopped() != null) {
            streamEndDate = stream.getStopped();
          } else {
            streamEndDate = date;
          }

          Date recordingEndDate = null;
          if (streamEndDate.before(recording.getEndDate())) {
            recordingEndDate = streamEndDate;
          } else {
            recordingEndDate = recording.getEndDate();
          }

          Date adjustedStreamStartDate = null;
          // adjust start to match what has been wrapped
          Calendar cal = new GregorianCalendar();
          cal.setTime(streamEndDate);
          // TODO: move the 4 hour value to config. should match the hls_wrap value
          cal.add(Calendar.HOUR, -4);
          if (stream.getStarted().before(cal.getTime())) {
            adjustedStreamStartDate = cal.getTime();
          } else {
            adjustedStreamStartDate = stream.getStarted();
          }

          Date recordingStartDate = null;
          if (adjustedStreamStartDate.after(recording.getStartDate())) {
            recordingStartDate = adjustedStreamStartDate;
          } else {
            recordingStartDate = recording.getStartDate();
          }

          long startOffsetSeconds = Math
              .round((recordingStartDate.getTime() - adjustedStreamStartDate.getTime()) / 1000);
          long durationSeconds = Math.round((recordingEndDate.getTime() - recordingStartDate.getTime()) / 1000);

          recording.setStart(startOffsetSeconds);
          recording.setDuration(durationSeconds);

          // padded to ensure that we don't query the previous program
          cal.setTime(recording.getStartDate());
          cal.add(Calendar.SECOND, 150);

          Airing airing = lineupService.findAiring(recording.getProfileTitle(), recording.getStationId(),
              cal.getTime());
          if (airing != null) {
            recording.setTitle(TitleUtil.cleanse(airing.getTitle()));
            recording.setDescription(airing.getDescription());

            recording.setEpisodeTitle(airing.getEpisodeTitle());
            recording.setSeason(airing.getSeason());
            recording.setEpisode(airing.getEpisode());
            recording.setCategory(getCategory(airing.getAiringType()));
            recording.setReleaseDate(airing.getOriginalAirDate());
          } else {
            // default
            ProfileStation station = lineupService.findProfileStationById(recording.getProfileTitle(),
                recording.getStationId());
            recording.setTitle(station.getCallSign() + " "
                + new SimpleDateFormat("MM-dd-yyyy HH:mm").format(recording.getStartDate()));
            recording.setCategory("Recordings");
            recording.setReleaseDate(recording.getStartDate());
          }

          try {
            recording
                .setCanonicalPath(new File(config.getStreamsDir().getParentFile(), stream.getStreams().get(0).getUri())
                    .getCanonicalPath());
          } catch (IOException e) {
            log.error(e, e);
          }
          recording.setTimestamp(new Date());
          recording.setCompress(false);
          recording.setPriority(1);
          mediaProcessingManager.addToProcessingQueue(recording);
          recording.setRecordingStatus(RecordingStatus.PROCESSING);
          log.info("Added to the processing queue: " + recording);
        }
      } else if (recording.getRecordingStatus().equals(RecordingStatus.PROCESSING)) {
        if (!mediaProcessingManager.isInProcessingQueue(recording)) {
          recording.setRecordingStatus(RecordingStatus.COMPLETE);
          it.remove();
          log.info("Processing complete: " + recording);
        }
      }
    }
  }

  protected void deleteUnusedStreams(Set<Recording> recordings) {
    Set<Stream> activeRecordings = new HashSet<>();
    for (Recording recording : recordings) {
      activeRecordings.add(recording.getStream());
    }

    // delete streams that are no longer needed
    for (Stream stream : streamService.findInactive()) {
      if (!activeRecordings.contains(stream)) {
        streamService.delete(stream);
      }
    }
  }

  protected void verifyActiveStreams() {
    for (Stream stream : streamService.findActive()) {
      if (!streamService.verify(stream)) {
        log.error("Stream failed unexpectedly: " + stream);
        // TODO: restart and append
        streamService.untuneAllClients(stream);
      }
    }
  }

  // TODO: add the ability for the user to set the category
  protected String getCategory(AiringType airingType) {
    if (AiringType.Movie.equals(airingType)) {
      return "Movies";
    } else if (AiringType.Episode.equals(airingType)) {
      return "Series";
    } else if (AiringType.Sports.equals(airingType)) {
      return "Sports";
    } else {
      return "Videos";
    }
  }

  protected Date getPaddedStart(TimerOccurrence timerOccurrence) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(timerOccurrence.getStartDate());
    cal.add(Calendar.MINUTE, -1);
    return cal.getTime();
  }

  protected Date getPaddedEnd(TimerOccurrence timerOccurrence) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(timerOccurrence.getEndDate());
    cal.add(Calendar.MINUTE, 2);
    return cal.getTime();
  }
}

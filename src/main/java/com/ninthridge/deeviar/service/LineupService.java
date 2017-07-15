package com.ninthridge.deeviar.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.grabber.GrabberException;
import com.ninthridge.deeviar.grabber.LineupGrabber;
import com.ninthridge.deeviar.model.Airing;
import com.ninthridge.deeviar.model.Channel;
import com.ninthridge.deeviar.model.Device;
import com.ninthridge.deeviar.model.DeviceStation;
import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.Lineup;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.ProfileStation;
import com.ninthridge.deeviar.repository.ChannelRepository;
import com.ninthridge.deeviar.repository.LineupRepository;
import com.ninthridge.deeviar.repository.ProfileLineupRepository;
import com.ninthridge.deeviar.util.ImageUtil;
import com.ninthridge.deeviar.util.ImageUtil.VALIGN;

@Service("lineupService")
public class LineupService {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private LineupGrabber lineupGrabber;

  @Autowired
  private LineupRepository lineupRepository;

  @Autowired
  private ProfileLineupRepository profileLineupRepository;
  
  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private ProfileService profileService;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private TimerService timerService;

  public boolean isConfigured() {
    boolean configured = lineupGrabber.isConfigured();
    if (!configured) {
      log.warn("Please configure a schedules provider");
    }
    return configured;
  }

  public void refreshLineup() {
    log.info("Starting lineup refresh");
    Lineup<GrabberStation> oldLineup = lineupRepository.get();
    Lineup<GrabberStation> lineup = null;
    try {
      Set<String> lineupIds = getAllLineupIds();
      if (lineupIds != null && !lineupIds.isEmpty()) {
        lineup = lineupGrabber.grabLineup(lineupIds);
        if (lineup == null || !lineup.containsFutureAirings()) {
          if (oldLineup == null || !oldLineup.containsFutureAirings()) {
            lineup = createLineupFromDeviceStations();
            if (lineup == null || lineup.getStations().isEmpty()) {
              log.warn("Failed to retrieve new airings and failed to create a lineup from scanned device stations.");
              return;
            } else {
              log.warn("Failed to retrive new airings.  Created a lineup from scanned device stations.");
            }
          } else {
            log.warn("Failed to retrieve new airings.");
            return;
          }
        }

        createImages(lineup);
        lineupRepository.save(lineup);
        log.info("Completed lineup refresh.  Found " + lineup.getStations().size() + " stations and "
            + lineup.getAirings().size() + " airings");
      }
    } catch (GrabberException e) {
      log.error("Unable to grab lineup", e);
    }
  }

  public void refreshProfileLineups() {
    Lineup<GrabberStation> lineup = lineupRepository.get();
    if (lineup != null) {
      for (Profile profile : profileService.findAll()) {
        Lineup<ProfileStation> profileLineup = profileLineup(profile, lineup);
        profileLineupRepository.save(profile.getTitle(), profileLineup);
        log.info("Loaded lineup for " + profile.getTitle() + " with " + profileLineup.getStations().size()
            + " stations and " + profileLineup.getAirings().size() + " airings");
      }
    }

    timerService.refreshTimerOccurrences();
  }

  protected Lineup<GrabberStation> createLineupFromDeviceStations() {
    Lineup<GrabberStation> lineup = new Lineup<>();
    lineup.setTimestamp(new Date());
    Set<String> lineupIds = new HashSet<>();
    for (Device device : deviceService.findAllAvailableDevices()) {
      lineupIds.add(device.getId());
      for (DeviceStation deviceStation : device.getStations()) {
        GrabberStation grabberStation = new GrabberStation();
        grabberStation.setId(deviceStation.getId());
        grabberStation.setChannel(deviceStation.getChannel());
        grabberStation.setCallSign(deviceStation.getCallSign());
        grabberStation.setLineupId(device.getId());
        lineup.addStation(grabberStation);
      }
    }
    lineup.setLineupIds(lineupIds);
    return lineup;
  }

  protected Lineup<ProfileStation> profileLineup(Profile profile, Lineup<GrabberStation> lineup) {
    Lineup<ProfileStation> profileLineup = new Lineup<ProfileStation>();

    List<GrabberStation> stations = lineup.getStations();
    List<Channel> channels = channelRepository.get();
    for (GrabberStation grabberStation : stations) {
      Set<String> deviceIds = getDeviceIds(grabberStation);
      if (deviceIds != null && !deviceIds.isEmpty()) {
        ProfileStation profileStation = new ProfileStation();
        profileStation.setId(grabberStation.getId());
        profileStation.setCallSign(grabberStation.getCallSign());
        profileStation.setHdPosterUri(grabberStation.getHdPosterUri());
        profileStation.setSdPosterUri(grabberStation.getSdPosterUri());

        if (channels != null && !channels.isEmpty()) {
          for (Channel channel : channels) {
            if (channel.getLineupId().equals(grabberStation.getLineupId())
                && channel.getStationId().equals(grabberStation.getId())
                && (channel.getProfileTitles() != null && channel.getProfileTitles().contains(profile.getTitle()))) {

              profileStation.setChannel(channel.getChannel());
              profileStation.setTitle(channel.getChannel() + " - " + grabberStation.getCallSign());
              profileLineup.addStation(profileStation);
            }
          }
        } else {
          profileStation.setChannel(grabberStation.getChannel());
          profileStation.setTitle(grabberStation.getChannel() + " - " + grabberStation.getCallSign());
          profileLineup.addStation(profileStation);
        }
      }
    }

    for (Airing airing : lineup.getAirings()) {
      profileLineup.addAiring(airing);
    }

    return profileLineup;
  }

  public List<Channel> getChannels() {
    List<GrabberStation> grabberStations = findGrabberStations();
    List<Channel> persistedChannels = channelRepository.get();
    List<Channel> channels = new ArrayList<>();

    for (GrabberStation grabberStation : grabberStations) {
      Channel channel = getChannel(persistedChannels, grabberStation.getLineupId(), grabberStation.getId());
      if (channel == null) {
        channel = new Channel();
        channel.setChannel(grabberStation.getChannel());
        channel.setStationId(grabberStation.getId());
        channel.setLineupId(grabberStation.getLineupId());
        channel.setProfileTitles(new HashSet<String>());
      }
      channel.setLineupChannel(grabberStation.getChannel());
      channel.setCallSign(grabberStation.getCallSign());
      channels.add(channel);
    }
    Collections.sort(channels);
    return channels;
  }

  protected Channel getChannel(List<Channel> channels, String lineupId, String stationId) {
    if(channels != null) {
      for (Channel channel : channels) {
        if (channel.getStationId().equals(stationId) && channel.getLineupId().equals(lineupId)) {
          return channel;
        }
      }
    }
    return null;
  }

  public void saveChannels(List<Channel> channels) {
    channelRepository.save(channels);
    refreshProfileLineups();
  }

  protected void createImages(Lineup<GrabberStation> lineup) {
    for (GrabberStation station : lineup.getStations()) {
      File stationLogoFile = new File(config.getStationImagesDir(), station.getCallSign() + ".png");
      File md5File = new File(config.getStationImagesDir(), station.getCallSign() + ".md5");
      String uri = "/images/stations/" + station.getCallSign() + ".png";

      try {
        if (station.getExternalPosterUrl() != null) {
          boolean download = false;
          if (stationLogoFile.exists()) {
            if (station.getExternalPosterMd5() != null) {
              if (md5File.exists()) {
                List<String> lines = Files.readAllLines(md5File.toPath(), StandardCharsets.UTF_8);
                String md5 = null;
                if (!lines.isEmpty()) {
                  md5 = lines.get(0);
                }

                if (!station.getExternalPosterMd5().equals(md5)) {
                  download = true;
                }
              } else {
                download = true;
              }
            }
          } else {
            download = true;
          }

          if (download) {
            log.info("Creating images for " + station.getCallSign());
            BufferedImage originalImage = ImageIO.read(new URL(station.getExternalPosterUrl()));
            if (originalImage != null) {
              BufferedImage adjustedImage = ImageUtil.scaleDownAndPadImage(originalImage, 300, 300, Color.WHITE);
              ImageUtil.saveImage(adjustedImage, stationLogoFile);
              if (station.getExternalPosterMd5() != null) {
                Files.write(md5File.toPath(), station.getExternalPosterMd5().getBytes());
              } else if (md5File.exists()) {
                Files.delete(md5File.toPath());
              }
            }
          }
        }
      } catch (IOException e) {
        log.error("Unexpected IOException creating images for " + station.getCallSign(), e);
      }

      if (!stationLogoFile.exists()) {
        log.info("Creating text image for " + station.getCallSign());
        BufferedImage image = ImageUtil.createTextImage(Arrays.asList(station.getCallSign()), 300, 300, Color.WHITE,
            Color.BLACK, new Font("Arial", Font.BOLD, 40), VALIGN.CENTER);
        try {
          ImageUtil.saveImage(image, stationLogoFile);
          if (md5File.exists()) {
            Files.delete(md5File.toPath());
          }

        } catch (IOException e) {
          log.error(e, e);
        }
      }

      if (stationLogoFile.exists()) {
        station.setHdPosterUri(uri);
        station.setSdPosterUri(uri);
      }
    }
  }

  public List<GrabberStation> findGrabberStations() {
    Lineup<GrabberStation> lineup = lineupRepository.get();
    if (lineup != null) {
      return lineup.getStations();
    }
    return new ArrayList<GrabberStation>();
  }

  public Set<GrabberStation> findGrabberStationsByStationId(String stationId) {
    Set<GrabberStation> stations = new HashSet<>();
    
    Lineup<GrabberStation> lineup = lineupRepository.get();
    if (lineup != null) {
      stations.addAll(lineup.getStations(stationId));
    }
    return stations;
  }

  public List<ProfileStation> findAllStations(String profileTitle) {
    Lineup<ProfileStation> lineup = profileLineupRepository.get(profileTitle);
    if (lineup != null) {
      List<ProfileStation> stations = lineup.getStations();
      Date date = new Date();
      DateFormat df = new SimpleDateFormat("h:mm");
      for (ProfileStation station : stations) {
        Airing airing = findAiring(profileTitle, station.getId(), date);
        if (airing != null) {
          String description = "";
          if (airing.getStart() != null) {
            description += df.format(airing.getStart());
            if (airing.getEnd() != null) {
              description += " - " + df.format(airing.getEnd());
            }
            description += "\n";
          }
          description += airing.getTitle();
          station.setDescription(description);
        }
      }
      return stations;
    }
    return new ArrayList<ProfileStation>();
  }

  protected Set<String> getDeviceIds(GrabberStation grabberStation) {
    Set<String> deviceStationIds = new HashSet<>();
    for (Device device : deviceService.findAllAvailableDevices()) {
      if (grabberStation.getLineupId().equals(device.getLineupId())
          || grabberStation.getLineupId().equals(device.getId())) {
        for (DeviceStation ds : device.getStations()) {
          if (ds.getChannel().equals(grabberStation.getChannel())) {
            deviceStationIds.add(ds.getId());
          }
        }
      }
    }
    return deviceStationIds;
  }

  public ProfileStation findProfileStationById(String profileTitle, String stationId) {
    Lineup<ProfileStation> lineup = profileLineupRepository.get(profileTitle);
    if (lineup != null) {
      List<ProfileStation> profileStations = lineup.getStations(stationId);
      if (profileStations != null && !profileStations.isEmpty()) {
        ProfileStation station = profileStations.get(0);
        if (station != null) {
          Date date = new Date();
          DateFormat df = new SimpleDateFormat("h:mm");
          Airing airing = findAiring(profileTitle, station.getId(), date);
          if (airing != null) {
            String description = "";
            if (airing.getStart() != null) {
              description += df.format(airing.getStart());
              if (airing.getEnd() != null) {
                description += " - " + df.format(airing.getEnd());
              }
              description += "\n";
            }
            description += airing.getTitle();
            station.setDescription(description);
          }
        }
        return station;
      }
    }
    return null;
  }

  public Map<String, List<Airing>> findAirings(String profileTitle, Integer hours, Integer offset) {
    if (offset == null) {
      offset = 0;
    }

    if (hours == null) {
      hours = 0;
    }

    Calendar from = new GregorianCalendar();
    from.setTime(new Date());
    from.set(Calendar.SECOND, 0);
    from.set(Calendar.MILLISECOND, 0);
    if (hours > 0) {
      from.add(Calendar.MINUTE, -(from.get(Calendar.MINUTE) % 30));
      from.add(Calendar.HOUR_OF_DAY, offset * hours);
    }

    Calendar to = new GregorianCalendar();
    to.setTime(from.getTime());
    to.add(Calendar.HOUR_OF_DAY, hours);

    Map<String, List<Airing>> stationAiringMap = new HashMap<>();
    Lineup<ProfileStation> lineup = profileLineupRepository.get(profileTitle);
    if (lineup != null) {
      List<ProfileStation> stations = lineup.getStations();
      for (ProfileStation station : stations) {
        Set<Airing> airingSet = new TreeSet<Airing>();
        for (Airing airing : lineup.getAirings(station.getId())) {
          if (airing.getStart().before(to.getTime()) && airing.getEnd().after(from.getTime())) {
            airingSet.add(airing);
          }
        }
        if (!airingSet.isEmpty()) {
          stationAiringMap.put(station.getId(), new ArrayList<Airing>(airingSet));
        }
      }
    }
    return stationAiringMap;
  }

  public List<Airing> findAirings(String profileTitle, String stationId) {
    Lineup<ProfileStation> lineup = profileLineupRepository.get(profileTitle);
    if (lineup != null) {
      return lineup.getAirings(stationId);
    } else {
      return new ArrayList<Airing>();
    }
  }

  public Airing findAiring(String profileTitle, String stationId, Date date) {
    Lineup<ProfileStation> lineup = profileLineupRepository.get(profileTitle);
    if (lineup != null) {
      for (Airing airing : lineup.getAirings(stationId)) {
        if (!airing.getStart().after(date) && date.before(airing.getEnd())) {
          return airing;
        }
      }
    }
    return null;
  }

  public Airing findAiring(String profileTitle, String airingId) {
    Lineup<ProfileStation> lineup = profileLineupRepository.get(profileTitle);
    if (lineup != null) {
      return lineup.getAiring(airingId);
    }
    return null;
  }

  public Set<String> getAllLineupIds() {
    Set<String> lineupIds = new HashSet<>();
    for (Device device : deviceService.findAllDevices()) {
      String lineupId = device.getLineupId();
      if (lineupId != null) {
        lineupIds.add(lineupId);
      }
    }
    return lineupIds;
  }
}

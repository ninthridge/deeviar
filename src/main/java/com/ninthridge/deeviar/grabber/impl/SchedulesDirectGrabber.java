package com.ninthridge.deeviar.grabber.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.grabber.GrabberException;
import com.ninthridge.deeviar.model.Airing;
import com.ninthridge.deeviar.model.Airing.AiringType;
import com.ninthridge.deeviar.model.GrabberConfig;
import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.Lineup;
import com.ninthridge.deeviar.util.TitleUtil;
import com.ninthridge.schedulesdirectclient.SdClient;
import com.ninthridge.schedulesdirectclient.exception.SchedulesDirectException;
import com.ninthridge.schedulesdirectclient.model.Description;
import com.ninthridge.schedulesdirectclient.model.Gracenote;
import com.ninthridge.schedulesdirectclient.model.Headend;
import com.ninthridge.schedulesdirectclient.model.Program;
import com.ninthridge.schedulesdirectclient.model.Schedule;
import com.ninthridge.schedulesdirectclient.model.Token;

@Service("schedulesDirectGrabber")
public class SchedulesDirectGrabber implements Grabber {

  protected final Log log = LogFactory.getLog(getClass());
  protected final SdClient sdClient = new SdClient();
  
  private Token token;
  
  private final Lock tokenLock = new ReentrantLock();
  
  public Lineup<GrabberStation> grabLineup(GrabberConfig grabberConfig, Set<String> lineupIds) throws GrabberException {
    String lineupIdsStr = "";
    for(String lineupId : lineupIds) {
      if(lineupIdsStr.length() > 0) {
        lineupIdsStr += ",";
      }
      lineupIdsStr += lineupId;
    }
    log.info("Grabbing lineup from Schedules Direct for " + lineupIdsStr);
    Lineup<GrabberStation> lineup = new Lineup<>();
    lineup.setLineupIds(lineupIds);
    lineup.setTimestamp(new Date());
    try {
      Token token = getToken(grabberConfig);
      if(token != null) {
        List<com.ninthridge.schedulesdirectclient.model.Lineup> sdLineups = getSdLineups(token, lineupIds);
        for(com.ninthridge.schedulesdirectclient.model.Lineup sdLineup : sdLineups) {
          List<com.ninthridge.schedulesdirectclient.model.Station> sdStations = sdClient.getStations(token, sdLineup);
          
          for (com.ninthridge.schedulesdirectclient.model.Station sdStation : sdStations) {
            String channel = null;
            if (sdStation.getChannels() != null && !sdStation.getChannels().isEmpty()) {
              if(sdStation.getChannels().get(0).getChannel() != null) {
                channel = sdStation.getChannels().get(0).getChannel();
              }
              else if(sdStation.getChannels().get(0).getAtscMajor() != null) {
                channel = sdStation.getChannels().get(0).getAtscMajor().toString();
                
                if(sdStation.getChannels().get(0).getAtscMinor() != null) {
                  channel += "." + sdStation.getChannels().get(0).getAtscMinor();
                }
              }
            }
            
            if (channel != null) {
              
              while(channel.charAt(0) == '0') {
                channel = channel.substring(1);
              }
              
              channel = channel.replace('-', '.');
              
              if(channel.length() > 0 && sdStation.getCallsign() != null) {
                GrabberStation station = new GrabberStation();
                station.setId(sdStation.getStationID());
                station.setChannel(channel);
                station.setCallSign(TitleUtil.cleanse(sdStation.getCallsign()).toUpperCase());
                station.setLineupId(sdLineup.getLineup());
                if(sdStation.getLogo() != null && sdStation.getLogo().getUrl() != null) {
                  station.setExternalPosterUrl(sdStation.getLogo().getUrl());
                  station.setExternalPosterMd5(sdStation.getLogo().getMd5());
                }
                
                lineup.addStation(station);
                log.debug("Station id=" + station.getId() + " channel=" + station.getChannel() + " callsign=" + station.getCallSign());
              }
            }
          }
          
          
          List<com.ninthridge.schedulesdirectclient.model.Schedule> sdSchedules = sdClient.getSchedules(token, sdStations);
  
          Set<String> programIds = new LinkedHashSet<>();
          for(Schedule sdSchedule : sdSchedules) {
            if(sdSchedule.getAirings() != null) {
              for(com.ninthridge.schedulesdirectclient.model.Airing airing : sdSchedule.getAirings()) {
                programIds.add(airing.getProgramID());
              }
            }
          }
          
          Map<String, Program> programMap = new HashMap<>();
          for(Program program : sdClient.getPrograms(token, new ArrayList<String>(programIds))) {
            programMap.put(program.getProgramID(), program);
          }
          
          for(com.ninthridge.schedulesdirectclient.model.Schedule sdSchedule : sdSchedules) {
            if(sdSchedule.getAirings() != null) {
              for(com.ninthridge.schedulesdirectclient.model.Airing sdAiring : sdSchedule.getAirings()) {
                Program program = programMap.get(sdAiring.getProgramID());
                
                List<GrabberStation> stations = lineup.getStations(sdSchedule.getStationID());
                if(!stations.isEmpty()) {
                  GrabberStation station = stations.get(0);
                  Airing airing = new Airing();
                  airing.setStationId(station.getId());
                  airing.setExternalPosterUrl(station.getExternalPosterUrl());
                  airing.setHdPosterUri(station.getHdPosterUri());
                  airing.setSdPosterUri(station.getSdPosterUri());
                  airing.setStart(sdAiring.getAirDateTime());
                  airing.setDuration(sdAiring.getDuration());
                  airing.setNewAiring(sdAiring.getNewAiring() != null ? sdAiring.getNewAiring() : false); 
                  airing.setOriginalAirDate(program.getOriginalAirDate());
                  airing.setTitle(getTitle(program));
                  airing.setDescription(getDescription(program));
                  airing.setId("A" + airing.getStationId() + airing.getStart().getTime());
                  
                  if("Series".equals(program.getShowType())) {
                    airing.setAiringType(AiringType.Episode);
                    airing.setEpisodeTitle(program.getEpisodeTitle150());
                    
                    if(program.getMetadata() != null && !program.getMetadata().isEmpty() && program.getMetadata().get(0).getGracenote() != null) {
                      Gracenote graceNote = program.getMetadata().get(0).getGracenote();
                      airing.setEpisode(graceNote.getEpisode());
                      airing.setSeason(graceNote.getSeason());
                    }
                    else {
                      Calendar cal = new GregorianCalendar();
                      cal.setTime(sdAiring.getAirDateTime());
                      airing.setSeason(cal.get(Calendar.YEAR));
                      airing.setEpisode(((cal.get(Calendar.MONTH)+1) * 100) + cal.get(Calendar.DAY_OF_MONTH));
                    }
                  }
                  else if("Movie".equals(program.getEntityType())) {
                    airing.setAiringType(AiringType.Movie);
                  }
                  else if("Sports".equals(program.getEntityType())) {
                    airing.setAiringType(AiringType.Sports);
                  }
                  else {
                    airing.setAiringType(AiringType.Show);
                  }
    
                  lineup.addAiring(airing);
                }
              }
            }
          }
        }
      }
      else {
        log.error("Unable to get a schedules direct token");
      }
    } catch (Exception e) {
      log.error("Unexpected exception getting airings from schedules direct", e);
      throw new GrabberException(e);
    }

    return lineup;
  }
  
  protected List<com.ninthridge.schedulesdirectclient.model.Lineup> getSdLineups(Token token, Set<String> lineupIds) throws GrabberException {
    List<com.ninthridge.schedulesdirectclient.model.Lineup> sdLineups = new ArrayList<>();
    try {
      List<com.ninthridge.schedulesdirectclient.model.Lineup> lineups = sdClient.getLineups(token);

      for(String lineupId : lineupIds) {
        boolean addLineup = true;
        for (com.ninthridge.schedulesdirectclient.model.Lineup lineup : lineups) {
          if (lineup.getLineup().equals(lineupId)) {
            sdLineups.add(lineup);
            addLineup = false;
          }
        }
        if(addLineup) {
          try {
            sdClient.addLineup(token, lineupId);
            lineups = sdClient.getLineups(token);
            for (com.ninthridge.schedulesdirectclient.model.Lineup lineup : lineups) {
              if (lineup.getLineup().equals(lineupId)) {
                sdLineups.add(lineup);
              }
            }
          }
          catch(Exception e) {
            throw new GrabberException("Failed to add lineup: " + lineupId + " " + e);
          }
        }
      }
    } catch (Exception e) {
      log.error("Unexpected Exception retrieving schedules direct lineup", e);
    }
    return sdLineups;
  }

  public Set<Map<String, String>> getLineups(GrabberConfig grabberConfig, String countryCode, String postalCode) throws GrabberException {
    Set<Map<String, String>> set = new HashSet<>();
    try {
      Token token = getToken(grabberConfig);
      if(countryCode != null && countryCode.trim().length() > 0 && postalCode != null && postalCode.trim().length() > 0) {
        List<Headend> headends = sdClient.getHeadends(token, countryCode, postalCode);
        for(Headend headend : headends) {
          for(com.ninthridge.schedulesdirectclient.model.Lineup lineup : headend.getLineups()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", lineup.getLineup());
            map.put("description", lineup.getName());
            set.add(map);
          }
        }
      }
      else {
        throw new GrabberException("Postal code is not configured");
      }
    } catch (SchedulesDirectException | IOException e) {
      log.error(e);
    } 
    return set;
  }
  
  private String getTitle(com.ninthridge.schedulesdirectclient.model.Program program) {
    if(program.getTitles() != null && !program.getTitles().isEmpty()) {
      return program.getTitles().get(0).getTitle120();
    }
    return null;
  }
  
  private String getDescription(com.ninthridge.schedulesdirectclient.model.Program program) {
    if(program.getDescriptions() != null && program.getDescriptions().getDescription1000() != null) {
      for(Description description : program.getDescriptions().getDescription1000()) {
        if("en".equals(description.getDescriptionLanguage())) {
          return description.getDescription();
        }
      }
    }
    return null;
  }
  
  private Token getToken(GrabberConfig grabberConfig) throws SchedulesDirectException, IOException {
    //cache for 12 hours
    if(token == null || new Date().getTime() - token.getCreatedDate().getTime() > (1000*60*60*12)) {
      try {
        tokenLock.lock();
        token = sdClient.requestToken(grabberConfig.getUsername(), grabberConfig.getPassword());
      }
      finally {
        tokenLock.unlock();
      }
    }
    return token;
  }
}

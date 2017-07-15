package com.ninthridge.deeviar.api;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.model.Channel;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.service.LineupService;
import com.ninthridge.deeviar.service.ProfileService;

@Controller
@RequestMapping("/api/lineup")
public class LineupResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private LineupService lineupService;

  @Autowired
  private ProfileService profileService;
  
  @RequestMapping(method = RequestMethod.GET, value="/stations")
  public @ResponseBody ResponseEntity<?> getStations(@RequestHeader(value="token") String token) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getStations: profileTitle=" + profile.getTitle());
      try {
        return new ResponseEntity<>(lineupService.findAllStations(profile.getTitle()), HttpStatus.OK);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      log.warn("invalid token");
      return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
    }
  }

  @RequestMapping(method = RequestMethod.GET, value="/stations/{stationId}")
  public @ResponseBody ResponseEntity<?> getStation(@RequestHeader(value="token") String token, @PathVariable("stationId") String stationId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getStation: profileTitle=" + profile.getTitle() + " stationId=" + stationId);
      try {
        return new ResponseEntity<>(lineupService.findProfileStationById(profile.getTitle(), stationId), HttpStatus.OK);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      log.warn("invalid token");
      return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
    }
  }

  @RequestMapping(method = RequestMethod.GET, value="/airings")
  public @ResponseBody ResponseEntity<?> get(@RequestHeader("token") String token, @RequestParam(value="hours", required=false) Integer hours, @RequestParam(value="offset", required=false) Integer offset) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getAirings: hours=" + hours + " offset=" + offset);
      try {
        return new ResponseEntity<>(lineupService.findAirings(profile.getTitle(), hours, offset), HttpStatus.OK);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      log.warn("invalid token");
      return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.GET, value="/channels")
  public @ResponseBody ResponseEntity<?> getChannels() {
    return new ResponseEntity<>(lineupService.getChannels(), HttpStatus.OK);
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.PUT, value="/channels")
  public @ResponseBody ResponseEntity<?> saveChannels(@RequestBody List<Channel> channels) {
    lineupService.saveChannels(channels);
    return new ResponseEntity<>(lineupService.getChannels(), HttpStatus.OK);
  }
}

package com.ninthridge.deeviar.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.Stream;
import com.ninthridge.deeviar.service.ProfileService;
import com.ninthridge.deeviar.service.StreamService;

@Controller
@RequestMapping("/api/streams")
public class StreamResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private StreamService streamService;

  @Autowired
  private ProfileService profileService;
  
  @RequestMapping(method = RequestMethod.PUT, value="/stations/{stationId}")
  public @ResponseBody ResponseEntity<?> stream(@RequestHeader("token") String token, @PathVariable("stationId") String stationId, HttpServletRequest request) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("startStation: stationId=" + stationId + " client=" + request.getRemoteAddr());
      try {
        Stream stream = streamService.stream(profile.getTitle(), stationId, request.getRemoteAddr(), null);
        if(stream != null) {
          return new ResponseEntity<>(stream, HttpStatus.OK);
        }
        else {
          return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      log.warn("invalid token");
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

  @RequestMapping(method = RequestMethod.GET, value="/stations/{stationId}")
  public @ResponseBody ResponseEntity<?> getStationStream(@RequestHeader("token") String token, @PathVariable("stationId") String stationId, HttpServletRequest request) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getStationStream: stationId=" + stationId + " client=" + request.getRemoteAddr());
      try {
        Stream stream = streamService.getStream(profile.getTitle(), stationId);
        if(stream != null) {
          return new ResponseEntity<>(stream, HttpStatus.OK);
        }
        else {
          return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      log.warn("invalid token");
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

  @RequestMapping(method = RequestMethod.DELETE)
  public @ResponseBody ResponseEntity<?> stopStream(@RequestHeader("token") String token, HttpServletRequest request) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("stopStream: client=" + request.getRemoteAddr());
      try {
        streamService.untune(request.getRemoteAddr());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      log.warn("invalid token");
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }
}

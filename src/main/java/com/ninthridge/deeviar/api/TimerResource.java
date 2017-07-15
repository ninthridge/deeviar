package com.ninthridge.deeviar.api;

import java.util.Date;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.TimerType;
import com.ninthridge.deeviar.service.ProfileService;
import com.ninthridge.deeviar.service.TimerService;

@Controller
@RequestMapping("/api/timers")
public class TimerResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private TimerService timerService;

  @Autowired
  private ProfileService profileService;
  
  @RequestMapping(method = RequestMethod.PUT, value="/airing/{airingId}/{timerType}")
  public @ResponseBody ResponseEntity<?> addTimer(@RequestHeader("token") String token, @PathVariable("airingId") String airingId, @PathVariable("timerType") TimerType timerType) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("addTimer: airingId=" + airingId + " timerType=" + timerType);
      try {
        return new ResponseEntity<>(timerService.add(profile.getTitle(), airingId, timerType), HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> getTimers(@RequestHeader("token") String token) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getTimers");
      try {
        return new ResponseEntity<>(timerService.getTimers(profile.getTitle()), HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.GET, value="/occurrences")
  public @ResponseBody ResponseEntity<?> getTimerOccurrences(@RequestHeader("token") String token, @RequestParam(value="stationId", required=false) String stationId, @RequestParam(value="date", required=false) Long date) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getTimerOccurrences");
      try {
        //date hack to work around roku's epoch millisecond limitation.  Assume that any date < 1000000 is in seconds, otherwise it is in milliseconds.  
        //This should work as long as we're dealing with dates between Jan 12 1970 and Nov 20 2286
        return new ResponseEntity<>(timerService.getTimerOccurrences(profile.getTitle(), stationId, (date != null) ? new Date((date < 1000000) ? date*1000 : date) : null), HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.DELETE, value="/{timerId}")
  public @ResponseBody ResponseEntity<?> deleteTimer(@RequestHeader("token") String token, @PathVariable("timerId") String timerId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("deleteTimer: timerId=" + timerId);
      try {
        timerService.deleteTimer(profile.getTitle(), timerId);
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

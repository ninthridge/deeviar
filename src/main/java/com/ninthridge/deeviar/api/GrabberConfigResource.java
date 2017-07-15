package com.ninthridge.deeviar.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.grabber.LineupGrabber;
import com.ninthridge.deeviar.model.GrabberConfig;
import com.ninthridge.deeviar.service.GrabberConfigService;

@Controller
@RequestMapping("/api/grabber")
public class GrabberConfigResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private GrabberConfigService grabberConfigService;

  @Autowired
  private LineupGrabber lineupGrabber;
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> getGrabberConfig() {
    log.info("getGrabberConfig");
    try {
      return new ResponseEntity<>(grabberConfigService.getGrabberConfig(), HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.PUT)
  public @ResponseBody ResponseEntity<?> saveGrabberConfig(@RequestBody GrabberConfig grabberConfig) {
    log.info("saveGrabberConfig");
    try {
      grabberConfigService.saveGrabberConfig(grabberConfig);
      return new ResponseEntity<>(grabberConfig, HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.GET, value="/lineups/{countryCode}/{postalCode}")
  public @ResponseBody ResponseEntity<?> getLineups(@PathVariable("countryCode") String countryCode, @PathVariable("postalCode") String postalCode) throws Exception {
    try {
      return new ResponseEntity<>(lineupGrabber.getLineups(countryCode, postalCode), HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

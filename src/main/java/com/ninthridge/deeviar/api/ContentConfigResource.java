package com.ninthridge.deeviar.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.model.ContentConfig;
import com.ninthridge.deeviar.service.ContentConfigService;

@Controller
@RequestMapping("/api/content")
public class ContentConfigResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private ContentConfigService contentConfigService;

  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> getContentConfig() {
    log.info("getContentConfig");
    try {
      return new ResponseEntity<>(contentConfigService.getContentConfig(), HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.PUT)
  public @ResponseBody ResponseEntity<?> saveContentConfig(@RequestBody ContentConfig contentConfig) {
    log.info("saveContentConfig");
    try {
      contentConfigService.saveContentConfig(contentConfig);
      return new ResponseEntity<>(contentConfig, HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

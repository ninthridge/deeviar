package com.ninthridge.deeviar.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.service.ProfileService;

@Controller
@RequestMapping("/api/token")
public class TokenResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private ProfileService profileService;
  
  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> requestToken(@RequestParam(value="profileTitle", required=true) String profileTitle, @RequestParam(value="pin", required=false) String pin) {
    log.info("requestToken");
    if(profileTitle != null) {
      Profile profile = profileService.find(profileTitle);
      if(profile != null && (profile.getPin() == null || profile.getPin().equals("") || profile.getPin().equals(pin))) {
        try {
          return new ResponseEntity<>(profile.getToken(), HttpStatus.OK);
        }
        catch(Exception e) {
          log.error(e, e);
          return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
    }
    
    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
  }
}

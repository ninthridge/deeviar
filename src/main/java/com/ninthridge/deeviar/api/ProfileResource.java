package com.ninthridge.deeviar.api;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.ProfileDto;
import com.ninthridge.deeviar.service.ProfileService;
import com.ninthridge.deeviar.util.FileNameUtil;

@Controller
public class ProfileResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private ProfileService profileService;

  @RequestMapping(method = RequestMethod.GET, value="/api/profile")
  public @ResponseBody ResponseEntity<?> getProfiles(@RequestHeader("token") String token) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      try {
        return new ResponseEntity<>(convert(profile), HttpStatus.OK);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }
  
  @RequestMapping(method = RequestMethod.GET, value="/api/profiles")
  public @ResponseBody ResponseEntity<?> getProfiles(@RequestParam(value="all", required=false) Boolean all) {
    log.info("getProfiles");
    try {
      Set<ProfileDto> profileDtos = new HashSet<>();
      Set<Profile> profiles = null;
      if(all != null && all) {
        profiles = profileService.findAll();
      }
      else {
        profiles = profileService.findEnabled();
      }
      
      for(Profile profile : profiles) {
        profileDtos.add(convert(profile));
      }
      
      return new ResponseEntity<>(profileDtos, HttpStatus.OK);
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.GET, value="/api/profiles/{profileTitle}")
  public @ResponseBody ResponseEntity<?> getProfile(@PathVariable("profileTitle") String profileTitle) {
    log.info("getProfile " + profileTitle);
    Profile profile = profileService.find(profileTitle);
    if(profile != null) {
      try {
        return new ResponseEntity<>(profile, HttpStatus.OK);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      return new ResponseEntity<>("Invalid profile: " + profileTitle, HttpStatus.BAD_REQUEST);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.PATCH, value="/api/profiles/{profileTitle}")
  public @ResponseBody ResponseEntity<?> saveProfile(@PathVariable("profileTitle") String profileTitle, @RequestBody Profile profile) {
    log.info("saveProfile " + profileTitle);
    try {
      profileService.saveProfile(profile);
      
      profile = profileService.find(profileTitle);
      if(profile != null) {
        try {
          return new ResponseEntity<>(profile, HttpStatus.OK);
        }
        catch(Exception e) {
          log.error(e, e);
          return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
      else {
        return new ResponseEntity<>("Invalid profile: " + profileTitle, HttpStatus.BAD_REQUEST);
      }
    }
    catch(Exception e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(method = RequestMethod.DELETE, value="/api/profiles/{profileTitle}")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public @ResponseBody ResponseEntity<?> deleteProfile(@PathVariable("profileTitle") String profileTitle) {
    log.info("deleteProfile: profileTitle=" + profileTitle);
    if(profileService.find(profileTitle) != null) {
      try {
        profileService.deleteProfile(profileTitle);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      return new ResponseEntity<>("Invalid profile: " + profileTitle, HttpStatus.BAD_REQUEST);
    }
  }
  
  //TODO: should only accessible to authenticated webapp user
  @RequestMapping(value="/api/profiles/{profileTitle}/image", method=RequestMethod.POST)
  public @ResponseBody ResponseEntity<?> handleFileUpload(@PathVariable("profileTitle") String profileTitle, @RequestParam("file") MultipartFile file) {
    log.info("profile image - profileTitle=" + profileTitle + " file=" + file.getOriginalFilename());
    if(profileTitle != null) {
      Profile profile = profileService.find(profileTitle);
      try {
        if(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename())) {
          profileService.saveProfileImage(profile, null, null);
        }
        else if(FileNameUtil.isImageFile(file.getOriginalFilename())) {
          BufferedImage image = ImageIO.read(file.getInputStream());
          profileService.saveProfileImage(profile, image, FileNameUtil.parseExtension(file.getOriginalFilename()));
        }
        else {
          return new ResponseEntity<>("Invalid image", HttpStatus.BAD_REQUEST);
        }
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      return new ResponseEntity<>("Invalid profile: " + profileTitle, HttpStatus.BAD_REQUEST);
    }
    
    Profile profile = profileService.find(profileTitle);
    if(profile != null) {
      try {
        return new ResponseEntity<>(profile, HttpStatus.OK);
      }
      catch(Exception e) {
        log.error(e, e);
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      return new ResponseEntity<>("Invalid profile: " + profileTitle, HttpStatus.BAD_REQUEST);
    }
  }
  
  protected ProfileDto convert(Profile profile) {
    if(profile != null) {
      ProfileDto profileDto = new ProfileDto();
      profileDto.setTitle(profile.getTitle());
      profileDto.setHdPosterUri(profile.getHdPosterUri());
      profileDto.setSdPosterUri(profile.getSdPosterUri());
      profileDto.setPermissions(profile.getPermissions());
      profileDto.setRestricted(profile.getPin() != null && profile.getPin().trim().length() > 0);
      return profileDto;
    }
    return null;
  }
  
  
}

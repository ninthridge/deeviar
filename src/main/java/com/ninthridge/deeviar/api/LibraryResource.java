package com.ninthridge.deeviar.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.multipart.MultipartFile;

import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.SubtitleTrack;
import com.ninthridge.deeviar.service.LibraryService;
import com.ninthridge.deeviar.service.ProfileService;
import com.ninthridge.deeviar.util.FileNameUtil;

@Controller
@RequestMapping("/api/library")
public class LibraryResource {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private LibraryService libraryService;

  @Autowired
  private ProfileService profileService;
  
  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> getLibrary(@RequestHeader("token") String token, @RequestParam(value="category", required=false) String category) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getLibrary: profileTitle=" + profile.getTitle());
      try {
        if(category != null && category.trim().length() > 0) {
          return new ResponseEntity<>(libraryService.findAllActive(profile.getTitle(), category), HttpStatus.OK);
        }
        else {
          return new ResponseEntity<>(libraryService.findAllActive(profile.getTitle()), HttpStatus.OK);
        }
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

  @RequestMapping(method = RequestMethod.GET, value="/videos/{videoId}")
  public @ResponseBody ResponseEntity<?> getVideo(@RequestHeader("token") String token, @PathVariable("videoId") String videoId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getVideo: profileTitle=" + profile.getTitle());
      try {
        return new ResponseEntity<>(libraryService.findById(profile.getTitle(), videoId), HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.GET, value="/series/{seriesId}")
  public @ResponseBody ResponseEntity<?> getSeries(@RequestHeader("token") String token, @PathVariable("seriesId") String seriesId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getVideo: profileTitle=" + profile.getTitle());
      try {
        return new ResponseEntity<>(libraryService.findSeriesById(profile.getTitle(), seriesId), HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.GET, value="/timestamp")
  public @ResponseBody ResponseEntity<?> getLibraryTimestamp(@RequestHeader("token") String token) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getLibraryTimestamp: profileTitle=" + profile.getTitle());
      try {
        Map<String, Date> map = new HashMap<>();
        map.put("timestamp", libraryService.getTimestamp(profile.getTitle()));
        return new ResponseEntity<>(map, HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.GET, value="/categories")
  public @ResponseBody ResponseEntity<?> getCategories(@RequestHeader("token") String token) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("getCategories: profileTitle=" + profile.getTitle());
      try {
        return new ResponseEntity<>(libraryService.findActiveCategories(profile.getTitle()), HttpStatus.OK);
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

  @RequestMapping(method = {RequestMethod.PUT}, value="/videos/{videoId}/bookmark/{bookmarkPosition}")
  public @ResponseBody ResponseEntity<?> bookmark(@RequestHeader("token") String token, @PathVariable("videoId") String videoId, @PathVariable("bookmarkPosition") Integer bookmarkPosition) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("bookmark: profileTitle=" + profile.getTitle() + " videoId=" + videoId + " bookmarkPosition=" + bookmarkPosition);
      try {
        return new ResponseEntity<>(libraryService.bookmark(profile.getTitle(), videoId, bookmarkPosition), HttpStatus.OK);
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

  @RequestMapping(method = {RequestMethod.PUT}, value="/favorites/{videoId}")
  public @ResponseBody ResponseEntity<?> favorite(@RequestHeader("token") String token, @PathVariable("videoId") String videoId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("bookmark: profileTitle=" + profile.getTitle() + " videoId=" + videoId + " favorite");
      try {
        return new ResponseEntity<>(libraryService.favorite(profile.getTitle(), videoId, true), HttpStatus.OK);
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

  @RequestMapping(method = {RequestMethod.DELETE}, value="/favorites/{videoId}")
  public @ResponseBody ResponseEntity<?> unfavorite(@RequestHeader("token") String token, @PathVariable("videoId") String videoId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("unfavorite: profileTitle=" + profile.getTitle() + " videoId=" + videoId);
      try {
        return new ResponseEntity<>(libraryService.favorite(profile.getTitle(), videoId, false), HttpStatus.OK);
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

  @RequestMapping(method = RequestMethod.DELETE, value="/videos/{videoId}")
  public @ResponseBody ResponseEntity<?> delete(@RequestHeader("token") String token, @PathVariable("videoId") String videoId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("delete video: profileTitle=" + profile.getTitle() + " videoId=" + videoId);
      try {
        libraryService.delete(profile.getTitle(), videoId);
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
  
  @RequestMapping(value="/videos/{videoId}/subtitles", method=RequestMethod.POST)
  public @ResponseBody ResponseEntity<?> updateSubtitleTrackFile(@RequestHeader("token") String token, @PathVariable("videoId") String videoId, @RequestParam("language") String language, @RequestParam("description") String description, @RequestParam("file") MultipartFile file) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("updateSubtitleTrackFile - profileTitle=" + profile.getTitle() + " videoId=" + videoId + " language=" + language + " description=" + description + " file=" + file.getOriginalFilename());
      
      try {
        if(FileNameUtil.isSubtitleTrackFile(file.getOriginalFilename())) {
          return new ResponseEntity<>(libraryService.addSubtitleTrack(profile.getTitle(), videoId, language, description, file.getBytes(), FileNameUtil.parseExtension(file.getOriginalFilename())), HttpStatus.OK);
        }
        else {
          String s = "";
          for(int i=0; i<FileNameUtil.SUBTITLE_FILE_EXTENSIONS.size(); i++) {
            if(i > 0) {
              s += ", ";
              if(i+1 == FileNameUtil.SUBTITLE_FILE_EXTENSIONS.size()) {
                s += " or ";
              }
            }
            s += FileNameUtil.SUBTITLE_FILE_EXTENSIONS.get(i);
          }
          return new ResponseEntity<>("Invalid subtitle track file.  Must be of type " + s, HttpStatus.BAD_REQUEST);
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
  
  @RequestMapping(value="/videos/{videoId}/subtitles", method=RequestMethod.PUT)
  public @ResponseBody ResponseEntity<?> addSubtitleTrack(@RequestHeader("token") String token, @PathVariable("videoId") String videoId, @RequestBody SubtitleTrack subtitleTrack) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("addSubtitleTrack - profileTitle=" + profile.getTitle() + " videoId=" + videoId + " language=" + subtitleTrack.getLanguage() + " description=" + subtitleTrack.getDescription() + " url=" + subtitleTrack.getUri());
      
      try {
        if(FileNameUtil.isSubtitleTrackFile(subtitleTrack.getUri())) {
          return new ResponseEntity<>(libraryService.addSubtitleTrack(profile.getTitle(), videoId, subtitleTrack.getLanguage(), subtitleTrack.getDescription(), subtitleTrack.getUri()), HttpStatus.OK);
        }
        else {
          String s = "";
          for(int i=0; i<FileNameUtil.SUBTITLE_FILE_EXTENSIONS.size(); i++) {
            if(i > 0) {
              s += ", ";
              if(i+1 == FileNameUtil.SUBTITLE_FILE_EXTENSIONS.size()) {
                s += " or ";
              }
            }
            s += FileNameUtil.SUBTITLE_FILE_EXTENSIONS.get(i);
          }
          return new ResponseEntity<>("Invalid subtitle track file.  Must be of type " + s, HttpStatus.BAD_REQUEST);
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
  
  @RequestMapping(method = RequestMethod.DELETE, value="/videos/{videoId}/subtitles/{subtitleId}")
  public @ResponseBody ResponseEntity<?> deleteSubtitleTrack(@RequestHeader("token") String token, @PathVariable("videoId") String videoId, @PathVariable("subtitleId") String subtitleTrackId) {
    Profile profile = profileService.findByToken(token);
    if(profile != null) {
      log.info("delete subtitle: profileTitle=" + profile.getTitle() + " videoId=" + videoId + " subtitleTrackId=" + subtitleTrackId);
      try {
        libraryService.deleteSubtitleTrack(profile.getTitle(), videoId, subtitleTrackId);
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

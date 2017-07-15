package com.ninthridge.deeviar.library.scraper.impl.omdb;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ninthridge.deeviar.model.VideoContent;
import com.ninthridge.deeviar.repository.ContentConfigRepository;
import com.ninthridge.omdbclient.OmdbClient;
import com.ninthridge.omdbclient.exception.OmdbException;
import com.ninthridge.omdbclient.model.OmdbVideo;
import com.ninthridge.omdbclient.model.Type;

public class OmdbContentScraper {

  protected final Log log = LogFactory.getLog(getClass());
  protected final OmdbClient omdbClient = new OmdbClient();
  
  private DateFormat df = new SimpleDateFormat("dd MMM yyyy");
  
  @Autowired
  private ContentConfigRepository contentConfigRepository;
  
  protected boolean scrapeContent(VideoContent content, Type type) {
    OmdbVideo omdbVideo = null;
  
    if(contentConfigRepository.get() != null && contentConfigRepository.get().getApiKey() != null && !contentConfigRepository.get().getApiKey().isEmpty()) {
      String apiKey = contentConfigRepository.get().getApiKey();
      if(content.getImdbId() != null) {
        log.info("Searching OMDB for imdbId=" + content.getImdbId());
        try {
          omdbVideo = omdbClient.search(apiKey, content.getImdbId(), null, null, null, null, null, false, false);
        }
        catch(OmdbException | IOException e) {
          log.warn(e);
        }
      }
      else {
        log.info("Searching OMDB for title='" + content.getTitle() + "' year='" + content.getYear() + "' type='" + type + "'");
        
        try {
          omdbVideo = omdbClient.search(apiKey, null, content.getTitle(), content.getYear(), null, null, type, false, false);
        }
        catch(OmdbException | IOException e) {
          log.warn(e);
        }
        
        if(omdbVideo == null) {
          //apparently omdb doesn't resolve and/&
          if(content.getTitle().contains(" and ")) {
            String t = content.getTitle().replace(" and ", " & ");
            log.info("No OMDB search results found for '" + content.getTitle() + "'. Trying again with '" + t + "'");
            try {
              omdbVideo = omdbClient.search(apiKey, null, t, content.getYear(), null, null, type, false, false);
            }
            catch(OmdbException | IOException e) {
              log.warn(e);
            }
          }
          else if(content.getTitle().contains(" & ")) {
            String t = content.getTitle().replace(" & ", " and ");
            log.info("No OMDB search results found for '" + content.getTitle() + "'. Trying again with '" + t + "'");
            try {
              omdbVideo = omdbClient.search(apiKey, null, t, content.getYear(), null, null, type, false, false);
            }
            catch(OmdbException | IOException e) {
              log.warn(e);
            }
          }
        }
      }
    }
    
    if(omdbVideo != null && compare(content, omdbVideo)) {
      populateContent(content, omdbVideo);
      return true;
    }
    
    return false;
  }
  
  protected void populateContent(VideoContent content, OmdbVideo omdbVideo) {
    content.setTitle(omdbVideo.getTitle());
    content.setImdbId(omdbVideo.getImdbId());
    if(omdbVideo.getYear() != null) {
      Pattern pattern = Pattern.compile("[0-9]+");
      Matcher m = pattern.matcher(omdbVideo.getYear());
      if (m.find()) {
        content.setYear(new Integer(m.group(0)));
      }
    }
    content.setExternalPosterUrl(omdbVideo.getPoster());
    content.setDescription(omdbVideo.getPlot());
    content.setRated(omdbVideo.getRated());
    
    try {
      content.setReleased(df.parse(omdbVideo.getReleased()));
    } catch (ParseException e) {
    }
  }
  
  protected boolean compare(VideoContent content, OmdbVideo omdbVideo) {
    if(omdbVideo.getImdbId().equals(content.getImdbId())) {
      return true;
    }
    else {
      if(!cleanseTitle(omdbVideo.getTitle()).equalsIgnoreCase(cleanseTitle(content.getTitle()))) {
        return false;
      }
      if(content.getYear() != null && (omdbVideo.getYear() == null || !omdbVideo.getYear().startsWith(content.getYear().toString()))) {
        return false;
      }
      
      return true;
    }
  }

  private String cleanseTitle(String title) {
    return title.replaceAll(" & ", " and ").replaceAll("[^\\p{L}\\p{N}\\p{Z}]","").replaceAll("\\s+", " ").trim();
  }
}

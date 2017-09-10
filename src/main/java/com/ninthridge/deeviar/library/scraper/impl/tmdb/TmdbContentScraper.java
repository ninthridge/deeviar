package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ninthridge.deeviar.model.VideoContent;
import com.omertron.themoviedbapi.TheMovieDbApi;

public class TmdbContentScraper {

  protected static final Log log = LogFactory.getLog(TmdbContentScraper.class);

  protected DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
  
  protected static TheMovieDbApi tmdbClient = null;
  
  static {
    try {
      Properties prop = new Properties();
      prop.load(TmdbContentScraper.class.getClassLoader().getResourceAsStream("keys.properties"));
      tmdbClient = new TheMovieDbApi(prop.getProperty("tmdbApiKey"));
    } catch (Exception e) {
      log.error("Unable to initialize tmdbClient - " + e.toString(), e);
    }
  }
  
  protected boolean compare(VideoContent content, Integer tmdbId, String title, String releaseDate) {
    if(tmdbId != null && tmdbId.equals(content.getTmdbId())) {
      return true;
    }
    else {
      if(!cleanseTitle(title).equalsIgnoreCase(cleanseTitle(content.getTitle()))) {
        return false;
      }
      
      Integer year = null;
      try {
        Date date = df.parse(releaseDate);
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        year = cal.get(Calendar.YEAR);
      } catch (ParseException e) {
      }
      
      if(content.getYear() != null && (year == null || !year.equals(content.getYear()))) {
        return false;
      }
      
      return true;
    }
  }

  private String cleanseTitle(String title) {
    return title.replaceAll(" & ", " and ").replaceAll("[^\\p{L}\\p{N}\\p{Z}]","").replaceAll("\\s+", " ").trim();
  }
}

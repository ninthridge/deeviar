package com.ninthridge.deeviar.library.scraper.impl.omdb;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scraper.EpisodeScraper;
import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.repository.ContentConfigRepository;
import com.ninthridge.omdbclient.exception.OmdbException;
import com.ninthridge.omdbclient.model.OmdbVideo;
import com.ninthridge.omdbclient.model.Type;

@Service("episodeScraper")
public class OmdbEpisodeScraper extends OmdbContentScraper implements EpisodeScraper {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private ContentConfigRepository contentConfigRepository;
  
  @Override
  public boolean scrape(Series series, Episode episode) {
    OmdbVideo omdbVideo = null;
    
    if(contentConfigRepository.get() != null && contentConfigRepository.get().getApiKey() != null && !contentConfigRepository.get().getApiKey().isEmpty()) {
      String apiKey = contentConfigRepository.get().getApiKey();
      if(episode.getImdbId() != null) {
        log.info("Searching OMDB for imdbId='" + episode.getImdbId() + "'");
        try {
          omdbVideo = omdbClient.search(apiKey, episode.getImdbId(), null, null, null, null, Type.episode, false, false);
        } catch (OmdbException | IOException e) {
          log.warn(e);
        }
      }
      else if(series != null && series.getImdbId() != null && episode.getSeason() != null && episode.getEpisode() != null) {
        log.info("Searching OMDB for imdbId='" + series.getImdbId() + "' season='" + episode.getSeason() + "' episode='" + episode.getEpisode() + "'");
        try {
          omdbVideo = omdbClient.search(apiKey, series.getImdbId(), null, null, episode.getSeason(), episode.getEpisode(), Type.episode, false, false);
        } catch (OmdbException | IOException e) {
          log.warn(e);
        }
      }
    }
    
    if(omdbVideo != null) {
      populateContent(episode, omdbVideo);
      return true;
    }

    return false;
  }
}

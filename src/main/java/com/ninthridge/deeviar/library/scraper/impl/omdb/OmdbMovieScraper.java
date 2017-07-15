package com.ninthridge.deeviar.library.scraper.impl.omdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scraper.MovieScraper;
import com.ninthridge.deeviar.model.Movie;
import com.ninthridge.omdbclient.model.Type;

@Service("movieScraper")
public class OmdbMovieScraper extends OmdbContentScraper implements MovieScraper {

  protected final Log log = LogFactory.getLog(getClass());

  public boolean scrape(Movie content) {
    return scrapeContent(content, Type.movie);
  }
}

package com.ninthridge.deeviar.library.scraper.impl.omdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scraper.SeriesScraper;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.omdbclient.model.Type;

@Service("seriesScraper")
public class OmdbSeriesScraper extends OmdbContentScraper implements SeriesScraper {

  protected final Log log = LogFactory.getLog(getClass());

  public boolean scrape(Series series) {
    return scrapeContent(series, Type.series);
  }
}

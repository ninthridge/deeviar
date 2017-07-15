package com.ninthridge.deeviar.library.scraper;

import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.Series;

public interface EpisodeScraper {
  boolean scrape(Series series, Episode episode);
}

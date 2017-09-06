package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;

import com.ninthridge.deeviar.library.scraper.impl.tmdb.TmdbEpisodeScraper;
import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.model.id.EpisodeId;
import com.ninthridge.deeviar.model.id.SeriesId;

public class TmdbEpisodeScraperTest {

  private TmdbEpisodeScraper tmdbEpisodeScraper = new TmdbEpisodeScraper();
  
  @Test
  public void testScrape() throws ParseException {
    SeriesId seriesId = new SeriesId("Parks and Recreation", null);
    Series series = new Series(seriesId);
    series.setTmdbId(new Integer(8592));
    Episode episode = new Episode(new EpisodeId(seriesId, 1, 1));
    tmdbEpisodeScraper.scrape(series, episode);
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2009-04-09"), episode.getReleased());
    Assert.assertEquals(new Integer(397621), episode.getTmdbId());
    Assert.assertNotNull(episode.getRated());
  }
}

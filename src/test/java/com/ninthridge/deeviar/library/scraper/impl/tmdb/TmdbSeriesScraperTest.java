package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;

import com.ninthridge.deeviar.library.scraper.impl.tmdb.TmdbSeriesScraper;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.model.id.SeriesId;

public class TmdbSeriesScraperTest {

  private TmdbSeriesScraper tmdbSeriesScraper = new TmdbSeriesScraper();
  
  @Test
  public void testScrape() throws ParseException {
    Series series = new Series(new SeriesId("Parks and Recreation", null));
    tmdbSeriesScraper.scrape(series);
    Assert.assertNotNull(series.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2009-04-09"), series.getReleased());
    Assert.assertEquals(new Integer(8592), series.getTmdbId());
    Assert.assertNotNull(series.getExternalPosterUrl());
    Assert.assertNotNull(series.getRated());
  }
  
  @Test
  public void testScrapeWithYear() throws ParseException {
    Series series = new Series(new SeriesId("Late Night with Seth Meyers", 2014));
    tmdbSeriesScraper.scrape(series);
    Assert.assertNotNull(series.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2014-12-02"), series.getReleased());
    Assert.assertEquals(new Integer(61818), series.getTmdbId());
    Assert.assertNotNull(series.getExternalPosterUrl());
    Assert.assertNotNull(series.getRated());
  }
  
  @Test
  public void testScrapeWithoutYear() throws ParseException {
    Series series = new Series(new SeriesId("Late Night with Seth Meyers", null));
    tmdbSeriesScraper.scrape(series);
    Assert.assertNotNull(series.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2014-12-02"), series.getReleased());
    Assert.assertEquals(new Integer(61818), series.getTmdbId());
    Assert.assertNotNull(series.getExternalPosterUrl());
    Assert.assertNotNull(series.getRated());
  }
  
  
}

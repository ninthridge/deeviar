package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.ninthridge.deeviar.library.scraper.impl.tmdb.TmdbMovieScraper;
import com.ninthridge.deeviar.model.Movie;
import com.ninthridge.deeviar.model.id.MovieId;

@Ignore
public class TmdbMovieScraperTest {

  private TmdbMovieScraper tmdbMovieScraper = new TmdbMovieScraper();
  
  @Test
  public void testScrape() throws ParseException {
    Movie movie = new Movie(new MovieId("The Dark Knight", 2008));
    tmdbMovieScraper.scrape(movie);
    Assert.assertNotNull(movie.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2008-07-16"), movie.getReleased());
    Assert.assertEquals(new Integer(155), movie.getTmdbId());
    Assert.assertNotNull(movie.getExternalPosterUrl());
    Assert.assertNotNull(movie.getRated());
  }
  
  @Test
  public void testScrapeAmpersand() throws ParseException {
    Movie movie = new Movie(new MovieId("Cowboys & Aliens", 2011));
    tmdbMovieScraper.scrape(movie);
    Assert.assertNotNull(movie.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2011-07-29"), movie.getReleased());
    Assert.assertEquals(new Integer(49849), movie.getTmdbId());
    Assert.assertNotNull(movie.getExternalPosterUrl());
    Assert.assertNotNull(movie.getRated());
  }
  
  @Test
  public void testScrapeWrongAnd() throws ParseException {
    Movie movie = new Movie(new MovieId("Cowboys and Aliens", 2011));
    tmdbMovieScraper.scrape(movie);
    Assert.assertNotNull(movie.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2011-07-29"), movie.getReleased());
    Assert.assertEquals(new Integer(49849), movie.getTmdbId());
    Assert.assertNotNull(movie.getExternalPosterUrl());
    Assert.assertNotNull(movie.getRated());
  }
  
  @Test
  public void testApostrophe() throws ParseException {
    Movie movie = new Movie(new MovieId("The 'Burbs", 1989));
    tmdbMovieScraper.scrape(movie);
    Assert.assertNotNull(movie.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("1989-02-17"), movie.getReleased());
    Assert.assertEquals(new Integer(11974), movie.getTmdbId());
    Assert.assertNotNull(movie.getExternalPosterUrl());
    Assert.assertNotNull(movie.getRated());
  }
  
  @Test
  public void testMissingApostrophe() throws ParseException {
    Movie movie = new Movie(new MovieId("The Burbs", 1989));
    tmdbMovieScraper.scrape(movie);
    Assert.assertNotNull(movie.getDescription());
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("1989-02-17"), movie.getReleased());
    Assert.assertEquals(new Integer(11974), movie.getTmdbId());
    Assert.assertNotNull(movie.getExternalPosterUrl());
    Assert.assertNotNull(movie.getRated());
  }
}

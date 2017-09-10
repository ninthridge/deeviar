package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scraper.MovieScraper;
import com.ninthridge.deeviar.model.Movie;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

@Service("movieScraper")
public class TmdbMovieScraper extends TmdbContentScraper implements MovieScraper {

  public boolean scrape(Movie movie) {
    if(tmdbClient != null) {
      ResultList<MovieInfo> results = null;
      try {
        results = tmdbClient.searchMovie(movie.getTitle(), null, null, null, movie.getYear(), null, null);
      } catch (MovieDbException e) {
        log.error(e, e);
      }
      if(results != null && results.getResults() != null && !results.getResults().isEmpty()) {
        MovieInfo movieInfo = results.getResults().get(0);
        log.info("Found tmdbId=" + movieInfo.getId() + " title='" + movieInfo.getTitle() + "' year='" + movieInfo.getReleaseDate());
        if(compare(movie, movieInfo.getId(), movieInfo.getTitle(), movieInfo.getReleaseDate())) {
          //movie.setTitle(movieInfo.getTitle());
          movie.setTmdbId(movieInfo.getId());
          movie.setImdbId(movieInfo.getImdbID());
          
          try {
            movie.setReleased(df.parse(movieInfo.getReleaseDate()));
            //Calendar cal = new GregorianCalendar();
            //cal.setTime(movie.getReleased());
            //movie.setYear(cal.get(Calendar.YEAR));
          } catch (ParseException e) {
          }
          //if(movie.getReleased() != null) {
          //  Pattern pattern = Pattern.compile("[0-9]{4}");
          //  Matcher m = pattern.matcher(movieInfo.getReleaseDate());
          //  if (m.find()) {
          //    movie.setYear(new Integer(m.group(0)));
          //  }
          //}
          if(movieInfo.getPosterPath() != null) {
            movie.setExternalPosterUrl("https://image.tmdb.org/t/p/w500" + movieInfo.getPosterPath());
          }
          movie.setDescription(movieInfo.getOverview());
          movie.setRated(new Float(movieInfo.getVoteAverage()).toString());
          
          return true;
        }
      }
  
      log.info("No results found for title='" + movie.getTitle() + "' year='" + movie.getYear());
    }
    return false;
  }
}

package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scraper.SeriesScraper;
import com.ninthridge.deeviar.model.Series;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.tv.TVBasic;
import com.omertron.themoviedbapi.results.ResultList;

@Service("seriesScraper")
public class TmdbSeriesScraper extends TmdbContentScraper implements SeriesScraper {

  public boolean scrape(Series series) {
    if(tmdbClient != null) {
      ResultList<TVBasic> results = null;
      try {
        results = tmdbClient.searchTV(series.getTitle(), null, null, series.getYear(), null);
      } catch (MovieDbException e) {
        log.error(e, e);
      }
      if(results != null && results.getResults() != null && !results.getResults().isEmpty()) {
        TVBasic tvBasic = results.getResults().get(0);
        log.info("Found tmdbId=" + tvBasic.getId() + " title='" + tvBasic.getName() + "' year='" + tvBasic.getFirstAirDate());
        if(compare(series, tvBasic.getId(), tvBasic.getName(), tvBasic.getFirstAirDate())) {
          //series.setTitle(tvBasic.getName());
          series.setTmdbId(tvBasic.getId());
          
          try {
            series.setReleased(df.parse(tvBasic.getFirstAirDate()));
            //Calendar cal = new GregorianCalendar();
            //cal.setTime(series.getReleased());
            //series.setYear(cal.get(Calendar.YEAR));
          } catch (ParseException e) {
          }
          //if(series.getReleased() != null) {
          //  Pattern pattern = Pattern.compile("[0-9]{4}");
          //  Matcher m = pattern.matcher(tvBasic.getFirstAirDate());
          //  if (m.find()) {
          //    series.setYear(new Integer(m.group(0)));
          //  }
          //}
          if(tvBasic.getPosterPath() != null) {
            series.setExternalPosterUrl("https://image.tmdb.org/t/p/w500" + tvBasic.getPosterPath());
          }
          series.setDescription(tvBasic.getOverview());
          series.setRated(new Float(tvBasic.getVoteAverage()).toString());
          
          return true;
        }
      }
  
      log.info("No results found for title='" + series.getTitle() + "' year='" + series.getYear());
    }
    return false;
  }
}

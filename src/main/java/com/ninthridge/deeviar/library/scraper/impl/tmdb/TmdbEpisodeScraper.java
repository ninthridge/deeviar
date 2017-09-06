package com.ninthridge.deeviar.library.scraper.impl.tmdb;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scraper.EpisodeScraper;
import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.Series;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.tv.TVEpisodeInfo;

@Service("episodeScraper")
public class TmdbEpisodeScraper extends TmdbContentScraper implements EpisodeScraper {

  @Override
  public boolean scrape(Series series, Episode episode) {
    if(tmdbClient != null) {
      TVEpisodeInfo tvEpisodeInfo = null;
      try {
        tvEpisodeInfo = tmdbClient.getEpisodeInfo(new Integer(series.getTmdbId()), episode.getSeason(), episode.getEpisode(), null);
      } catch (MovieDbException e) {
        log.error(e, e);
      }
      if(tvEpisodeInfo != null) {
        log.info("Found tmdbId=" + tvEpisodeInfo.getId() + " title='" + tvEpisodeInfo.getName() + "' year='" + tvEpisodeInfo.getAirDate());
        //series.setTitle(tvEpisodeInfo.getName());
        episode.setTmdbId(tvEpisodeInfo.getId());
        
        try {
          episode.setReleased(df.parse(tvEpisodeInfo.getAirDate()));
          //Calendar cal = new GregorianCalendar();
          //cal.setTime(series.getReleased());
          //episode.setYear(cal.get(Calendar.YEAR));
        } catch (ParseException e) {
        }
        //if(episode.getReleased() != null) {
        //  Pattern pattern = Pattern.compile("[0-9]{4}");
        //  Matcher m = pattern.matcher(tvEpisodeInfo.getAirDate());
        //  if (m.find()) {
        //    episode.setYear(new Integer(m.group(0)));
        //  }
        //}
        if(tvEpisodeInfo.getPosterPath() != null) {
          episode.setExternalPosterUrl("https://image.tmdb.org/t/p/w500" + tvEpisodeInfo.getPosterPath());
        }
        
        episode.setDescription(tvEpisodeInfo.getOverview());
        episode.setRated(new Float(tvEpisodeInfo.getVoteAverage()).toString());
        
        return true;
      }
  
      log.info("No results found for " + series.getTmdbId() + " " + episode.getSeason() + " " + episode.getEpisode());
    }
    return false;
  }
}

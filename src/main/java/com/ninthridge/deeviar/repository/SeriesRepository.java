package com.ninthridge.deeviar.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.Profile;
import com.ninthridge.deeviar.model.Series;

@Repository("seriesRepository")
public class SeriesRepository extends CachedOwnerMultiJsonRepository<Series> {

  @Autowired
  private EpisodeRepository episodeRepository;
  
  @Autowired
  private ProfileRepository profileRepository;
  
  public SeriesRepository() {
    super(new TypeReference<Series>(){});
  }

  // This is to populate the list of episode associated with a series since they are stored separately
  @Override
  protected void initialize() {
    super.initialize();
    for(Profile profile : profileRepository.getAll()) {
      for(Episode episode : episodeRepository.getAll(profile.getTitle())) {
        Series series = this.get(profile.getTitle(), episode.getSeriesId());
        if(series != null) {
          series.getEpisodes().add(episode);
        }
      }
    }
  }
  
  @Override
  protected String getDirName() {
    return "series";
  }

  @Override
  protected String getId(Series series) {
    return series.getId();
  }
}

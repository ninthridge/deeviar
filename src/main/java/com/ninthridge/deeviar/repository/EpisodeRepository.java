package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Episode;

@Repository("episodeRepository")
public class EpisodeRepository extends CachedOwnerMultiJsonRepository<Episode> {

  public EpisodeRepository() {
    super(new TypeReference<Episode>(){});
  }

  @Override
  protected String getDirName() {
    return "episodes";
  }

  @Override
  protected String getId(Episode episode) {
    return episode.getId();
  }
}

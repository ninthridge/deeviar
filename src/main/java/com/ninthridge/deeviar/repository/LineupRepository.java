package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.Lineup;

@Repository("lineupRepository")
public class LineupRepository extends CachedJsonRepository<Lineup<GrabberStation>> {

  public LineupRepository() {
    super(new TypeReference<Lineup<GrabberStation>>(){});
  }
  
  @Override
  protected String getFileName() {
    return "lineup.json";
  }
}

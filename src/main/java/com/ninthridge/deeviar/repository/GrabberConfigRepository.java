package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.GrabberConfig;

@Repository("grabberConfigRepository")
public class GrabberConfigRepository extends CachedJsonRepository<GrabberConfig> {

  public GrabberConfigRepository() {
    super(new TypeReference<GrabberConfig>(){});
  }
  
  @Override
  protected String getFileName() {
    return "grabber.json";
  }
}

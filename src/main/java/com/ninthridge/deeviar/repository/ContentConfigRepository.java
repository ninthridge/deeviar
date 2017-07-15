package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.ContentConfig;

@Repository("contentConfigRepository")
public class ContentConfigRepository extends CachedJsonRepository<ContentConfig> {

  public ContentConfigRepository() {
    super(new TypeReference<ContentConfig>(){});
  }
  
  @Override
  protected String getFileName() {
    return "content.json";
  }
}

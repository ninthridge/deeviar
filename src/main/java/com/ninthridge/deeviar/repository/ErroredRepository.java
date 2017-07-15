package com.ninthridge.deeviar.repository;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;

@Repository("erroredRepository")
public class ErroredRepository extends CachedOwnerJsonRepository<Map<String, Date>> {

  public ErroredRepository() {
    super(new TypeReference<Map<String, Date>>(){});
  }
  
  @Override
  protected String getFileName() {
    return "errored.json";
  }
}

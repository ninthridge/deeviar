package com.ninthridge.deeviar.repository;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;

@Repository("deletedRepository")
public class DeletedRepository extends CachedOwnerJsonRepository<Map<String, Date>> {

  public DeletedRepository() {
    super(new TypeReference<Map<String, Date>>(){});
  }
  
  @Override
  protected String getFileName() {
    return "deleted.json";
  }
}

package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Profile;

@Repository("profileRepository")
public class ProfileRepository extends CachedOwnerJsonRepository<Profile> {

  public ProfileRepository() {
    super(new TypeReference<Profile>(){});
  }
  
  @Override
  protected String getFileName() {
    return "profile.json";
  }
}

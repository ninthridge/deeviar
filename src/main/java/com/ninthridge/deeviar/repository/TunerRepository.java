package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.ninthridge.deeviar.model.Tuner;

@Repository("tunerRepository")
public class TunerRepository extends CachedMultiRepository<Tuner> {

  @Override
  protected String getId(Tuner content) {
    return content.getId().toString();
  }
}

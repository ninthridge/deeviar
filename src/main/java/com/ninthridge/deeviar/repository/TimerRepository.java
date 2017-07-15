package com.ninthridge.deeviar.repository;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Timer;

@Repository("timerRepository")
public class TimerRepository extends CachedOwnerJsonRepository<Set<Timer>> {

  public TimerRepository() {
    super(new TypeReference<Set<Timer>>(){});
  }
  
  @Override
  protected String getFileName() {
    return "timers.json";
  }
}

package com.ninthridge.deeviar.repository;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.ninthridge.deeviar.model.TimerOccurrence;

@Repository("timerOccurrenceRepository")
public class TimerOccurrenceRepository extends CachedOwnerRepository<Set<TimerOccurrence>> {
 
}

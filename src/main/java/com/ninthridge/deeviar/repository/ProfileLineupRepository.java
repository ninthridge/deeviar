package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.ninthridge.deeviar.model.Lineup;
import com.ninthridge.deeviar.model.ProfileStation;

@Repository("profileLineupRepository")
public class ProfileLineupRepository extends CachedOwnerRepository<Lineup<ProfileStation>> {

}

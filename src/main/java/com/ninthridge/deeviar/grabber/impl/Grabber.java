package com.ninthridge.deeviar.grabber.impl;

import java.util.Map;
import java.util.Set;

import com.ninthridge.deeviar.grabber.GrabberException;
import com.ninthridge.deeviar.model.GrabberConfig;
import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.Lineup;

public interface Grabber {
  Lineup<GrabberStation> grabLineup(GrabberConfig grabberConfig, Set<String> lineupIds) throws GrabberException;
  Set<Map<String, String>> getLineups(GrabberConfig grabberConfig, String countryCode, String postalCode) throws GrabberException;
}

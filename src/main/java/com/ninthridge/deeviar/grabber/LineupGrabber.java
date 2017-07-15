package com.ninthridge.deeviar.grabber;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.grabber.impl.Grabber;
import com.ninthridge.deeviar.model.GrabberConfig;
import com.ninthridge.deeviar.model.GrabberStation;
import com.ninthridge.deeviar.model.Lineup;
import com.ninthridge.deeviar.model.GrabberConfig.LineupProvider;
import com.ninthridge.deeviar.service.GrabberConfigService;

@Service("lineupGrabber")
public class LineupGrabber {

  @Autowired
  private GrabberConfigService grabberConfigService;

  @Autowired
  private Grabber schedulesDirectGrabber;

  public boolean isConfigured() {
    GrabberConfig grabberConfig = grabberConfigService.getGrabberConfig();
    return grabberConfig.getProvider() != null && grabberConfig.getUsername() != null
        && !"".equals(grabberConfig.getUsername().trim()) && grabberConfig.getPassword() != null
        && !"".equals(grabberConfig.getPassword().trim());
  }

  public Lineup<GrabberStation> grabLineup(Set<String> lineupIds) throws GrabberException {
    if (isConfigured()) {
      GrabberConfig grabberConfig = grabberConfigService.getGrabberConfig();
      if (LineupProvider.SchedulesDirect.equals(grabberConfig.getProvider())) {
        return schedulesDirectGrabber.grabLineup(grabberConfig, lineupIds);
      }
    }
    return null;
  }

  public Set<Map<String, String>> getLineups(String countryCode, String postalCode) throws GrabberException {
    if (isConfigured()) {
      GrabberConfig grabberConfig = grabberConfigService.getGrabberConfig();
      if (LineupProvider.SchedulesDirect.equals(grabberConfig.getProvider())) {
        return schedulesDirectGrabber.getLineups(grabberConfig, countryCode, postalCode);
      }
    }
    return null;
  }
}

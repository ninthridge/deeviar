package com.ninthridge.deeviar.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutor;
import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutorFactory;
import com.ninthridge.deeviar.util.Pinger;

@Controller
@RequestMapping("/api/health")
public class HealthResource {

  protected final Log log = LogFactory.getLog(getClass());
  
  @Autowired
  private OperatingSystemCmdExecutorFactory operatingSystemCmdExecutorFactory;
  
  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<?> get() {
    try {
      OperatingSystemCmdExecutor executor = operatingSystemCmdExecutorFactory.getExecutor();
      Map<String, Object> map = new HashMap<>();
      
      Set<String> missingRequireddependencies = new HashSet<String>();
      if(!executor.programExists("hdhomerun_config")) {
        missingRequireddependencies.add("hdhomerun_config");
      }
      if(!executor.programExists("ffmpeg")) {
        missingRequireddependencies.add("ffmpeg");
      }
      if(!executor.programExists("ffprobe")) {
        missingRequireddependencies.add("ffprobe");
      }
      map.put("missingRequiredDependencies", missingRequireddependencies);
      
      Set<String> missingRecommendedDependencies = new HashSet<String>();
      if(!executor.programExists("biftool")) {
        missingRecommendedDependencies.add("biftool");
      }
      map.put("missingRecommendedDependencies", missingRecommendedDependencies);
      
      Set<String> incompatibleDependencies = new HashSet<String>();
      map.put("incompatibleDependencies", incompatibleDependencies);
      
      map.put("connectivity", Pinger.ping("yahoo.com") || Pinger.ping("facebook.com") || Pinger.ping("google.com"));
      map.put("tmdbConnectivity", Pinger.ping("tmdb.org"));
      map.put("schedulesDirectConnectivity", Pinger.ping("schedulesdirect.org"));
      
      return new ResponseEntity<>(map, HttpStatus.OK);
    } catch (IOException e) {
      log.error(e, e);
      return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

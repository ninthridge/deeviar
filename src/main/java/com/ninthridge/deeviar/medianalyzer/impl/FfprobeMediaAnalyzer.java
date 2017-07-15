package com.ninthridge.deeviar.medianalyzer.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.medianalyzer.MediaAnalyzer;
import com.ninthridge.deeviar.medianalyzer.model.MediaInfo;
import com.ninthridge.deeviar.util.CmdUtil;

@Service("mediaAnalyzer")
public class FfprobeMediaAnalyzer implements MediaAnalyzer {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  public MediaInfo analyze(String canonicalPath) throws IOException, InterruptedException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("input", canonicalPath);

    Process p = CmdUtil.execute(config.getFfprobeCommand(), params, null, null);
    p.waitFor();
    
    String str = "";
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        str += line;
      }
    }
    
    if(str.trim().length() > 0) {
      log.debug(str);
      MediaInfo mediaInfo = new ObjectMapper().readValue(str, MediaInfo.class);
      return mediaInfo;
    }
    else {
      log.error("ffprobe returned no result for " + canonicalPath);
      return null;
    }
  }
}

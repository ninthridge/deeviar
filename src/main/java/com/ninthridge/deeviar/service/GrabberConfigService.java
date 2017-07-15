package com.ninthridge.deeviar.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.model.GrabberConfig;
import com.ninthridge.deeviar.repository.GrabberConfigRepository;

@Service("grabberConfigService")
public class GrabberConfigService {

  protected final Log log = LogFactory.getLog(getClass());;

  @Autowired
  private GrabberConfigRepository grabberConfigRepository;

  public GrabberConfig getGrabberConfig() {
    GrabberConfig grabberConfig = grabberConfigRepository.get();
    if(grabberConfig == null) {
      grabberConfig = new GrabberConfig();
      saveGrabberConfig(grabberConfig);
    }
    return grabberConfig;
  }

  public void saveGrabberConfig(GrabberConfig grabberConfig) {   
    grabberConfigRepository.save(grabberConfig);      
  }
}
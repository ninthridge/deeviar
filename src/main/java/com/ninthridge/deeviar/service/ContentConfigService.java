package com.ninthridge.deeviar.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.model.ContentConfig;
import com.ninthridge.deeviar.repository.ContentConfigRepository;

@Service("contentConfigService")
public class ContentConfigService {

  protected final Log log = LogFactory.getLog(getClass());;

  @Autowired
  private ContentConfigRepository contentConfigService;

  public ContentConfig getContentConfig() {
    ContentConfig contentConfig = contentConfigService.get();
    if(contentConfig == null) {
      contentConfig = new ContentConfig();
      saveContentConfig(contentConfig);
    }
    return contentConfig;
  }

  public void saveContentConfig(ContentConfig contentConfig) {   
    contentConfigService.save(contentConfig);      
  }
}
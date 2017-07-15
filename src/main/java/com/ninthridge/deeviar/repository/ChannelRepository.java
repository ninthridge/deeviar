package com.ninthridge.deeviar.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Channel;

@Repository("channelRepository")
public class ChannelRepository extends CachedJsonRepository<List<Channel>> {

  public ChannelRepository() {
    super(new TypeReference<List<Channel>>(){});
  }
  
  @Override
  protected String getFileName() {
    return "channels.json";
  }
}

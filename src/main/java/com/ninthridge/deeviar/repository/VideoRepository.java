package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Video;

@Repository("videoRepository")
public class VideoRepository extends CachedOwnerMultiJsonRepository<Video> {

  public VideoRepository() {
    super(new TypeReference<Video>(){});
  }

  @Override
  protected String getDirName() {
    return "videos";
  }

  @Override
  protected String getId(Video videos) {
    return videos.getId();
  }
}

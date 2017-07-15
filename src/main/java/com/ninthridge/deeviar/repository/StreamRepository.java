package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.ninthridge.deeviar.model.Stream;

@Repository("streamRepository")
public class StreamRepository extends CachedMultiRepository<Stream> {

  @Override
  protected String getId(Stream content) {
    return content.getId();
  }
}

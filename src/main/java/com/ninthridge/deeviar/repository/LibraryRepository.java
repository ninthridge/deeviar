package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Library;

@Repository("libraryRepository")
public class LibraryRepository extends CachedOwnerJsonRepository<Library> {

  public LibraryRepository() {
    super(new TypeReference<Library>(){});
  }
  
  @Override
  protected String getFileName() {
    return "library.json";
  }
}

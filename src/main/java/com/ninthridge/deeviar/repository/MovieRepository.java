package com.ninthridge.deeviar.repository;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ninthridge.deeviar.model.Movie;

@Repository("movieRepository")
public class MovieRepository extends CachedOwnerMultiJsonRepository<Movie> {

  public MovieRepository() {
    super(new TypeReference<Movie>(){});
  }

  @Override
  protected String getDirName() {
    return "movies";
  }

  @Override
  protected String getId(Movie movie) {
    return movie.getId();
  }

}

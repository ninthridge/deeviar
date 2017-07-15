package com.ninthridge.deeviar.model;

import java.io.Serializable;

import com.ninthridge.deeviar.model.id.MovieId;

public class Movie extends Video implements Serializable {

  private static final long serialVersionUID = 1L;

  public Movie() {

  }

  public Movie(MovieId movieId) {
    setId(movieId.getId());
    setTitle(movieId.getTitle());
    setYear(movieId.getYear());
  }

  @Override
  public ContentType getType() {
    return ContentType.Movie;
  }
}
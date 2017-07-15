package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.pojomatic.Pojomatic;

public class Library implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date timestamp;

  private Set<Series> series = new TreeSet<Series>();
  private Set<Movie> movies = new TreeSet<Movie>();
  private Set<Video> videos = new TreeSet<Video>();
  
  private Map<String, Date> deleted = new HashMap<>();
  private Map<String, Date> errored = new HashMap<>();
  
  public Set<Series> getSeries() {
    return series;
  }

  public void setSeries(Set<Series> series) {
    this.series.clear();
    this.series.addAll(series);
  }

  public Set<Movie> getMovies() {
    return movies;
  }

  public void setMovies(Set<Movie> movies) {
    this.movies.clear();
    this.movies.addAll(movies);
  }

  public Set<Video> getVideos() {
    return videos;
  }

  public void setVideos(Set<Video> videos) {
    this.videos.clear();
    this.videos.addAll(videos);
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public Map<String, Date> getDeleted() {
    return deleted;
  }

  public void setDeleted(Map<String, Date> deleted) {
    this.deleted = deleted;
  }

  public Map<String, Date> getErrored() {
    return errored;
  }

  public void setErrored(Map<String, Date> errored) {
    this.errored = errored;
  }

  @Override
  public boolean equals(Object o) {
    return Pojomatic.equals(this, o);
  }

  @Override
  public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override
  public String toString() {
    return Pojomatic.toString(this);
  }
}

package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import com.ninthridge.deeviar.model.id.SeriesId;

public class Series extends VideoContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private Set<Episode> episodes = new TreeSet<Episode>();

  private Integer year;

  public Series() {

  }

  public Series(SeriesId seriesId) {
    setId(seriesId.getId());
    setTitle(seriesId.getTitle());
    setYear(seriesId.getYear());
  }

  @Override
  public ContentType getType() {
    return ContentType.Series;
  }

  public Set<Episode> getEpisodes() {
    return episodes;
  }

  public void setEpisodes(Set<Episode> episodes) {
    this.episodes.clear();
    this.episodes.addAll(episodes);
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }
}
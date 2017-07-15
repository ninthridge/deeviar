package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;

public abstract class VideoContent extends Content implements Serializable, Comparable<VideoContent> {

  private static final long serialVersionUID = 1L;

  private String imdbId;
  private Integer year;
  private String rated;
  private String category;
  private Date released;
  
  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public String getRated() {
    return rated;
  }

  public void setRated(String rated) {
    this.rated = rated;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Date getReleased() {
    return released;
  }

  public void setReleased(Date released) {
    this.released = released;
  }

  @Override
  public int compareTo(VideoContent o) {
    if(!getTitle().equals(o.getTitle())) {
      return getTitle().compareTo(o.getTitle());
    }
    else {
      if(getYear() != null) {
        return getYear().compareTo(o.getYear());
      }
      else if(o.getYear() != null) {
        return 1;
      }
      else {
        return 0;
      }
    }
  }
}

package com.ninthridge.deeviar.model.id;

public class SeriesId extends MediaId {

  private String title;
  private Integer year;

  public SeriesId(String title, Integer year) {
    this.title = title;
    this.year = year;
    setId("S" + Math.abs(title.hashCode()) + (year != null ? year : ""));
  }

  public String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }

  public Integer getYear() {
    return year;
  }

  protected void setYear(Integer year) {
    this.year = year;
  }
}
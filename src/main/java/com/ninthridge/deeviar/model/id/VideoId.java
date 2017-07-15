package com.ninthridge.deeviar.model.id;

public class VideoId extends MediaId {

  private String title;
  
  public VideoId(String title) {
    this.title = title;
    setId("V" + Math.abs(title.hashCode()));
  }

  public String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }
}
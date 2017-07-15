package com.ninthridge.deeviar.model;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class BifProcessingItem {
  @Property
  private String profileTitle;
  @Property
  private Video video;
  private int priority;

  public BifProcessingItem() {
  }

  public BifProcessingItem(String profileTitle, Video video, int priority) {
    this.profileTitle = profileTitle;
    this.video = video;
    this.priority = priority;
  }

  public String getProfileTitle() {
    return profileTitle;
  }

  public void setProfileTitle(String profileTitle) {
    this.profileTitle = profileTitle;
  }

  public Video getVideo() {
    return video;
  }

  public void setVideo(Video video) {
    this.video = video;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
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

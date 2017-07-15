package com.ninthridge.deeviar.model;

import java.util.Date;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class Timer {

  private String id;
  
  @Property
  private String profile;
  
  @Property
  private String stationId;
  
  @Property
  private String title;
  
  @Property
  private Date date;
  
  @Property
  private Time startTime;
  
  @Property
  private List<Integer> daysOfWeek;
  
  private Long duration;
  
  private String callSign;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getProfile() {
    return profile;
  }
  public void setProfile(String profile) {
    this.profile = profile;
  }
  public String getStationId() {
    return stationId;
  }
  public void setStationId(String stationId) {
    this.stationId = stationId;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public Time getStartTime() {
    return startTime;
  }
  public void setStartTime(Time startTime) {
    this.startTime = startTime;
  }
  public Long getDuration() {
    return duration;
  }
  public void setDuration(Long duration) {
    this.duration = duration;
  }
  public List<Integer> getDaysOfWeek() {
    return daysOfWeek;
  }
  public void setDaysOfWeek(List<Integer> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
  }
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  
  public String getCallSign() {
    return callSign;
  }
  public void setCallSign(String callSign) {
    this.callSign = callSign;
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

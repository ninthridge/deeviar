package com.ninthridge.deeviar.model;

import java.util.Date;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TimerOccurrence {
  
  private String id;
  private String timerId;
  @Property
  private String stationId;
  @Property
  private Date startDate;
  private long duration;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getTimerId() {
    return timerId;
  }
  public void setTimerId(String timerId) {
    this.timerId = timerId;
  }
  public String getStationId() {
    return stationId;
  }
  public void setStationId(String stationId) {
    this.stationId = stationId;
  }
  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  public long getDuration() {
    return duration;
  }
  public void setDuration(long duration) {
    this.duration = duration;
  }
  
  @JsonIgnore
  public Date getEndDate() {
    return new Date(startDate.getTime() + (duration*1000L));
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

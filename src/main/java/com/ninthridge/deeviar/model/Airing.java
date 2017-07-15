package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Airing extends Content implements Serializable, Comparable<Airing> {

  private static final long serialVersionUID = 1L;

  public enum AiringType {Movie, Episode, Show, Sports};
  
  @Property
  private Date start;
  private long duration;
  private Boolean newAiring;
  private Date originalAirDate;
  @Property
  private String stationId;
  
  private AiringType airingType;
  private String episodeTitle;
  private Integer season;
  private Integer episode;
  
  private Set<String> timerIds = new HashSet<>();
  
  @Override
  public ContentType getType() {
    return ContentType.Airing;
  }
  
  public Boolean getNewAiring() {
    return newAiring;
  }
  public void setNewAiring(Boolean newAiring) {
    this.newAiring = newAiring;
  }
  public Date getStart() {
    return start;
  }
  public void setStart(Date start) {
    this.start = start;
  }
  public long getDuration() {
    return duration;
  }
  public void setDuration(long duration) {
    this.duration = duration;
  }
  @JsonIgnore
  public Date getEnd() {
    return new Date(start.getTime() + (duration*1000L));
  }
  
  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public Date getOriginalAirDate() {
    return originalAirDate;
  }
  public void setOriginalAirDate(Date originalAirDate) {
    this.originalAirDate = originalAirDate;
  }
  
  public String getEpisodeTitle() {
    return episodeTitle;
  }
  public void setEpisodeTitle(String episodeTitle) {
    this.episodeTitle = episodeTitle;
  }
  public Integer getSeason() {
    return season;
  }
  public void setSeason(Integer season) {
    this.season = season;
  }
  public Integer getEpisode() {
    return episode;
  }
  public void setEpisode(Integer episode) {
    this.episode = episode;
  }
  
  
  public AiringType getAiringType() {
    return airingType;
  }
  public void setAiringType(AiringType airingType) {
    this.airingType = airingType;
  }
  
  public Set<String> getTimerIds() {
    return timerIds;
  }

  public void setTimerIds(Set<String> timerIds) {
    this.timerIds = timerIds;
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
  @Override
  public int compareTo(Airing o) {
    if(!getStationId().equals(o.getStationId())) {
      return getStationId().compareTo(o.getStationId());
    }
    else {
      return start.compareTo(o.getStart());
    }
  }

}
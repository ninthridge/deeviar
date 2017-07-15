package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Lineup <T extends Station> implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date timestamp;
  
  private Set<String> lineupIds;
  
  private Map<String, StationAirings<T>> stationAiringMap = new LinkedHashMap<>();

  public Map<String, StationAirings<T>> getStationAiringMap() {
    return stationAiringMap;
  }

  public void setStationAiringMap(Map<String, StationAirings<T>> stationAiringMap) {
    this.stationAiringMap = stationAiringMap;
  }

  @JsonIgnore
  public void addStation(T station) {
    StationAirings<T> stationAirings = stationAiringMap.get(station.getId());
    if(stationAirings == null) {
      stationAirings = new StationAirings<T>();
      stationAiringMap.put(station.getId(), stationAirings);
    }
    stationAirings.getStations().add(station);
  }

  @JsonIgnore
  public void addAiring(Airing airing) {
    StationAirings<T> stationAirings = stationAiringMap.get(airing.getStationId());
    if(stationAirings != null) {
      stationAirings.getAirings().add(airing);
    }
  }

  @JsonIgnore
  public List<T> getStations() {
    Set<T> stations = new TreeSet<T>();
    for(StationAirings<T> stationAirings : stationAiringMap.values()) {
      stations.addAll(stationAirings.getStations());
    }
    return new ArrayList<T>(stations);
  }

  @JsonIgnore
  public List<T> getStations(String stationId) {
    Set<T> stations = new TreeSet<T>();
    StationAirings<T> stationAirings = stationAiringMap.get(stationId);
    if(stationAirings != null) {
      stations.addAll(stationAirings.getStations());
    }
    return new ArrayList<T>(stations);
  }

  @JsonIgnore
  public List<Airing> getAirings() {
    List<Airing> airings = new ArrayList<Airing>();
    for(StationAirings<T> stationAirings : stationAiringMap.values()) {
      airings.addAll(stationAirings.getAirings());
    }
    return airings;
  }

  @JsonIgnore
  public List<Airing> getAirings(String stationId) {
    if(stationId == null) {
      return getAirings();
    }
    StationAirings<T> stationAirings = stationAiringMap.get(stationId);
    if(stationAirings != null) {
      return new ArrayList<Airing>(stationAirings.getAirings());
    }
    return new ArrayList<Airing>();
  }

  @JsonIgnore
  public Airing getAiring(String airingId) {
    for(Airing airing : getAirings()) {
      if(airing.getId().equals(airingId)) {
        return airing;
      }
    }
    return null;
  }
  
  @JsonIgnore
  public boolean containsFutureAirings() {
    Date date = new Date();
    for(Airing airing : getAirings()) {
      if(date.before(airing.getEnd())) {
        return true;
      }
    }
    return false;
  }
  
  public Set<String> getLineupIds() {
    return lineupIds;
  }

  public void setLineupIds(Set<String> lineupIds) {
    this.lineupIds = lineupIds;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}

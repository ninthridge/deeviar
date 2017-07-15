package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class StationAirings <T extends Station> implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private Set<T> stations = new HashSet<>();
  private Set<Airing> airings = new HashSet<>();

  public StationAirings() {
    
  }
  
  public Set<T> getStations() {
    return stations;
  }

  public void setStations(Set<T> stations) {
    this.stations = stations;
  }

  public Set<Airing> getAirings() {
    return airings;
  }

  public void setAirings(Set<Airing> airings) {
    this.airings = airings;
  }

  
}

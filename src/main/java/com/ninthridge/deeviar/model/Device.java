package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Device implements Serializable, Comparable<Device> {

  private static final long serialVersionUID = 1L;

  public enum DeviceStatus {Available, Scanning, WaitingToScan, New, Offline}
  
  @Property
  private String id;
  private String ipAddress;
  private String hwmodel;
  private Set<Tuner> tuners;
  private Set<DeviceStation> stations;
  private Boolean virtualChannelSupport;
  private String lineupId;
  private Date scanTimestamp;
  private DeviceStatus status;
  private boolean discovered;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getIpAddress() {
    return ipAddress;
  }
  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }
  public String getHwmodel() {
    return hwmodel;
  }
  public void setHwmodel(String hwmodel) {
    this.hwmodel = hwmodel;
  }
  @JsonIgnore
  public Set<Tuner> getTuners() {
    return tuners;
  }
  public void setTuners(Set<Tuner> tuners) {
    this.tuners = tuners;
  }
  
  public Set<DeviceStation> getStations() {
    return stations;
  }
  public void setStations(Set<DeviceStation> stations) {
    this.stations = stations;
  }
  public Boolean getVirtualChannelSupport() {
    return virtualChannelSupport;
  }
  public void setVirtualChannelSupport(Boolean virtualChannelSupport) {
    this.virtualChannelSupport = virtualChannelSupport;
  }
  public String getLineupId() {
    return lineupId;
  }
  public void setLineupId(String lineupId) {
    this.lineupId = lineupId;
  }
  public DeviceStatus getStatus() {
    return status;
  }
  public void setStatus(DeviceStatus status) {
    this.status = status;
  }
  
  public Date getScanTimestamp() {
    return scanTimestamp;
  }
  public void setScanTimestamp(Date scanTimestamp) {
    this.scanTimestamp = scanTimestamp;
  }
  
  public boolean getDiscovered() {
    return discovered;
  }
  public void setDiscovered(boolean discovered) {
    this.discovered = discovered;
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
  public int compareTo(Device device) {
    return id.compareTo(device.getId());
  }
}

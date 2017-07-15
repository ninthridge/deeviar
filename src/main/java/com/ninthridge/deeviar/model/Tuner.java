package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class Tuner implements Serializable, Comparable<Tuner> {

  private static final long serialVersionUID = 1L;

  @Property
  private Device device;

  @Property
  private Integer id;

  private Integer port;
  
  private String targetUrl;

  private Date lastTuned;

  private Date lastUntuned;

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTargetUrl() {
    return targetUrl;
  }

  public void setTargetUrl(String targetUrl) {
    this.targetUrl = targetUrl;
  }

  public Date getLastTuned() {
    return lastTuned;
  }

  public void setLastTuned(Date lastTuned) {
    this.lastTuned = lastTuned;
  }

  public Date getLastUntuned() {
    return lastUntuned;
  }

  public void setLastUntuned(Date lastUntuned) {
    this.lastUntuned = lastUntuned;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
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
  public int compareTo(Tuner tuner) {
    return tuner.getPort() - port;
  }
  
  
}

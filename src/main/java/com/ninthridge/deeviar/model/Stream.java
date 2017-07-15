package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Stream implements Serializable, Comparable<Stream> {

  private static final long serialVersionUID = 1L;

  private String id;
  @Property
  private Date started;
  private Date stopped;
  private Map<String, Date> expires = new HashMap<>();
  private String streamFormat;
  private List<StreamContent> streams;
  
  @Property
  private String stationId;
  
  @JsonIgnore
  private Tuner tuner;

  @JsonIgnore
  private Process process;

  
  public String input() {
    return tuner.getTargetUrl();
  }

  public String getStreamFormat() {
    return streamFormat;
  }

  public void setStreamFormat(String streamFormat) {
    this.streamFormat = streamFormat;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public Process getProcess() {
    return process;
  }

  public void setProcess(Process process) {
    this.process = process;
  }

  public String getStationId() {
    return stationId;
  }


  public void setStationId(String stationId) {
    this.stationId = stationId;
  }


  public Tuner getTuner() {
    return tuner;
  }
  public void setTuner(Tuner tuner) {
    this.tuner = tuner;
  }

  public Map<String, Date> getExpires() {
    return expires;
  }
  public void setExpires(Map<String, Date> expires) {
    this.expires = expires;
  }

  public Date getStopped() {
    return stopped;
  }

  public void setStopped(Date stopped) {
    this.stopped = stopped;
  }

  public List<StreamContent> getStreams() {
    return streams;
  }

  public void setStreams(List<StreamContent> streams) {
    this.streams = streams;
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
  public int compareTo(Stream stream) {
    return started.compareTo(stream.getStarted());
  }
}

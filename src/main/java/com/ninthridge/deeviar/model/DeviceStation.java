package com.ninthridge.deeviar.model;

import java.io.Serializable;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class DeviceStation implements Station, Serializable {

  private static final long serialVersionUID = 1L;
  
  @Property
  private String deviceId;
  @Property
  private String channel;
  @Property
  private String callSign;
  private Integer program;
  private String modulation;
  private Long frequency;
  
  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  @Override
  public String getId() {
    return channel;
  }
  
  @Override
  public void setId(String id) {
    
  }
  
  @Override
  public String getChannel() {
    return channel;
  }

  @Override
  public void setChannel(String channel) {
    this.channel = channel;
  }

  @Override
  public String getCallSign() {
    return callSign;
  }

  @Override
  public void setCallSign(String callSign) {
    this.callSign = callSign;
  }

  public Integer getProgram() {
    return program;
  }

  public void setProgram(Integer program) {
    this.program = program;
  }

  public String getModulation() {
    return modulation;
  }

  public void setModulation(String modulation) {
    this.modulation = modulation;
  }

  public Long getFrequency() {
    return frequency;
  }

  public void setFrequency(Long frequency) {
    this.frequency = frequency;
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

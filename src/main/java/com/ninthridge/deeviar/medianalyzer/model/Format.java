package com.ninthridge.deeviar.medianalyzer.model;

import java.io.Serializable;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@AutoProperty
@JsonIgnoreProperties(ignoreUnknown = true)
public class Format implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("filename")
  private String fileName;

  @JsonProperty("format_name")
  private String formatName;

  @JsonProperty("duration")
  private String duration;

  @JsonProperty("bit_rate")
  private String bitRate;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFormatName() {
    return formatName;
  }

  public void setFormatName(String formatName) {
    this.formatName = formatName;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getBitRate() {
    return bitRate;
  }

  public void setBitRate(String bitRate) {
    this.bitRate = bitRate;
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

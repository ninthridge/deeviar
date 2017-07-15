package com.ninthridge.deeviar.medianalyzer.model;

import java.io.Serializable;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@AutoProperty
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("format")
  private Format format;

  @JsonProperty("streams")
  private List<Stream> streams;

  public Format getFormat() {
    return format;
  }
  public void setFormat(Format format) {
    this.format = format;
  }
  public List<Stream> getStreams() {
    return streams;
  }
  public void setStreams(List<Stream> streams) {
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
}

package com.ninthridge.deeviar.library.scanner.impl;

import java.io.Serializable;
import java.util.Date;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class ScannedFile implements Serializable {

  private static final long serialVersionUID = 1L;
  
  @Property
  private String uri;
  
  private Date timestamp;
  
  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  public Date getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
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

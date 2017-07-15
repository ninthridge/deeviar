package com.ninthridge.deeviar.model.id;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class MediaId {
  
  @Property
  private String id;
  
  public String getId() {
    return id;
  }

  protected void setId(String id) {
    this.id = id;
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
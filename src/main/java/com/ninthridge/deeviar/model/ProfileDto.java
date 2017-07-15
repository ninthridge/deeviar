package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class ProfileDto implements Serializable {

  private static final long serialVersionUID = 1L;

  @Property
  private String title;
  private String hdPosterUri;
  private String sdPosterUri;
  private Set<ProfilePermission> permissions;
  private Boolean restricted;
  
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }

  public Set<ProfilePermission> getPermissions() {
    return permissions;
  }
  public void setPermissions(Set<ProfilePermission> permissions) {
    this.permissions = permissions;
  }

  public String getHdPosterUri() {
    return hdPosterUri;
  }
  public void setHdPosterUri(String hdPosterUri) {
    this.hdPosterUri = hdPosterUri;
  }
  
  public String getSdPosterUri() {
    return sdPosterUri;
  }
  public void setSdPosterUri(String sdPosterUri) {
    this.sdPosterUri = sdPosterUri;
  }
  
  public Boolean getRestricted() {
    return restricted;
  }
  public void setRestricted(Boolean restricted) {
    this.restricted = restricted;
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
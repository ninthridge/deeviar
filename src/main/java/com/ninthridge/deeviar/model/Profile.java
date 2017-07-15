package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class Profile implements Serializable, Comparable<Profile> {

  private static final long serialVersionUID = 1L;

  @Property
  private String title;
  private String hdPosterUri;
  private String sdPosterUri;
  private Set<ProfilePermission> permissions;
  private String pin;
  private Map<String, Set<String>> libraryImportLocations;
  private String token;
  private Boolean enabled;
  
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
  public String getPin() {
    return pin;
  }
  public void setPin(String pin) {
    this.pin = pin;
  }
  
  public Map<String, Set<String>> getLibraryImportLocations() {
    return libraryImportLocations;
  }
  public void setLibraryImportLocations(Map<String, Set<String>> libraryImportLocations) {
    this.libraryImportLocations = libraryImportLocations;
  }
  
  public String getToken() {
    return token;
  }
  public void setToken(String token) {
    this.token = token;
  }
  
  public Boolean getEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
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
  public int compareTo(Profile profile) {
    return getTitle().compareTo(profile.getTitle());
  }

  
}
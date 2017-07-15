package com.ninthridge.deeviar.model;

import java.io.Serializable;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Content implements Serializable {

  private static final long serialVersionUID = 1L;

  public static enum ContentType {Station, Airing, Movie, Series, Episode, Video};

  @Property
  private String id;
  private String title;
  private String hdPosterUri;
  private String sdPosterUri;
  private String description;
  private Boolean favorite = false;
  private Boolean active = true;
  private String shortDescription;
  private String externalPosterUrl;

  public abstract ContentType getType();

  protected void setType(ContentType type) {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getFavorite() {
    return favorite;
  }

  public void setFavorite(Boolean favorite) {
    this.favorite = favorite;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }

  @JsonIgnore
  public String getExternalPosterUrl() {
    return externalPosterUrl;
  }

  public void setExternalPosterUrl(String externalPosterUrl) {
    this.externalPosterUrl = externalPosterUrl;
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

package com.ninthridge.deeviar.model;

import java.io.Serializable;

public class StreamContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private String uri;
  private Integer bitRate;
  private Boolean quality;
  private Integer width;
  private Integer height;
  
  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  public Integer getBitRate() {
    return bitRate;
  }
  public void setBitRate(Integer bitRate) {
    this.bitRate = bitRate;
  }
  public Boolean getQuality() {
    return quality;
  }
  public void setQuality(Boolean quality) {
    this.quality = quality;
  }
  public Integer getWidth() {
    return width;
  }
  public void setWidth(Integer width) {
    this.width = width;
  }
  public Integer getHeight() {
    return height;
  }
  public void setHeight(Integer height) {
    this.height = height;
  }
}

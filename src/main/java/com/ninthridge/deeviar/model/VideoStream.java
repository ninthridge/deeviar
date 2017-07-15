package com.ninthridge.deeviar.model;

import java.io.Serializable;

public class VideoStream implements Serializable {

  private static final long serialVersionUID = 1L;

  private String uri;
  private Integer bitRate;
  private String streamFormat;
  private Integer length;
  private Integer height;
  private Integer width;
  
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
  public String getStreamFormat() {
    return streamFormat;
  }
  public void setStreamFormat(String streamFormat) {
    this.streamFormat = streamFormat;
  }
  public Integer getLength() {
    return length;
  }
  public void setLength(Integer length) {
    this.length = length;
  }
  public Integer getHeight() {
    return height;
  }
  public void setHeight(Integer height) {
    this.height = height;
  }
  public Integer getWidth() {
    return width;
  }
  public void setWidth(Integer width) {
    this.width = width;
  }


}

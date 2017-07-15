package com.ninthridge.deeviar.medianalyzer.model;

import java.io.Serializable;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@AutoProperty
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stream implements Serializable {

  private static final long serialVersionUID = 1L;

  private int index;

  @JsonProperty("codec_name")
  private String codecName;

  // video, audio, subtitle
  @JsonProperty("codec_type")
  private String codecType;

  @JsonProperty("codec_tag_string")
  private String codecTagString;

  @JsonProperty("width")
  private int width;

  @JsonProperty("height")
  private int height;

  @JsonProperty("channels")
  private int channels;

  @JsonProperty("bit_rate")
  private String bitRate;

  @JsonProperty("duration")
  private String duration;

  @JsonProperty("channel_layout")
  private String channelLayout;

  @JsonProperty("sample_fmt")
  private String sampleFormat;

  @JsonProperty("sample_rate")
  private int sampleRate;

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String getCodecName() {
    return codecName;
  }

  public void setCodecName(String codecName) {
    this.codecName = codecName;
  }

  public String getCodecType() {
    return codecType;
  }

  public void setCodecType(String codecType) {
    this.codecType = codecType;
  }

  public String getCodecTagString() {
    return codecTagString;
  }

  public void setCodecTagString(String codecTagString) {
    this.codecTagString = codecTagString;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getChannels() {
    return channels;
  }

  public void setChannels(int channels) {
    this.channels = channels;
  }

  public String getBitRate() {
    return bitRate;
  }

  public void setBitRate(String bitRate) {
    this.bitRate = bitRate;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getChannelLayout() {
    return channelLayout;
  }

  public void setChannelLayout(String channelLayout) {
    this.channelLayout = channelLayout;
  }

  public String getSampleFormat() {
    return sampleFormat;
  }

  public void setSampleFormat(String sampleFormat) {
    this.sampleFormat = sampleFormat;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
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

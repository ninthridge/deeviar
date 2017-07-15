package com.ninthridge.deeviar.model;

import java.io.Serializable;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GrabberStation implements Station, Serializable, Comparable<GrabberStation> {

  private static final long serialVersionUID = 1L;

  @Property
  private String id;
  @Property
  private String lineupId;
  private String callSign;
  private String channel;
  private String hdPosterUri;
  private String sdPosterUri;
  private String externalPosterUrl;
  private String externalPosterMd5;
  
  @Override
  public String getId() {
    return id;
  }
  @Override
  public void setId(String id) {
    this.id = id;
  }
  @Override
  public String getChannel() {
    return channel;
  }
  @Override
  public void setChannel(String channel) {
    this.channel = channel;
  }
  @Override
  public String getCallSign() {
    return callSign;
  }
  @Override
  public void setCallSign(String callSign) {
    this.callSign = callSign;
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
  @JsonIgnore
  public String getExternalPosterUrl() {
    return externalPosterUrl;
  }
  public void setExternalPosterUrl(String externalPosterUrl) {
    this.externalPosterUrl = externalPosterUrl;
  }
  @JsonIgnore
  public String getExternalPosterMd5() {
    return externalPosterMd5;
  }
  public void setExternalPosterMd5(String externalPosterMd5) {
    this.externalPosterMd5 = externalPosterMd5;
  }
  public String getLineupId() {
    return lineupId;
  }
  public void setLineupId(String lineupId) {
    this.lineupId = lineupId;
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
  public int compareTo(GrabberStation o) {
    if(getChannel() == null) {
      return -1;
    }
    else if(o == null || o.getChannel() == null) {
      return 1;
    }

    String channel1 = null;
    String subchannel1 = null;
    String channel2 = null;
    String subchannel2 = null;

    if(getChannel().contains(".")) {
      String s[] = getChannel().split("\\.");
      channel1 = s[0];
      if(s.length > 1) {
        subchannel1 = s[1];
      }
    }
    else if(getChannel().contains("-")) {
      String s[] = getChannel().split("-");
      channel1 = s[0];
      if(s.length > 1) {
        subchannel1 = s[1];
      }
    }
    else {
      channel1 = getChannel();
    }

    if(o.getChannel().contains(".")) {
      String s[] = o.getChannel().split("\\.");
      channel2 = s[0];
      if(s.length > 1) {
        subchannel2 = s[1];
      }
    }
    else if(o.getChannel().contains("-")) {
      String s[] = o.getChannel().split("-");
      channel2 = s[0];
      if(s.length > 1) {
        subchannel2 = s[1];
      }
    }
    else {
      channel2 = o.getChannel();
    }

    try {
      int diff = new Integer(channel1) - new Integer(channel2);
      if(diff == 0) {
        if(subchannel1 == null) {
          return -1;
        }
        else if(subchannel2 == null) {
          return 1;
        }
        else {
          return new Integer(subchannel1) - new Integer(subchannel2);
        }
      }
      else {
        return diff;
      }
    }
    catch(Exception e) {
      return getChannel().compareTo(o.getChannel());
    }
  }
}

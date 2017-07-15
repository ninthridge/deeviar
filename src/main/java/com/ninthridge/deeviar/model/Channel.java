package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Channel implements Serializable, Comparable<Channel> {

  private static final long serialVersionUID = 1L;

  @Property
  private String stationId;
  @Property
  private String lineupId;
  private String callSign;
  private String channel;
  private String lineupChannel;
  private Set<String> profileTitles;
  
  public String getChannel() {
    return channel;
  }
  public void setChannel(String channel) {
    this.channel = channel;
  }
  
  public Set<String> getProfileTitles() {
    return profileTitles;
  }
  public void setProfileTitles(Set<String> profileTitles) {
    this.profileTitles = profileTitles;
  }
  public String getStationId() {
    return stationId;
  }
  public void setStationId(String stationId) {
    this.stationId = stationId;
  }
  public String getLineupId() {
    return lineupId;
  }
  public void setLineupId(String lineupId) {
    this.lineupId = lineupId;
  }
  
  public String getCallSign() {
    return callSign;
  }
  public void setCallSign(String callSign) {
    this.callSign = callSign;
  }
  
  public String getLineupChannel() {
    return lineupChannel;
  }
  public void setLineupChannel(String lineupChannel) {
    this.lineupChannel = lineupChannel;
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
  public int compareTo(Channel o) {
    if((profileTitles == null || profileTitles.isEmpty()) && o.getProfileTitles() != null && !o.getProfileTitles().isEmpty()) {
      return 1;
    }
    else if(profileTitles != null && !profileTitles.isEmpty() && (o.getProfileTitles() == null || o.getProfileTitles().isEmpty())) {
      return -1;
    }
    else {
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
  
}

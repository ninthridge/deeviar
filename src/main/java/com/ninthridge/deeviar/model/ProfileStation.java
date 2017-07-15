package com.ninthridge.deeviar.model;

import java.io.Serializable;

import org.pojomatic.Pojomatic;

public class ProfileStation extends Content implements Station, Serializable, Comparable<ProfileStation> {

  private static final long serialVersionUID = 1L;

  //id = The schedules direct stationID.  References GrabberStation.stationID
  //channel = user displayed value.  Default is the same as the lineupChannel, but can be user defined
  
  private String callSign;
  private String channel;
  
  @Override
  public ContentType getType() {
    return ContentType.Station;
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

  public Boolean getLive() {
    return true;
  }
  protected void setLive(Boolean live) {

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
  public int compareTo(ProfileStation o) {
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

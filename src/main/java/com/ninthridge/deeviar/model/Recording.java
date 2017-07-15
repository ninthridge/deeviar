package com.ninthridge.deeviar.model;

import java.util.Date;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class Recording extends MediaProcessingItem {

  public static enum RecordingStatus {RECORDING, PROCESSING, COMPLETE};
  
  @Property
  private String stationId;
  
  @Property
  private Date startDate;
  
  private Date endDate;
  
  private Stream stream;
  
  private RecordingStatus recordingStatus;
  
  public Stream getStream() {
    return stream;
  }
  public void setStream(Stream stream) {
    this.stream = stream;
  }
  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  public Date getEndDate() {
    return endDate;
  }
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
  
  public String getStationId() {
    return stationId;
  }
  public void setStationId(String stationId) {
    this.stationId = stationId;
  }
  public RecordingStatus getRecordingStatus() {
    return recordingStatus;
  }
  public void setRecordingStatus(RecordingStatus recordingStatus) {
    this.recordingStatus = recordingStatus;
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

package com.ninthridge.deeviar.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ninthridge.deeviar.model.id.VideoId;

public class Video extends VideoContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<VideoStream> streams;
  private String hdBifUri;
  private String sdBifUri;
  private Integer bookmarkPosition;
  private Date bookmarkDate;
  private Boolean watched = false;
  private Integer height;
  private Integer width;
  private Set<SubtitleTrack> subtitleTracks = new HashSet<>();
  private Date addedToLibrary;
  private Date timestamp;
  
  public Video() {
    
  }
  
  public Video(VideoId videoId) {
    setId(videoId.getId());
    setTitle(videoId.getTitle());
  }
  
  public ContentType getType() {
    return ContentType.Video;
  }

  protected void setType(ContentType type) {

  }

  public List<VideoStream> getStreams() {
    return streams;
  }

  public void setStreams(List<VideoStream> streams) {
    this.streams = streams;
  }

  public String getHdBifUri() {
    return hdBifUri;
  }

  public void setHdBifUri(String hdBifUri) {
    this.hdBifUri = hdBifUri;
  }

  public String getSdBifUri() {
    return sdBifUri;
  }

  public void setSdBifUri(String sdBifUri) {
    this.sdBifUri = sdBifUri;
  }

  public Integer getBookmarkPosition() {
    return bookmarkPosition;
  }

  public void setBookmarkPosition(Integer bookmarkPosition) {
    this.bookmarkPosition = bookmarkPosition;
  }

  public Date getBookmarkDate() {
    return bookmarkDate;
  }

  public void setBookmarkDate(Date bookmarkDate) {
    this.bookmarkDate = bookmarkDate;
  }

  public Boolean getWatched() {
    return watched;
  }

  public void setWatched(Boolean watched) {
    this.watched = watched;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
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

  public Integer getLength() {
    if(streams != null && !streams.isEmpty()) {
      return streams.get(0).getLength();
    }
    return null;
  }

  protected void setLength(Integer length) {

  }

  public Set<SubtitleTrack> getSubtitleTracks() {
    return subtitleTracks;
  }

  public void setSubtitleTracks(Set<SubtitleTrack> subtitleTracks) {
    this.subtitleTracks = subtitleTracks;
  }

  public Date getAddedToLibrary() {
    return addedToLibrary;
  }

  public void setAddedToLibrary(Date addedToLibrary) {
    this.addedToLibrary = addedToLibrary;
  }
}

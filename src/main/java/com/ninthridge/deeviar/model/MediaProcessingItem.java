package com.ninthridge.deeviar.model;

import java.util.Date;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class MediaProcessingItem implements Comparable<MediaProcessingItem> {
  
  @Property
  private String profileTitle;
  @Property
  private String category;
  @Property
  private String title;
  
  private String episodeTitle;
  @Property
  private Integer season;
  @Property
  private Integer episode;
  @Property
  private Integer year;
  
  private String canonicalPath;
  
  private String description;
  
  private Boolean deleteSource;
  
  private Date timestamp;
  
  private Date releaseDate;
  
  private Boolean compress;
  
  @Property
  private Long start;
  
  private Long duration;
  
  private int priority;
  
  public String getProfileTitle() {
    return profileTitle;
  }
  public void setProfileTitle(String profileTitle) {
    this.profileTitle = profileTitle;
  }
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  
  public String getCanonicalPath() {
    return canonicalPath;
  }
  public void setCanonicalPath(String canonicalPath) {
    this.canonicalPath = canonicalPath;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getEpisodeTitle() {
    return episodeTitle;
  }
  public void setEpisodeTitle(String episodeTitle) {
    this.episodeTitle = episodeTitle;
  }
  public Integer getSeason() {
    return season;
  }
  public void setSeason(Integer season) {
    this.season = season;
  }
  public Integer getEpisode() {
    return episode;
  }
  public void setEpisode(Integer episode) {
    this.episode = episode;
  }
  public Integer getYear() {
    return year;
  }
  public void setYear(Integer year) {
    this.year = year;
  }
  
  public Boolean getDeleteSource() {
    return deleteSource;
  }
  public void setDeleteSource(Boolean deleteSource) {
    this.deleteSource = deleteSource;
  }
  
  public Long getStart() {
    return start;
  }
  public void setStart(Long start) {
    this.start = start;
  }
  public Long getDuration() {
    return duration;
  }
  public void setDuration(Long duration) {
    this.duration = duration;
  }
  
  public Date getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
  
  public Boolean getCompress() {
    return compress;
  }
  public void setCompress(Boolean compress) {
    this.compress = compress;
  }
  
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  public Date getReleaseDate() {
    return releaseDate;
  }
  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
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
  public int compareTo(MediaProcessingItem mediaProcessingItem) {
    return priority - mediaProcessingItem.getPriority();
  }
}

package com.ninthridge.deeviar.model;

import java.io.Serializable;

import com.ninthridge.deeviar.model.id.EpisodeId;

public class Episode extends Video implements Serializable {

  private static final long serialVersionUID = 1L;

  private String seriesId;
  private Integer season;
  private Integer episode;
  
  public Episode() {

  }

  public Episode(EpisodeId episodeId) {
    setId(episodeId.getId());
    this.seriesId = episodeId.getSeriesId().getId();
    this.season = episodeId.getSeason();
    this.episode = episodeId.getEpisode();
  }

  @Override
  public ContentType getType() {
    return ContentType.Episode;
  }

  public String getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
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

  @Override
  public int compareTo(VideoContent videoContent) {
    if(videoContent instanceof Episode) {
      Episode e = (Episode)videoContent;
      if(!seriesId.equals(e.getSeriesId())) {
        return seriesId.compareTo(e.getSeriesId());
      }
      if(!season.equals(e.getSeason())) {
        return season.compareTo(e.getSeason());
      }
      return episode.compareTo(e.getEpisode());
    }
    return 1;
  }
}
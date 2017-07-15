package com.ninthridge.deeviar.model.id;

public class EpisodeId extends MediaId {

  private SeriesId seriesId;
  private Integer season;
  private Integer episode;
  
  public EpisodeId(SeriesId seriesId, Integer season, Integer episode) {
    this.seriesId = seriesId;
    this.season = season;
    this.episode = episode;
    //For series without season and episode numbers, we are defaulting to season=YYYY episode=MMDD.  
    //This could create a uniqueness conflict if there are multiple episodes that aired on the same day
    setId(seriesId.getId() + "S" + (season != null ? String.format("%1$04d", season) : "0000") + "E" + (episode != null ? String.format("%1$04d", episode) : "0000"));
  }

  public SeriesId getSeriesId() {
    return seriesId;
  }

  protected void setSeriesId(SeriesId seriesId) {
    this.seriesId = seriesId;
  }

  public Integer getSeason() {
    return season;
  }

  protected void setSeason(Integer season) {
    this.season = season;
  }

  public Integer getEpisode() {
    return episode;
  }

  protected void setEpisode(Integer episode) {
    this.episode = episode;
  }
}
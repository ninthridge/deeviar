package com.ninthridge.deeviar.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class SeriesMixIn {
  @JsonIgnore
  public abstract Set<Episode> getEpisodes();
}

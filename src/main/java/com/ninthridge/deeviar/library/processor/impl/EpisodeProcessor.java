package com.ninthridge.deeviar.library.processor.impl;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.image.ImageExtractor;
import com.ninthridge.deeviar.library.processor.ContentProcessorException;
import com.ninthridge.deeviar.library.scraper.EpisodeScraper;
import com.ninthridge.deeviar.model.Episode;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.model.id.EpisodeId;
import com.ninthridge.deeviar.model.id.SeriesId;
import com.ninthridge.deeviar.util.ImageUtil;

@Service("episodeProcessor")
public class EpisodeProcessor extends AbstractVideoProcessor<Episode> {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private EpisodeScraper episodeScraper;

  @Autowired
  private SeriesProcessor seriesProcessor;
  
  @Autowired
  private ImageExtractor imageExtractor;
  
  @Override
  public Episode processVideo(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException {
    SeriesId seriesId = seriesProcessor.getSeriesId(mediaProcessingItem);
    if(seriesId != null && seriesId.getId() != null) {
      Series series = libraryService.findSeriesById(mediaProcessingItem.getProfileTitle(), seriesId.getId());
      log.info(seriesId.getId());
      if(series != null) {
        EpisodeId episodeId = getEpisodeId(mediaProcessingItem, seriesId);
        if(episodeId != null && episodeId.getId() != null) {
          String title = seriesId.getTitle() + " - S" + episodeId.getSeason() + ": E" + episodeId.getEpisode();
          log.info("Processing episode " + title);
          Episode episode = new Episode(episodeId);
          episode.setTimestamp(mediaProcessingItem.getTimestamp());
          if(!episodeScraper.scrape(series, episode)) {
            log.warn("Unable to find episode match for " + mediaProcessingItem.getCanonicalPath());
            if(mediaProcessingItem.getEpisodeTitle() != null) {
              episode.setDescription(mediaProcessingItem.getTitle());
            }
            if(mediaProcessingItem.getDescription() != null) {
              if(episode.getDescription() == null) {
                episode.setDescription("");
              }
              if(episode.getDescription().length() > 0) {
                episode.setDescription(episode.getDescription() + " - ");
              }
              episode.setDescription(episode.getDescription() + mediaProcessingItem.getDescription());
            }
          }
          else {
            episode.setDescription(episode.getTitle() + " - " + episode.getDescription());
          }
          
          if(episode.getReleased() == null) {
            episode.setReleased(mediaProcessingItem.getReleaseDate());
          }
          
          episode.setShortDescription(episode.getTitle());
          episode.setTitle(series.getTitle() + " - S" + episode.getSeason() + ": E" + episode.getEpisode());
          log.info("Finished processing episode " + title);
          return episode;
        }
        else {
          throw new ContentProcessorException("Invalid episode name " + mediaProcessingItem.getCanonicalPath());
        }
      }
      else {
        throw new ContentProcessorException("Invalid series name " + mediaProcessingItem.getCanonicalPath());
      }
    }
    else {
      throw new ContentProcessorException("Invalid series name " + mediaProcessingItem.getCanonicalPath());
    }
  }

  @Override
  public String getRelativeDestination(String profileTitle, Episode episode) {
    return libraryService.getRelativeEpisodeFile(profileTitle, episode);
  }

  @Override
  public void setupImages(String profileTitle, Episode episode) {
    if(episode.getLength() > 0) {
      File imageFile = new File(config.getImagesDir(), profileTitle + File.separator + episode.getId() + ".png");
      if(!imageFile.getParentFile().exists()) {
        imageFile.getParentFile().mkdirs();
      }
      
      double s = 300.0;
      if(episode.getLength() < 360) {
        s = episode.getLength() / 2;
      }
      try {
        File videoFile = new File(config.getVideosDir().getParentFile(), episode.getStreams().get(0).getUri());
        BufferedImage image = imageExtractor.extractImage(videoFile, s);
        ImageUtil.saveImage(image, imageFile);
        //TODO: resize
        episode.setHdPosterUri("/images/" + profileTitle + "/" + imageFile.getName());
        episode.setSdPosterUri(episode.getHdPosterUri());
      } catch (Exception e) {
        log.error(e, e);
      } 
    }
  }
  
  private EpisodeId getEpisodeId(MediaProcessingItem mediaProcessingItem, SeriesId seriesId) {
    EpisodeId episodeId = null;
    if(mediaProcessingItem.getSeason() != null && mediaProcessingItem.getEpisode() != null) {
      episodeId = new EpisodeId(seriesId, mediaProcessingItem.getSeason(), mediaProcessingItem.getEpisode());
    }
    return episodeId;
  }
  
  @Override
  protected String getId(MediaProcessingItem mediaProcessingItem) {
    SeriesId seriesId = seriesProcessor.getSeriesId(mediaProcessingItem);
    if(seriesId != null && seriesId.getId() != null) {
      Series series = libraryService.findSeriesById(mediaProcessingItem.getProfileTitle(), seriesId.getId());
      if(series != null) {
        EpisodeId episodeId = getEpisodeId(mediaProcessingItem, seriesId);
        return episodeId.getId();
      }
    }
    return null;
  }
}

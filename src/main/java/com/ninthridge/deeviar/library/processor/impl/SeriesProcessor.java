package com.ninthridge.deeviar.library.processor.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.library.processor.ContentProcessor;
import com.ninthridge.deeviar.library.processor.ContentProcessorException;
import com.ninthridge.deeviar.library.scraper.SeriesScraper;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.model.id.SeriesId;
import com.ninthridge.deeviar.service.LibraryService;
import com.ninthridge.deeviar.util.FileNameUtil;
import com.ninthridge.deeviar.util.HttpUtil;
import com.ninthridge.deeviar.util.ImageUtil;
import com.ninthridge.deeviar.util.ImageUtil.VALIGN;

@Service("seriesProcessor")
public class SeriesProcessor implements ContentProcessor<Series> {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private SeriesScraper seriesScraper;

  @Autowired
  private LibraryService libraryService;

  @Autowired
  private Config config;
  
  @Override
  public void process(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException {
    SeriesId seriesId = getSeriesId(mediaProcessingItem);
    if(seriesId != null) {
      Series series = libraryService.findSeriesById(mediaProcessingItem.getProfileTitle(), seriesId.getId());
      if(series == null) {
        String title = seriesId.getTitle() + (seriesId.getYear() != null ? " (" + seriesId.getYear() + ")" : "");
        log.info("Processing series " + title);
        series = new Series(seriesId);
        series.setCategory(mediaProcessingItem.getCategory());
        if(!seriesScraper.scrape(series)) {
          log.warn("Unable to find series match for " + mediaProcessingItem.getCanonicalPath());
          series.setDescription("");
        }
        setupImages(mediaProcessingItem.getProfileTitle(), series);
        libraryService.save(mediaProcessingItem.getProfileTitle(), series);
        log.info("Finished processing series " + title);
      }
    }
    else {
      throw new ContentProcessorException("Invalid series name " + mediaProcessingItem.getCanonicalPath());
    }
  }

  protected void setupImages(String profileTitle, Series series) {
    
    if(series.getExternalPosterUrl() != null) {
      String extension = FileNameUtil.parseExtension(series.getExternalPosterUrl());
      if(extension == null) {
        extension = "png";
      }
      File posterFile = new File(config.getImagesDir(), profileTitle + File.separator + series.getId() + "." + extension);
      if(!posterFile.getParentFile().exists()) {
        posterFile.getParentFile().mkdirs();
      }
      
      if(!posterFile.exists()) {
        log.info("Downloading poster for " + series.getTitle());
        try {
          HttpUtil.download(series.getExternalPosterUrl(), posterFile);
        } catch (IOException e) {
          log.error("Unexpected IOException downloading poster for " + series.getTitle(), e);
        }
      }

      if(posterFile.exists()) {
        //TODO: resize
        series.setHdPosterUri("/images/" + profileTitle + "/" + posterFile.getName());
        series.setSdPosterUri(series.getHdPosterUri());
      }
    }
    
    if(series.getHdPosterUri() == null) {
      File posterFile = new File(config.getImagesDir(), profileTitle + File.separator + series.getId() + ".png");
      if(!posterFile.getParentFile().exists()) {
        posterFile.getParentFile().mkdirs();
      }
      BufferedImage image = ImageUtil.createTextImage(Arrays.asList(series.getTitle()), 210, 270, Color.BLACK, Color.WHITE, new Font("Arial", Font.BOLD, 30), VALIGN.CENTER);
      try {
        ImageUtil.saveImage(image, posterFile);
      } catch (IOException e) {
        log.error(e, e);
      }
      //TODO: resize
      series.setHdPosterUri("/images/" + profileTitle + "/" + posterFile.getName());
      series.setSdPosterUri(series.getHdPosterUri());
    }
  }
  
  public SeriesId getSeriesId(MediaProcessingItem mediaProcessingItem) {
    SeriesId seriesId = null;
    if(mediaProcessingItem.getTitle() != null) {
      seriesId = new SeriesId(mediaProcessingItem.getTitle(), mediaProcessingItem.getYear());
    }
    
    return seriesId;
  }
}

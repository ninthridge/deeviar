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

import com.ninthridge.deeviar.library.processor.ContentProcessorException;
import com.ninthridge.deeviar.library.scraper.MovieScraper;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Movie;
import com.ninthridge.deeviar.model.id.MovieId;
import com.ninthridge.deeviar.util.FileNameUtil;
import com.ninthridge.deeviar.util.HttpUtil;
import com.ninthridge.deeviar.util.ImageUtil;
import com.ninthridge.deeviar.util.ImageUtil.VALIGN;

@Service("movieProcessor")
public class MovieProcessor extends AbstractVideoProcessor<Movie> {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private MovieScraper movieScraper;

  @Override
  public Movie processVideo(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException {
    MovieId movieId = getMovieId(mediaProcessingItem);
    if(movieId != null && movieId.getId() != null) {
      String title = movieId.getTitle() + (movieId.getYear() != null ? " (" + movieId.getYear() + ")" : "");
      log.info("Processing movie " + title);
      Movie movie = new Movie(movieId);
      movie.setTimestamp(mediaProcessingItem.getTimestamp());
      if(!movieScraper.scrape(movie)) {
        log.warn("Unable to find movie match for " + mediaProcessingItem.getCanonicalPath());
        if(mediaProcessingItem.getDescription() != null) {
          movie.setDescription(mediaProcessingItem.getDescription());
        }
        else {
          movie.setDescription("");
        }
      }
      
      if(movie.getReleased() == null) {
        movie.setReleased(mediaProcessingItem.getReleaseDate());
      }
      
      log.info("Finished processing movie " + title);
      return movie;
    }
    else {
      throw new ContentProcessorException("Invalid movie name " + mediaProcessingItem.getCanonicalPath());
    }
  }

  @Override
  public String getRelativeDestination(String profileTitle, Movie movie) {
    return libraryService.getRelativeMovieFile(profileTitle, movie);
  }

  @Override
  public void setupImages(String profileTitle, Movie movie) {
    if(movie.getExternalPosterUrl() != null) {
      String extension = FileNameUtil.parseExtension(movie.getExternalPosterUrl());
      if(extension == null) {
        extension = "png";
      }
      
      File posterFile = new File(config.getImagesDir(), profileTitle + File.separator + movie.getId() + "." + extension);
      if(!posterFile.getParentFile().exists()) {
        posterFile.getParentFile().mkdirs();
      }

      if(!posterFile.exists()) {
        log.info("Downloading poster for " + movie.getTitle());
        try {
          HttpUtil.download(movie.getExternalPosterUrl(), posterFile);
        } catch (IOException e) {
          log.error("Unexpected IOException downloading poster for " + movie.getTitle(), e);
        }
      }

      if(posterFile.exists()) {
        //TODO: resize
        movie.setHdPosterUri("/images/" + profileTitle + "/" + posterFile.getName());
        movie.setSdPosterUri(movie.getHdPosterUri());
      }
    }
    
    if(movie.getHdPosterUri() == null) {
      File imageFile = new File(config.getImagesDir(), profileTitle + File.separator + movie.getId() + ".png");
      if(!imageFile.getParentFile().exists()) {
        imageFile.getParentFile().mkdirs();
      }
      BufferedImage image = ImageUtil.createTextImage(Arrays.asList(movie.getTitle()), 210, 270, Color.BLACK, Color.WHITE, new Font("Arial", Font.BOLD, 30), VALIGN.CENTER);
      try {
        ImageUtil.saveImage(image, imageFile);
      } catch (IOException e) {
        log.error(e, e);
      }
      //TODO: resize
      movie.setHdPosterUri("/images/" + profileTitle + "/" + imageFile.getName());
      movie.setSdPosterUri(movie.getHdPosterUri());
    }
  }
  
  private MovieId getMovieId(MediaProcessingItem mediaProcessingItem) {
    MovieId movieId = null;
    if(mediaProcessingItem.getTitle() != null) {
      movieId = new MovieId(mediaProcessingItem.getTitle(), mediaProcessingItem.getYear());
    }
    
    return movieId;
  }
  
  @Override
  protected String getId(MediaProcessingItem mediaProcessingItem) {
    MovieId movieId = getMovieId(mediaProcessingItem);
    if(movieId != null) {
      return movieId.getId();
    }
    return null;
  }
}

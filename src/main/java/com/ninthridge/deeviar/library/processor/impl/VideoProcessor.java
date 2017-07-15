package com.ninthridge.deeviar.library.processor.impl;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.image.ImageExtractor;
import com.ninthridge.deeviar.library.processor.ContentProcessorException;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Video;
import com.ninthridge.deeviar.model.id.VideoId;
import com.ninthridge.deeviar.util.ImageUtil;

@Service("videoProcessor")
public class VideoProcessor extends AbstractVideoProcessor<Video> {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private ImageExtractor imageExtractor;
  
  @Override
  public Video processVideo(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException {
    VideoId videoId = getVideoId(mediaProcessingItem);
    if(videoId != null && videoId.getId() != null) {
      String id = videoId.getId();
      String title = mediaProcessingItem.getTitle();
      if(title != null) {
        title = mediaProcessingItem.getCanonicalPath();
      }
      log.info("Processing video " + title);
      Video video = new Video(videoId);
      if(mediaProcessingItem.getDescription() != null) {
        video.setDescription(mediaProcessingItem.getDescription());
      }
      else {
        video.setDescription("");
      }
      video.setTimestamp(mediaProcessingItem.getTimestamp());
      video.setReleased(mediaProcessingItem.getReleaseDate());
      video.setId(id);
      
      log.info("Finished processing video " + title);
      return video;
    }
    else {
      throw new ContentProcessorException("Invalid video name " + mediaProcessingItem.getCanonicalPath());
    }
  }

  @Override
  public String getRelativeDestination(String profileTitle, Video video) {
    return libraryService.getRelativeVideoFile(profileTitle,  video);
  }

  @Override
  public void setupImages(String profileTitle, Video video) {
    if(video.getLength() > 0) {
      File imageFile = new File(config.getImagesDir(), profileTitle + File.separator + video.getId() + ".png");
      if(!imageFile.getParentFile().exists()) {
        imageFile.getParentFile().mkdirs();
      }
      
      double s = 300.0;
      if(s < 360) {
        s = video.getLength() / 2;
      }
      
      try {
        File videoFile = new File(config.getVideosDir().getParentFile(), video.getStreams().get(0).getUri());
        BufferedImage image = imageExtractor.extractImage(videoFile, s);
        ImageUtil.saveImage(image, imageFile);
        //TODO: resize
        video.setHdPosterUri("/images/" + profileTitle + "/" + imageFile.getName());
        video.setSdPosterUri(video.getHdPosterUri());
      } catch (Exception e) {
        log.error(e, e);
      } 
    }
  }
  
  private VideoId getVideoId(MediaProcessingItem mediaProcessingItem) {
    VideoId videoId = null;
    if(mediaProcessingItem.getTitle() != null) {
      videoId = new VideoId(mediaProcessingItem.getTitle());
    }
    
    return videoId;
  }
  
  @Override
  protected String getId(MediaProcessingItem mediaProcessingItem) {
    VideoId videoId = getVideoId(mediaProcessingItem);
    if(videoId != null) {
      return videoId.getId();
    }
    return null;
  }
}

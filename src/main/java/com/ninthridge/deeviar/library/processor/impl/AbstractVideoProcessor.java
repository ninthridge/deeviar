package com.ninthridge.deeviar.library.processor.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutorFactory;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.library.processor.ContentProcessor;
import com.ninthridge.deeviar.library.processor.ContentProcessorException;
import com.ninthridge.deeviar.manager.MediaProcessingManager;
import com.ninthridge.deeviar.medianalyzer.MediaAnalyzer;
import com.ninthridge.deeviar.medianalyzer.model.MediaInfo;
import com.ninthridge.deeviar.medianalyzer.model.Stream;
import com.ninthridge.deeviar.model.BifProcessingItem;
import com.ninthridge.deeviar.model.Content;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Video;
import com.ninthridge.deeviar.model.VideoStream;
import com.ninthridge.deeviar.service.LibraryService;
import com.ninthridge.deeviar.transcoder.Transcoder;

public abstract class AbstractVideoProcessor<T extends Video> implements ContentProcessor<Content> {
  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private MediaAnalyzer mediaAnalyzer;

  @Autowired
  private MediaProcessingManager mediaProcessingManager;

  @Autowired
  private Transcoder transcoder;

  @Autowired
  protected LibraryService libraryService;

  @Autowired
  private OperatingSystemCmdExecutorFactory operatingSystemCmdExecutorFactory;
  
  @Autowired
  protected Config config;
  
  protected abstract T processVideo(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException;

  protected abstract String getId(MediaProcessingItem mediaProcessingItem);
  
  public abstract String getRelativeDestination(String profileTitle, T video);

  protected abstract void setupImages(String profile, T video);

  @Override
  public void process(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException {
    String id = getId(mediaProcessingItem);
    Date timestamp = libraryService.getTimestamp(mediaProcessingItem.getProfileTitle(), id);
    if(timestamp == null) {
      Date deletedTimestamp = libraryService.getDeletedTimestamp(mediaProcessingItem.getProfileTitle(), id);
      Date erroredTimestamp = libraryService.getErroredTimestamp(mediaProcessingItem.getProfileTitle(), mediaProcessingItem.getCanonicalPath());
     
      if(deletedTimestamp != null && erroredTimestamp != null) {
        if(deletedTimestamp.before(erroredTimestamp)) {
          timestamp = deletedTimestamp;
        }
        else {
          timestamp = erroredTimestamp;
        }
      }
      else if(deletedTimestamp != null) {
        timestamp = deletedTimestamp;
      }
      else {
        timestamp = erroredTimestamp;
      }
    }
    
    if(timestamp == null || mediaProcessingItem.getTimestamp().compareTo(timestamp) > 0) {
      T video = processVideo(mediaProcessingItem);
      if(video != null) {
        video.setCategory(mediaProcessingItem.getCategory());
        video.setReleased(mediaProcessingItem.getReleaseDate());
        video.setAddedToLibrary(timestamp);
        try {
          String relativeDestination = getRelativeDestination(mediaProcessingItem.getProfileTitle(), video);
          File destinationFile = new File(config.getVideosDir(), relativeDestination);
          
          if(!destinationFile.exists() || mediaProcessingItem.getTimestamp().getTime() > destinationFile.lastModified()) {
            transcoder.transcode(mediaProcessingItem, destinationFile);
          }
  
          if(destinationFile.exists()) {
            MediaInfo destinationMediaInfo = null;
            destinationMediaInfo = mediaAnalyzer.analyze(destinationFile.getCanonicalPath());
            log.info(destinationMediaInfo);
            
            if(destinationMediaInfo != null && destinationMediaInfo.getFormat() != null && destinationMediaInfo.getFormat().getBitRate() != null && destinationMediaInfo.getFormat().getDuration() != null) {
              List<VideoStream> videoStreams = new ArrayList<>();
              
              for(Stream stream : destinationMediaInfo.getStreams()) {
                if("video".equals(stream.getCodecType())) {
                  VideoStream videoStream = new VideoStream();
                  
                  videoStream.setUri("/videos" + relativeDestination);
      
                  //TODO: dynamically set these fields
                  videoStream.setStreamFormat("mp4");
                  videoStream.setBitRate(new Integer(destinationMediaInfo.getFormat().getBitRate())/1000);
                  videoStream.setLength(new Double(Math.ceil(new Double(destinationMediaInfo.getFormat().getDuration()))).intValue());
                  
                  videoStream.setHeight(stream.getHeight());
                  videoStream.setWidth(stream.getWidth());
                  
                  videoStreams.add(videoStream);
                }
                else if("audio".equals(stream.getCodecType())) {
                  
                }
                else if("subtitle".equals(stream.getCodecType())) {
                  
                }
              }
              
              video.setStreams(videoStreams);
              
              setupImages(mediaProcessingItem.getProfileTitle(), video);
              libraryService.save(mediaProcessingItem.getProfileTitle(), video);
              
              if(operatingSystemCmdExecutorFactory.getExecutor().programExists("biftool")) {
                mediaProcessingManager.addToPostProcessingQueue(new BifProcessingItem(mediaProcessingItem.getProfileTitle(), video, mediaProcessingItem.getPriority()));
                log.info("Added " + video + " to the post processing queue");
              }
            }
            else {
              libraryService.addErrored(mediaProcessingItem.getProfileTitle(), mediaProcessingItem.getCanonicalPath());
              throw new ContentProcessorException("Unable to extract media info for " + video.getId() + " " + destinationFile.toPath());
              //TODO: delete the transcoded file and try again
            }
          }
          else {
            libraryService.addErrored(mediaProcessingItem.getProfileTitle(), mediaProcessingItem.getCanonicalPath());
            throw new ContentProcessorException("Unable to transcode " + video.getId() + " " + mediaProcessingItem.getCanonicalPath());
          }
        }
        catch(Exception e) {
          libraryService.addErrored(mediaProcessingItem.getProfileTitle(), mediaProcessingItem.getCanonicalPath());
          log.error(e, e);
          throw new ContentProcessorException("Unexpected exception processing " + video.getId() + " " + mediaProcessingItem.getCanonicalPath() + " " + e.toString());
        }
      }
    }
  }
}

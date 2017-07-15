package com.ninthridge.deeviar.manager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ninthridge.deeviar.bif.BifGenerator;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.library.processor.ContentProcessor;
import com.ninthridge.deeviar.library.processor.ContentProcessorFactory;
import com.ninthridge.deeviar.model.BifProcessingItem;
import com.ninthridge.deeviar.model.Content;
import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.service.LibraryService;

@Component("mediaProcessingManager")
public class MediaProcessingManager implements Runnable {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private ContentProcessorFactory contentProcessorFactory;

  @Autowired
  private BifGenerator bifGenerator;

  @Autowired
  private LibraryService libraryService;

  @Autowired
  private Config config;

  private Lock processingLock = new ReentrantLock();

  private Lock postProcessingLock = new ReentrantLock();

  private LinkedList<MediaProcessingItem> processingItems = new LinkedList<>();

  private LinkedList<BifProcessingItem> postProcessingItems = new LinkedList<>();

  private MediaProcessingItem mediaProcessingItem;
  
  private BifProcessingItem bifProcessingItem;
  
  @Override
  public void run() {
    try {
      libraryService.loadBifProcessingQueue();
      
      while (true) {
        if (!processOne()) {
          if (!postProcessOne()) {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              log.error(e);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error(e, e);
    }
  }

  private boolean processOne() {
    try {
      processingLock.lock();
      if (!processingItems.isEmpty()) {
        mediaProcessingItem = processingItems.pop();
      }
    } finally {
      processingLock.unlock();
    }

    if (mediaProcessingItem != null) {
      boolean success = true;
      List<ContentProcessor<? extends Content>> contentProcessors = contentProcessorFactory
          .getContentProcessors(mediaProcessingItem);
      for (ContentProcessor<? extends Content> contentProcessor : contentProcessors) {
        try {
          contentProcessor.process(mediaProcessingItem);
        } catch (Exception e) {
          log.error(e, e);
          success = false;
        }
      }

      if (success) {
        if (mediaProcessingItem.getDeleteSource()) {
          if (mediaProcessingItem.getCanonicalPath().startsWith("smb://")) {
            // TODO: add smb support
          } else {
            new File(mediaProcessingItem.getCanonicalPath()).delete();
          }
        }
      }

      try {
        processingLock.lock();
        mediaProcessingItem = null;
      } finally {
        processingLock.unlock();
      }

      return true;
    }

    return false;
  }

  public void addToProcessingQueue(MediaProcessingItem mediaProcessingItem) {
    try {
      processingLock.lock();
      
      //TODO: this method is very inefficient.  needs optimization
      int index = processingItems.indexOf(mediaProcessingItem);
      if(index > -1) {
        MediaProcessingItem item = processingItems.get(index);
        if(mediaProcessingItem.getTimestamp().after(item.getTimestamp())) {
          if(index > 0) {
            processingItems.remove(index);
          }
        }
        else {
          return;
        }
      }
      
      if(mediaProcessingItem.equals(this.mediaProcessingItem) && !mediaProcessingItem.getTimestamp().after(this.mediaProcessingItem.getTimestamp())) {
        return;
      }
      
      for (int i = processingItems.size()-1; i >= 0; i--) {
        if (mediaProcessingItem.getPriority() >= processingItems.get(i).getPriority()) {
          processingItems.add(i+1, mediaProcessingItem);
          return;
        }
      }
      processingItems.add(0, mediaProcessingItem);
    } finally {
      processingLock.unlock();
    }
  }
  
  public boolean isProcessingQueueEmpty() {
    try {
      processingLock.lock();
      return processingItems.isEmpty() && this.mediaProcessingItem == null;
    } finally {
      processingLock.unlock();
    }
  }

  public int processingQueueSize() {
    try {
      processingLock.lock();
      return processingItems.size() + (this.mediaProcessingItem != null ? 1 : 0);
    } finally {
      processingLock.unlock();
    }
  }

  public boolean isInProcessingQueue(MediaProcessingItem mediaProcessingItem) {
    try {
      processingLock.lock();
      return processingItems.contains(mediaProcessingItem) || mediaProcessingItem.equals(this.mediaProcessingItem);
    } finally {
      processingLock.unlock();
    }
  }

  private boolean postProcessOne() {
    // possibly only generate when no streams are active since extracting images
    // with ffmpeg is cpu intensive and can cause our active streams to get
    // hosed
    try {
      postProcessingLock.lock();
      if (!postProcessingItems.isEmpty()) {
        bifProcessingItem = postProcessingItems.pop();
      }
    } finally {
      postProcessingLock.unlock();
    }

    if (bifProcessingItem != null) {
      File videoFile = new File(config.getVideosDir().getParentFile(), bifProcessingItem.getVideo().getStreams().get(0).getUri());
      log.info("Post processing " + bifProcessingItem.getVideo().getId() + " " + videoFile.getAbsolutePath());
      try {

        if (videoFile != null && videoFile.exists()) {
          int bifWidthHd = 320;
          File bifFileHd = new File(config.getBifsDir(),
              bifProcessingItem.getProfileTitle() + File.separator + bifProcessingItem.getVideo().getId() + "_" + bifWidthHd + ".bif");
          if (!bifFileHd.exists()) {
            if (!bifFileHd.getParentFile().exists()) {
              bifFileHd.getParentFile().mkdirs();
            }
            bifGenerator.createBif(videoFile, bifFileHd, bifWidthHd);
          }

          int bifWidthSd = 240;
          File bifFileSd = new File(config.getBifsDir(),
              bifProcessingItem.getProfileTitle() + File.separator + bifProcessingItem.getVideo().getId() + "_" + bifWidthSd + ".bif");
          if (!bifFileSd.exists()) {
            if (!bifFileSd.getParentFile().exists()) {
              bifFileSd.getParentFile().mkdirs();
            }
            bifGenerator.createBif(videoFile, bifFileSd, bifWidthSd);
          }

          if (bifFileHd.exists() && bifFileSd.exists()) {
            libraryService.updateBif(bifProcessingItem.getProfileTitle(), bifProcessingItem.getVideo().getId(),
                "/bifs/" + bifProcessingItem.getProfileTitle() + "/" + bifFileHd.getName(),
                "/bifs/" + bifProcessingItem.getProfileTitle() + "/" + bifFileSd.getName());
          } else {
            log.error("Unable to generate bifs for " + bifProcessingItem.getVideo().getId() + " " + videoFile.getCanonicalPath());
          }
        } else {
          log.error(
              "Bif creation failed.  Unable to find " + bifProcessingItem.getVideo().getId() + " " + videoFile.getCanonicalPath());
        }
      } catch (Exception e) {
        log.error(e, e);
      } finally {
        try {
          postProcessingLock.lock();
          log.info("Finished post processing " + bifProcessingItem.getVideo().getId() + " " + videoFile.getAbsolutePath());
          bifProcessingItem = null;
        } finally {
          postProcessingLock.unlock();
        }
      }

      

      return true;
    }
    return false;
  }

  public void addToPostProcessingQueue(BifProcessingItem bifProcessingItem) {
    try {
      postProcessingLock.lock();
      //TODO: optimize
      if (!postProcessingItems.contains(bifProcessingItem) && !bifProcessingItem.equals(this.bifProcessingItem)) {
        for (int i = postProcessingItems.size()-1; i >= 0; i--) {
          if (bifProcessingItem.getPriority() >= postProcessingItems.get(i).getPriority()) {
            postProcessingItems.add(i+1, bifProcessingItem);
            return;
          }
        }
        postProcessingItems.add(0, bifProcessingItem);
      }
    } finally {
      postProcessingLock.unlock();
    }
  }
}

package com.ninthridge.deeviar.library.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.processor.impl.EpisodeProcessor;
import com.ninthridge.deeviar.library.processor.impl.MovieProcessor;
import com.ninthridge.deeviar.library.processor.impl.SeriesProcessor;
import com.ninthridge.deeviar.library.processor.impl.VideoProcessor;
import com.ninthridge.deeviar.model.Content;
import com.ninthridge.deeviar.model.MediaProcessingItem;

@Service("contentProcessorFactory")
public class ContentProcessorFactory {

  @Autowired
  private MovieProcessor movieProcessor;

  @Autowired
  private SeriesProcessor seriesProcessor;

  @Autowired
  private EpisodeProcessor episodeProcessor;

  @Autowired
  private VideoProcessor videoProcessor;

  public List<ContentProcessor<? extends Content>> getContentProcessors(MediaProcessingItem mediaProcessingItem) {
    List<ContentProcessor<? extends Content>> processors = new ArrayList<ContentProcessor<? extends Content>>();
    if ("Movies".equalsIgnoreCase(mediaProcessingItem.getCategory())) {
      processors.add(movieProcessor);
    } else if ("Series".equalsIgnoreCase(mediaProcessingItem.getCategory())) {
      processors.add(seriesProcessor);
      processors.add(episodeProcessor);
    } else {
      processors.add(videoProcessor);
    }
    return processors;
  }

}

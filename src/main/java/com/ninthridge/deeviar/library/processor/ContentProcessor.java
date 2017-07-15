package com.ninthridge.deeviar.library.processor;

import com.ninthridge.deeviar.model.Content;
import com.ninthridge.deeviar.model.MediaProcessingItem;

public interface ContentProcessor<T extends Content> {
  void process(MediaProcessingItem mediaProcessingItem) throws ContentProcessorException;
}

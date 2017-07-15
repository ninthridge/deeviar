package com.ninthridge.deeviar.transcoder;

import java.io.File;

import com.ninthridge.deeviar.model.MediaProcessingItem;
import com.ninthridge.deeviar.model.Stream;
import com.ninthridge.deeviar.model.Tuner;

public interface Transcoder {

  Stream start(Tuner tuner, String stationId);
  void transcode(MediaProcessingItem mediaProcessingItem, File output);
  void stop(Stream stream);
}

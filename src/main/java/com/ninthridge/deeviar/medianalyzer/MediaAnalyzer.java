package com.ninthridge.deeviar.medianalyzer;

import java.io.IOException;

import com.ninthridge.deeviar.medianalyzer.model.MediaInfo;

public interface MediaAnalyzer {
  public MediaInfo analyze(String canonicalPath) throws IOException, InterruptedException;
}

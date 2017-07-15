package com.ninthridge.deeviar.image;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ImageExtractor  {

  BufferedImage extractImage(File videoFile, double second) throws Exception;
  void extractImages(File videoFile, File dir, int width) throws Exception;
}

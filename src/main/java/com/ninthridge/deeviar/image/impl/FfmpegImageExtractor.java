package com.ninthridge.deeviar.image.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.image.ImageExtractor;
import com.ninthridge.deeviar.medianalyzer.MediaAnalyzer;
import com.ninthridge.deeviar.medianalyzer.model.MediaInfo;
import com.ninthridge.deeviar.util.CmdUtil;
import com.ninthridge.deeviar.util.FileNameUtil;

@Service("imageExtractor")
public class FfmpegImageExtractor implements ImageExtractor {
  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private MediaAnalyzer mediaAnalyzer;
  
  public BufferedImage extractImage(File videoFile, double second) throws Exception {
    log.info("Extracting image for " + videoFile.getCanonicalPath());
    
    File target = new File(config.getTmpDir(), FileNameUtil.trimExtension(videoFile.getName()) + ".jpg");
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("target", target.getCanonicalPath());
    params.put("input", videoFile.getCanonicalPath());
    params.put("second", new Double(second).toString());
    
    Process p = CmdUtil.execute(config.getSingleImageExtractionCommand(), params, null, null);
    p.waitFor();

    BufferedImage image = ImageIO.read(target);
    
    target.delete();
    
    return image;
  }
  
  public void extractImages(File videoFile, File dir, int width) throws Exception {
    if(!dir.exists()) {
      dir.mkdir();
    }

    MediaInfo mediaInfo = mediaAnalyzer.analyze(videoFile.getCanonicalPath());
    if(mediaInfo != null) {
      log.info("Extracting images for " + videoFile.getCanonicalPath());
      
      Map<String, String> params = new HashMap<String, String>();
      params.put("target", dir.getCanonicalPath());
      params.put("input", videoFile.getCanonicalPath());
      params.put("width", new Integer(width).toString());
      params.put("height", new Integer(width * mediaInfo.getStreams().get(0).getHeight() / mediaInfo.getStreams().get(0).getWidth()).toString());
  
      Process p = CmdUtil.execute(config.getMultipleImageExtractionCommand(), params, null, null);
      p.waitFor();
  
      for(File f : dir.listFiles()) {
        Integer dest = new Integer(FileNameUtil.trimExtension(f.getName()))-1;
        String ext = FileNameUtil.parseExtension(f.getName());
        File destFile = new File(f.getParent(), String.format("%08d.%s", dest, ext));
        f.renameTo(destFile);
      }
    }
    else {
      log.error("Unable to get MediaInfo for " + videoFile.getCanonicalPath());
    }
  }
}

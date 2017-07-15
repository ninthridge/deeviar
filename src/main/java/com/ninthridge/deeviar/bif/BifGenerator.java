package com.ninthridge.deeviar.bif;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.image.ImageExtractor;
import com.ninthridge.deeviar.util.CmdUtil;
import com.ninthridge.deeviar.util.FileNameUtil;
import com.ninthridge.deeviar.util.FileUtil;

@Service("bifGenerator")
public class BifGenerator {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  private Config config;

  @Autowired
  private ImageExtractor imageExtractor;

  public void createBif(File videoFile, File bifFile, int width) {
    File tmpDir = config.getTmpDir();
    File targetTmpDir = new File(tmpDir, FileNameUtil.trimExtension(bifFile.getName()));
    File tmpBifFile = new File(tmpDir, bifFile.getName());

    if (targetTmpDir.exists()) {
      FileUtil.deleteAll(targetTmpDir);
    } else {
      targetTmpDir.mkdir();
    }

    if (tmpBifFile.exists()) {
      FileUtil.deleteRecursively(tmpBifFile);
    }

    try {
      log.info("Creating bif for " + videoFile.getCanonicalPath() + " - " + bifFile.getCanonicalPath());

      imageExtractor.extractImages(videoFile, targetTmpDir, width);

      Map<String, String> params = new HashMap<String, String>();
      params.put("target", targetTmpDir.getCanonicalPath());

      Process p = CmdUtil.execute(config.getBifCreationCommand(), params, null, tmpDir);
      p.waitFor();

      if (tmpBifFile.exists()) {
        Files.move(tmpBifFile.toPath(), bifFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("Completed creating bif for " + videoFile.getCanonicalPath() + " - " + bifFile.getCanonicalPath());
      }
    } catch (Exception e) {
      log.error("Unexpected exception creating bif for " + videoFile.getAbsolutePath(), e);
    }

    FileUtil.deleteRecursively(targetTmpDir);
    FileUtil.deleteRecursively(tmpBifFile);
  }
}

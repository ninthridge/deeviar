package com.ninthridge.deeviar.library.scanner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scanner.FileScanner;
import com.ninthridge.deeviar.util.FileNameUtil;

@Service("fileSystemScanner")
public class FileSystemScanner implements FileScanner {

  protected final Log log = LogFactory.getLog(getClass());

  @Override
  public List<ScannedFile> scan(String location) {
    File file = new File(location + File.separator);
    if (file.exists()) {
      return scan(location, File.separator, file);
    } else {
      log.error(file + " does not exist");
    }
    return null;
  }

  protected List<ScannedFile> scan(String location, String path, File dir) {
    List<ScannedFile> items = new ArrayList<>();
    File[] files = dir.listFiles();
    for (File f : files) {
      if (f.isDirectory()) {
        String p = path + f.getName();
        if (!f.getName().endsWith(File.separator)) {
          p += File.separator;
        }
        items.addAll(scan(location, p, f));
      } else if (FileNameUtil.isVideoFile(f.getName())) {
        ScannedFile scannedFile = new ScannedFile();
        scannedFile.setUri(path + f.getName());
        scannedFile.setTimestamp(new Date(f.lastModified()));

        log.debug("Found file " + scannedFile.getUri());
        items.add(scannedFile);
      }
    }
    return items;
  }
}

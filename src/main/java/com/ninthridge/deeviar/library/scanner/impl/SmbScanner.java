package com.ninthridge.deeviar.library.scanner.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scanner.FileScanner;
import com.ninthridge.deeviar.util.FileNameUtil;

@Service("smbScanner")
public class SmbScanner implements FileScanner {

  protected final Log log = LogFactory.getLog(getClass());

  @Override
  public List<ScannedFile> scan(String location) {
    try {
      // TODO: store smb credentials in their own repository
      // SmbFile smbFile = new SmbFile(new NtlmPasswordAuthentication(username + ":" + password));
      SmbFile smbFile = new SmbFile(location + "/");
      if (smbFile.exists()) {
        return scan(location, "/", smbFile);
      } else {
        log.warn("Unable to connect to: " + location);
      }
    } catch (MalformedURLException e) {
      log.error("Invalid location: " + location, e);
    } catch (SmbException e) {
      log.error("Unexpected SmbException scanning " + location, e);
    }
    return null;
  }

  protected List<ScannedFile> scan(String location, String path, SmbFile dir) {
    List<ScannedFile> items = new ArrayList<>();
    try {
      SmbFile[] files = dir.listFiles();
      for (SmbFile f : files) {
        try {
          if (f.isDirectory()) {
            String p = path + f.getName();
            if (!f.getName().endsWith("/")) {
              p += "/";
            }
            items.addAll(scan(location, p, f));
          } else if (FileNameUtil.isVideoFile(f.getName())) {
            ScannedFile scannedFile = new ScannedFile();
            scannedFile.setUri(path + f.getName());
            scannedFile.setTimestamp(new Date(f.lastModified()));

            log.debug("Found file " + scannedFile.getUri());
            items.add(scannedFile);
          }
        } catch (SmbException e) {
          log.error("Unexpected SmbException scanning " + f.getCanonicalPath(), e);
        }
      }
    } catch (SmbException e) {
      log.error("Unexpected SmbException scanning " + dir.getCanonicalPath(), e);
    }
    return items;
  }
}

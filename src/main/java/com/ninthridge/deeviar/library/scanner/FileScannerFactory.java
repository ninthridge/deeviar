package com.ninthridge.deeviar.library.scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.library.scanner.impl.FileSystemScanner;
import com.ninthridge.deeviar.library.scanner.impl.SmbScanner;

@Service("fileScannerFactory")
public class FileScannerFactory {

  @Autowired
  private FileSystemScanner fileSystemScanner;

  @Autowired
  private SmbScanner smbScanner;

  public FileScanner getScanner(String location) {
    if (location != null) {
      if (location.startsWith("smb://")) {
        return smbScanner;
      } else {
        return fileSystemScanner;
      }
    }
    return null;
  }

}

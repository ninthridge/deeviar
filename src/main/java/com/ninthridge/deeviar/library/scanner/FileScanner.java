package com.ninthridge.deeviar.library.scanner;

import java.util.List;

import com.ninthridge.deeviar.library.scanner.impl.ScannedFile;

public interface FileScanner {
  List<ScannedFile> scan(String location);
}

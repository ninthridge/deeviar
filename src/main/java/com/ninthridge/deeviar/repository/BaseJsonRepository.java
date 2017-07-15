package com.ninthridge.deeviar.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ninthridge.deeviar.config.Config;
import com.ninthridge.deeviar.model.Series;
import com.ninthridge.deeviar.model.SeriesMixIn;
import com.ninthridge.deeviar.util.UniqueIdentifierGenerator;

public abstract class BaseJsonRepository<T> {
  
  private static ObjectMapper mapper;
  
  protected static FileFilter jsonFileFilter = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".json");
    }
  };
  
  @Autowired
  protected Config config;
  
  static {
    mapper = new ObjectMapper();
    mapper.addMixIn(Series.class, SeriesMixIn.class);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
  
  protected TypeReference<T> typeReference;

  public BaseJsonRepository(TypeReference<T> typeReference) {
    this.typeReference = typeReference;
  }
  
  protected void writeFile(File file, T content) throws IOException {
    if(!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    File tmpFile = new File(config.getTmpDir(), file.getName() + "." + UniqueIdentifierGenerator.generateUniqueIdentifier());
    mapper.writeValue(tmpFile, content);
    Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
  
  protected T readFile(File file) throws IOException {
    if(file.exists()) {
      return mapper.readValue(file, typeReference);
    }
    else {
      return null;
    }
  }
  
  protected void deleteFile(File file) {
    if(file.exists()) {
      file.delete();
    }
  }
}

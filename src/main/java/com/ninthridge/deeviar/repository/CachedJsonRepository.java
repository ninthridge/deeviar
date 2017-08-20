package com.ninthridge.deeviar.repository;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class CachedJsonRepository<T> extends BaseJsonRepository<T> {

  protected final Log log = LogFactory.getLog(getClass());

  protected T cache = null;
  protected Date timestamp = null;
  
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
 
  protected abstract String getFileName();

  public CachedJsonRepository(TypeReference<T> typeReference) {
    super(typeReference);
    initialize();
  }
  
  private void initialize() {
    File file = getFile();
    if (file.exists()) {
      try {
        cache = readFile(file);
        
        timestamp = new Date(file.lastModified());
      } catch (IOException e) {
        log.error(e, e);
      }
    }
  }

  public void save(T content) {
    log.info(getClass() + " save");
    try {
      lock.writeLock().lock();
      this.cache = content;
      File file = getFile();
      writeFile(file, content);
      timestamp = new Date();
    } catch (IOException e) {
      log.error(e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public T get() {
    log.info(getClass() + " get");
    try {
      lock.readLock().lock();
      return cache;
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public void delete() {
    log.info(getClass() + " delete");
    try {
      lock.writeLock().lock();
      cache = null;
      deleteFile(getFile());
      timestamp = new Date();
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public Date getTimestamp() {
    return timestamp;
  }
  
  private File getFile() {
    return new File(config.getDataDir(), getFileName());
  }
}

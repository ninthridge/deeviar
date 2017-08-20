package com.ninthridge.deeviar.repository;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class CachedMultiJsonRepository<T> extends BaseJsonRepository<T> {

  protected final Log log = LogFactory.getLog(getClass());
  
  protected Map<String, T> cache = new HashMap<>();
  protected Date timestamp = null;
  
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  
  private boolean initialized = false;
  
  protected abstract String getDirName();
  protected abstract String getId(T content);
  
  public CachedMultiJsonRepository(TypeReference<T> typeReference) {
    super(typeReference);
  }
  
  private void initialize() {
    File dir = getDir();
    if(dir.exists()) {
      for(File file : dir.listFiles(jsonFileFilter)) {
        try {
          T content = readFile(file);
          String id = getId(content);
          cache.put(id,  content);
          
          Date fileTimestamp = new Date(file.lastModified());
          if(timestamp == null || fileTimestamp.after(timestamp)) {
            timestamp = fileTimestamp;
          }
        } catch (IOException e) {
          log.error(e, e);
        }
      }
    }
    initialized = true;
  }
  
  public void save(T content) {
    try {
      lock.writeLock().lock();
      if(!initialized) {
        initialize();
      }
      cache.put(getId(content), content);
      File file = getFile(getId(content));
      writeFile(file, content);
      timestamp = new Date();
    } catch (IOException e) {
      log.error(e, e);
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public void delete(String id) {
    try {
      lock.writeLock().lock();
      if(!initialized) {
        initialize();
      }
      cache.remove(id);
      deleteFile(getFile(id));
      timestamp = new Date();
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public T get(String id) {
    try {
      lock.readLock().lock();
      if(!initialized) {
        initialize();
      }
      return cache.get(id);
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public SortedSet<T> getAll() {
    try {
      lock.readLock().lock();
      if(!initialized) {
        initialize();
      }
      SortedSet<T> set = new TreeSet<>();
      for(String key : cache.keySet()) {
        set.add(cache.get(key));
      }
      return set;
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public Date getTimestamp() {
    return timestamp;
  }
  
  private File getFile(String id) {
    return new File(getDir(), id + ".json");
  }
  
  private File getDir() {
    return new File(config.getDataDir(), getDirName());
  }
}

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

public abstract class CachedOwnerJsonRepository<T> extends BaseJsonRepository<T> {

  protected final Log log = LogFactory.getLog(getClass());
  
  protected Map<String, T> cache = new HashMap<>();
  protected Map<String, Date> timestamps = new HashMap<>();
  
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  
  private boolean initialized = false;
  
  protected abstract String getFileName();
  
  public CachedOwnerJsonRepository(TypeReference<T> typeReference) {
    super(typeReference);
  }
  
  private void initialize() {
    File profilesDir = config.getProfilesDir();
    if(profilesDir != null && profilesDir.exists()) {
      for(File f : profilesDir.listFiles()) {
        if(f.isDirectory()) {
          String profileTitle = f.getName();
          File file = getFile(profileTitle);
          if(file.exists()) {
            try {
              T content = readFile(file);
              cache.put(profileTitle, content);
              
              Date fileTimestamp = new Date(file.lastModified());
              if(timestamps.get(profileTitle) == null || fileTimestamp.after(timestamps.get(profileTitle))) {
                timestamps.put(profileTitle, fileTimestamp);
              }
            } catch (IOException e) {
              log.error(e, e);
            }
          }
        }
      } 
    }
    initialized = true;
  }
  
  public void save(String profileTitle, T content) {
    try {
      lock.writeLock().lock();
      if(!initialized) {
        initialize();
      }
      cache.put(profileTitle, content);
      File file = getFile(profileTitle);
      writeFile(file, content);
      timestamps.put(profileTitle, new Date());
    } catch (IOException e) {
      log.error(e, e);
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public T get(String profileTitle) {
    try {
      lock.readLock().lock();
      if(!initialized) {
        initialize();
      }
      return cache.get(profileTitle);
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
  
  public void delete(String profileTitle) {
    try {
      lock.writeLock().lock();
      if(!initialized) {
        initialize();
      }
      cache.remove(profileTitle);
      deleteFile(getFile(profileTitle));
      timestamps.put(profileTitle, new Date());
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public Date getTimestamp(String profileTitle) {
    return timestamps.get(profileTitle);
  }
  
  private File getFile(String profileTitle) {
    return new File(config.getProfilesDir(), profileTitle + File.separator + getFileName());
  }
}

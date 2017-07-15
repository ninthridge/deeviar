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

public abstract class CachedOwnerMultiJsonRepository<T> extends BaseJsonRepository<T> {

  protected final Log log = LogFactory.getLog(getClass());
  
  protected Map<String, Map<String, T>> cache = new HashMap<>();
  protected Map<String, Date> timestamps = new HashMap<>();
  
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  
  private boolean initialized = false;
  
  protected abstract String getDirName();
  protected abstract String getId(T content);
  
  public CachedOwnerMultiJsonRepository(TypeReference<T> typeReference) {
    super(typeReference);
  }
  
  protected void initialize() {
    File profilesDir = config.getProfilesDir();
    if(profilesDir != null && profilesDir.exists()) {
      for(File f : profilesDir.listFiles()) {
        if(f.isDirectory()) {
          String profileTitle = f.getName();
          File dir = getDir(profileTitle);
          if(dir != null && dir.exists()) {
            Map<String, T> map = new HashMap<>();
            for(File file : dir.listFiles(jsonFileFilter)) {
              try {
                T content = readFile(file);
                String id = getId(content);
                map.put(id,  content);
                
                Date fileTimestamp = new Date(file.lastModified());
                if(timestamps.get(profileTitle) == null || fileTimestamp.after(timestamps.get(profileTitle))) {
                  timestamps.put(profileTitle, fileTimestamp);
                }
              } catch (IOException e) {
                log.error(e, e);
              }
            }
            cache.put(profileTitle, map);
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
      if(cache.get(profileTitle) == null) {
        cache.put(profileTitle, new HashMap<String, T>());
      }
      
      cache.get(profileTitle).put(getId(content), content);
      File file = fileName(profileTitle, getId(content));
      writeFile(file, content);
      timestamps.put(profileTitle, new Date());
    } catch (IOException e) {
      log.error(e, e);
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public void delete(String profileTitle, String id) {
    try {
      lock.writeLock().lock();
      if(!initialized) {
        initialize();
      }
      if(cache.get(profileTitle) != null) {
        cache.get(profileTitle).remove(id);
        deleteFile(fileName(profileTitle, id));
        timestamps.put(profileTitle, new Date());
      }
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public T get(String profileTitle, String id) {
    try {
      lock.readLock().lock();
      if(!initialized) {
        initialize();
      }
      if(cache.get(profileTitle) != null) {
        return cache.get(profileTitle).get(id);
      }
    } finally {
      lock.readLock().unlock();
    }
    return null;
  }
  
  public SortedSet<T> getAll(String profileTitle) {
    try {
      lock.readLock().lock();
      if(!initialized) {
        initialize();
      }
      SortedSet<T> set = new TreeSet<>();
      if(cache.get(profileTitle) != null) {
        for(String key : cache.get(profileTitle).keySet()) {
          set.add(cache.get(profileTitle).get(key));
        }
      }
      return set;
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public Date getTimestamp(String profileTitle) {
    return timestamps.get(profileTitle);
  }
  
  private File fileName(String profileTitle, String id) {
    return new File(getDir(profileTitle), id + ".json");
  }
  
  private File getDir(String profileTitle) {
    return new File(config.getProfilesDir(), profileTitle + File.separator + getDirName()); 
  }
}

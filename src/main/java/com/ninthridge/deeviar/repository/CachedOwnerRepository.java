package com.ninthridge.deeviar.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class CachedOwnerRepository<T> {

  protected final Log log = LogFactory.getLog(getClass());
  
  protected Map<String, T> cache = new HashMap<>();
  
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  
  public void save(String profileTitle, T content) {
    try {
      lock.writeLock().lock();
      cache.put(profileTitle, content);
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public T get(String profileTitle) {
    try {
      lock.readLock().lock();
      return cache.get(profileTitle);
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public void delete(String profileTitle) {
    try {
      lock.writeLock().lock();
      cache.remove(profileTitle);
    } finally {
      lock.writeLock().unlock();
    }
  }
}

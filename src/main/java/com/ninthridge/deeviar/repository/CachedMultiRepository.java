package com.ninthridge.deeviar.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class CachedMultiRepository<T> {

  protected final Log log = LogFactory.getLog(getClass());
  
  protected Map<String, T> cache = new HashMap<>();
  
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  
  protected abstract String getId(T content);
  
  public void save(T content) {
    try {
      lock.writeLock().lock();
      cache.put(getId(content), content);
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  public T get(String id) {
    try {
      lock.readLock().lock();
      return cache.get(id);
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public SortedSet<T> getAll() {
    try {
      lock.readLock().lock();
      SortedSet<T> set = new TreeSet<>();
      for(String key : cache.keySet()) {
        set.add(cache.get(key));
      }
      return set;
    } finally {
      lock.readLock().unlock();
    }
  }
  
  public void delete(String id) {
    try {
      lock.writeLock().lock();
      cache.remove(id);
    } finally {
      lock.writeLock().unlock();
    }
  }
}

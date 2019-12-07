/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

/**
 *
 * BlockingCache是阻塞版本的缓存装饰器，它会保证只有一个线程到缓存中查找指定key对应的数据。
 * 即：
 *    假设线程A在BlockingCache中未查找到keyA对应的缓存项时，线程A会获取keyA对应的锁，这样后续线程在查找
 *    keyA时会发生阻塞。
 *
 * Simple blocking decorator
 *
 * Simple and inefficient version of EhCache's BlockingCache decorator.
 * It sets a lock over a cache key when the element is not found in cache.
 * This way, other threads will wait until this element is filled instead of hitting the database.
 *
 * @author Eduardo Macarron
 *
 */
public class BlockingCache implements Cache {

  /**
   * 阻塞超时时间
   */
  private long timeout;
  /**
   * 被装饰的底层Cache对象
   */
  private final Cache delegate;
  /**
   * 每个key都有对应的ReentrantLock(重入锁)
   */
  private final ConcurrentHashMap<Object, ReentrantLock> locks;

  public BlockingCache(Cache delegate) {
    this.delegate = delegate;
    this.locks = new ConcurrentHashMap<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public void putObject(Object key, Object value) {
    try {
      //向缓存中添加缓存项
      delegate.putObject(key, value);
    } finally {
      //释放锁
      releaseLock(key);
    }
  }

  /**
   * 获取指定key对应的对象
   * @param key The key 要查询的key
   * @return    key所对应的对象
   */
  @Override
  public Object getObject(Object key) {
    //获取该key对应的锁
    acquireLock(key);
    Object value = delegate.getObject(key);
    if (value != null) {
      //释放锁
      releaseLock(key);
    }
    return value;
  }

  @Override
  public Object removeObject(Object key) {
    // despite of its name, this method is called only to release locks
    releaseLock(key);
    return null;
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  /**
   * 获取锁，如果该key没有对应的锁对象则为其创建一个ReentrantLock对象
   * @param key
   * @return
   */
  private ReentrantLock getLockForKey(Object key) {
    return locks.computeIfAbsent(key, k -> new ReentrantLock());
  }

  /**
   * 尝试获取指定key对应的锁：
   *    如果该key没有对应的锁对象则为其创建一个ReetrantLock对象，再加锁；
   *    如果获取锁失败，则阻塞一段时间。
   * @param key
   */
  private void acquireLock(Object key) {
    //获取ReentrantLock
    Lock lock = getLockForKey(key);
    if (timeout > 0) {
      try {
        //获取锁带超时时长
        boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        //超时则抛出异常
        if (!acquired) {
          throw new CacheException("Couldn't get a lock in " + timeout + " for the key " +  key + " at the cache " + delegate.getId());
        }
      } catch (InterruptedException e) {
        throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
      }
    } else {
      //获取锁，不带超时时长
      lock.lock();
    }
  }

  /**
   * 释放指定key的锁
   * @param key 指定key
   */
  private void releaseLock(Object key) {
    ReentrantLock lock = locks.get(key);
    //锁是否被当前线程持有
    if (lock.isHeldByCurrentThread()) {
      //释放锁
      lock.unlock();
    }
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}

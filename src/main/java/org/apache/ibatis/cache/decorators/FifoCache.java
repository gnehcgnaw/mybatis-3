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

import java.util.Deque;
import java.util.LinkedList;

import org.apache.ibatis.cache.Cache;

/**
 * 在很多场景下，为了控制缓存的大小，系统需要按照一定的规则清理缓存。FifoCache是先进先出版本的装饰器，
 * 当向缓存添加数据时，如果缓存项中的个数已经达到了上线，则会将缓存中最老（即最早进入缓存）的缓存项删除。
 * FIFO (first in, first out) cache decorator.
 *
 * @author Clinton Begin
 */
public class FifoCache implements Cache {

  /**
   * 底层被装饰的底层Cache对象
   */
  private final Cache delegate;
  /**
   * 用于记录key进入缓存的先后顺序，使用的是LinkedList<Object>类型的集合对象
   */
  private final Deque<Object> keyList;
  /**
   * 记录缓存项的上线，超过该值，则需要清理最老的缓存项
   */
  private int size;

  public FifoCache(Cache delegate) {
    this.delegate = delegate;
    this.keyList = new LinkedList<>();
    this.size = 1024;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.size = size;
  }


  @Override
  public void putObject(Object key, Object value) {
    //检测并清理缓存
    cycleKeyList(key);
    //条件缓存项
    delegate.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyList.clear();
  }

  private void cycleKeyList(Object key) {
    //记录key
    keyList.addLast(key);
    //如果达到缓存上线，则清理最老的缓存项
    if (keyList.size() > size) {
      Object oldestKey = keyList.removeFirst();
      delegate.removeObject(oldestKey);
    }
  }

}

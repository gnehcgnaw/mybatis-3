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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.cache.Cache;

/**
 * LruCache是按照近期最少使用算法（Least Recently Used , LRU）进行缓存清理的装饰器，在需要清理换成时，它会清除最近最少使用的缓存项。
 * Lru (least recently used) cache decorator.
 * @see red.reksai.javabase.LinkedHashMapTest 连接LinkHashMap的特性
 *
 * @author Clinton Begin
 */
public class LruCache implements Cache {

  /**
   * 被装饰的底层Cache对象
   */
  private final Cache delegate;
  /**
   * LinkedHashMap<Object,Object> 类型对象，它是一个有序的HashMap，用于记录key最近的使用情况
   */
  private Map<Object, Object> keyMap;
  /**
   * 记录最少被使用的缓存项的key
   */
  private Object eldestKey;

  public LruCache(Cache delegate) {
    this.delegate = delegate;
    //设置默认的缓存大小为1024，我们也可以调用setSize()进行重新设置
    setSize(1024);
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  /**
   * 重置设置缓存大小，
   * @param size
   */
  public void setSize(final int size) {
    //注意LinkedHashMap构造函数的第三个参数，true表示该LinkedHashMap记录的属性是access-order，也就是说LinkedHashMap.get()方法会百变器记录的顺序
    keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
      private static final long serialVersionUID = 4267176411845948333L;
      //当调用LinkedHashMap.put()方法时，会调用该方法
      @Override
      protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
        //判断hashMap的长度是否大于限定的缓存长度
        boolean tooBig = size() > size;
        //如果达到缓存上限，后面会删除该项
        if (tooBig) {
          //获取需要删除的key
          eldestKey = eldest.getKey();
        }
        return tooBig;
      }
    };
  }

  @Override
  public void putObject(Object key, Object value) {
    //添加缓存项
    delegate.putObject(key, value);
    //删除最久未使用的缓存项
    cycleKeyList(key);
  }

  @Override
  public Object getObject(Object key) {
    //修改LinkedHashMap中记录的顺序
    keyMap.get(key);
    //返回查询的对象
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyMap.clear();
  }

  private void cycleKeyList(Object key) {
    keyMap.put(key, key);
    //eldestKey不为空表示已达到缓存上限
    if (eldestKey != null) {
      //删除最久未被使用的缓存项
      delegate.removeObject(eldestKey);
      eldestKey = null;
    }
  }

}

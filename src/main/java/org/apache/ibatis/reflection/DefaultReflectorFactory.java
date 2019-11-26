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
package org.apache.ibatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * mybatis提供的默认的{@link ReflectorFactory}的实现，
 *    除了mybatis自己提供的这个实现外，我们还可以在mybatis-config.xml中指定自定义的ReflectorFactory的实现，从而实现功能上的扩展。
 *    在介绍mybatis初始化流程时，会提到该扩展点。
 */
public class DefaultReflectorFactory implements ReflectorFactory {
  /**
   * 该字段决定是否开启对Reflector对象的缓存
   */
  private boolean classCacheEnabled = true;
  /**
   * 使用ConcurrentMap集合实现对Reflector的缓存
   */
  private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

  public DefaultReflectorFactory() {
  }

  @Override
  public boolean isClassCacheEnabled() {
    return classCacheEnabled;
  }

  @Override
  public void setClassCacheEnabled(boolean classCacheEnabled) {
    this.classCacheEnabled = classCacheEnabled;
  }

  /**
   * 为指定的类创建Reflector对象，并将Reflector对象缓存到reflectorMap中
   * @param type
   * @return
   */
  @Override
  public Reflector findForClass(Class<?> type) {
    //检测是否开启缓存
    if (classCacheEnabled) {
      //如果可以拿到缓存，就直接放回，如果不能，就先创建，然后放入map，最后再取出
      // synchronized (type) removed see issue #461
      return reflectorMap.computeIfAbsent(type, Reflector::new);
    } else {
      //未开启缓存，则直接创建并返回Reflector对象
      return new Reflector(type);
    }
  }

}

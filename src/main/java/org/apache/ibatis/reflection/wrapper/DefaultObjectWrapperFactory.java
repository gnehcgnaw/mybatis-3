/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;

/**
 * Mybatis提供的{@link ObjectWrapperFactory}的默认实现，但是它实现的getWrapperFor()方法始终抛出异常，hasWrapperFor()始终返回false，
 * 所以该实现实际上是不可用的，但是与{@link org.apache.ibatis.reflection.factory.ObjectFactory}类似，我们可以在mybatis-config.xml
 * 中配置自定义的ObjectWrapperFactory实现类进行扩展
 * @author Clinton Begin
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {
  /**
   * 判断某类是否有包装类， 始终返回false
   * @param object
   * @return
   */
  @Override
  public boolean hasWrapperFor(Object object) {
    return false;
  }

  /**
   * 通过提供的类，和类的元数据，获取包装类，始终抛出异常
   * @param metaObject
   * @param object
   * @return
   */
  @Override
  public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
    throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
  }

}

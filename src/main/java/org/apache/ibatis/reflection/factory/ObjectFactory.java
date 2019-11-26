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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * Mybatis中有很多模块会使用到ObjectFactory接口，该接口提供了多个create()方法的重载，
 * 通过这些方create()方法可以创建指定类型的对象。
 * @see DefaultObjectFactory
 * MyBatis uses an ObjectFactory to create all needed new Objects.
 *
 * @author Clinton Begin
 */
public interface ObjectFactory {

  /**
   * 设置配置信息
   * Sets configuration properties.
   * @param properties configuration properties
   */
  default void setProperties(Properties properties) {
    /**
     *   空操纵：说白了，如果要继承ObjectFactory，不仅要定义Properties 属性，还有再提供一个getProperties()的方法
     *   e.g. {@link red.reksai.mybatissample.objectfactory.ExampleObjectFactory}（我自定义的测试用例）
     *   {@link org.apache.ibatis.submitted.global_variables_defaults.SupportClasses.CustomObjectFactory}(官方测试用例)
     *   同时在mybatis-config.xml添加如下配置：
     *   <objectFactory type="org.mybatis.example.ExampleObjectFactory">
     *       <property name="someProperty" value="100"/>
     *   </objectFactory>
     *   这样可以拿到property的值。
     */
    // NOP
  }

  /**
   * 通过无参构造器创建指定类的对象
   * Creates a new object with default constructor.
   * @param type Object type
   * @return
   */
  <T> T create(Class<T> type);

  /**
   * 根据参数列表，从指定的类型中选择合适的构造器创建对象
   * Creates a new object with the specified constructor and params.
   * @param type Object type
   * @param constructorArgTypes Constructor argument types
   * @param constructorArgs Constructor argument values
   * @return
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  /**
   * 检测指定类型是否是集合类型，主要处理java.util.Collection及其子类
   * Returns true if this object can have a set of other objects.
   * It's main purpose is to support non-java.util.Collection objects like Scala collections.
   *
   * @param type Object type
   * @return whether it is a collection or not
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);

}

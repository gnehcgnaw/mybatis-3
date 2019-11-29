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
package org.apache.ibatis.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * 基础构建器抽象类
 *    为子类提供通用的工具类。
 * @author Clinton Begin
 */
public abstract class BaseBuilder {
  /**
   * Configuration对象是Mybatis初始化过程中的核心对象，Mybatis中几乎所有配置信息都会保存到Configuration对象中。
   * Configuration对象是Mybatis初始化过程中创建的且是全局唯一的。
   * 也有人称它是一个“All in One” 对象
   */
  protected final Configuration configuration;
  /**
   * 在mybatis-config.xml配置文件中可以使用<typeAliases></typeAliases>标签定义别名，这些定义的别名都会记录在TypeAliasesRegistry对象中
   */
  protected final TypeAliasRegistry typeAliasRegistry;
  /**
   * 在mybatis-config.xml配置文件中可以使用<typeHandler></typeHandler>标签定义添加的自定义的TypeHandler，
   * 完成指定数据库类型与Java类型的转换，这些TypeHandler都会记录在TypeHandlerRegistry中
   */
  protected final TypeHandlerRegistry typeHandlerRegistry;

  /**
   * 构造方法
   * @param configuration
   */
  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
  }

  /**
   * 获取Configuration对象
   * @return
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * 我没有发现这个方法有什么用，我在stackoverflow上给出了提问
   *  https://stackoverflow.com/questions/58987197/in-mybatis-basebuilderparseexpressionstring-regex-string-defaultvalue-is-th
   * 创建正则表达式
   * @param regex 指定表达式
   * @param defaultValue  默认表达式
   * @return  正则表达式
   */
  protected Pattern parseExpression(String regex, String defaultValue) {
    return Pattern.compile(regex == null ? defaultValue : regex);
  }

  /**
   *  将字符串转化成对应的数据类型的值
   * @param value
   * @param defaultValue
   * @return
   */
  protected Boolean booleanValueOf(String value, Boolean defaultValue) {
    return value == null ? defaultValue : Boolean.valueOf(value);
  }
  /**
   *  将字符串转化成对应的数据类型的值
   * @param value
   * @param defaultValue
   * @return
   */
  protected Integer integerValueOf(String value, Integer defaultValue) {
    return value == null ? defaultValue : Integer.valueOf(value);
  }
  /**
   *  将字符串转化成对应的数据类型的值
   * @param value
   * @param defaultValue
   * @return
   */
  protected Set<String> stringSetValueOf(String value, String defaultValue) {
    value = value == null ? defaultValue : value;
    return new HashSet<>(Arrays.asList(value.split(",")));
  }

  /**
   * 解析对应的jdbcType类型
   * @param alias
   * @return
   */
  protected JdbcType resolveJdbcType(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return JdbcType.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
    }
  }

  /**
   * 解析对应的 ResultSetType 类型
   * @param alias
   * @return
   */
  protected ResultSetType resolveResultSetType(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return ResultSetType.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
    }
  }

  /**
   * 解析对应的 ParameterMode 类型
   * @param alias
   * @return
   */
  protected ParameterMode resolveParameterMode(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return ParameterMode.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
    }
  }

  /**
   * 创建指定对象
   * @param alias
   * @return
   */
  protected Object createInstance(String alias) {
    //获取对应的类型
    Class<?> clazz = resolveClass(alias);
    if (clazz == null) {
      return null;
    }
    try {
      //创建对象
      return resolveClass(alias).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new BuilderException("Error creating instance. Cause: " + e, e);
    }
  }

  /**
   * 获得对应的类型
   * @param alias
   * @param <T>
   * @return
   */
  protected <T> Class<? extends T> resolveClass(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return resolveAlias(alias);
    } catch (Exception e) {
      throw new BuilderException("Error resolving class. Cause: " + e, e);
    }
  }

  protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
    if (typeHandlerAlias == null) {
      return null;
    }
    //得到TypeHandler对象
    Class<?> type = resolveClass(typeHandlerAlias);
    /**
     * isAssignableFrom()方法与instanceof关键字的区别总结为以下两个点：
     *
     *  isAssignableFrom()方法是从类继承的角度去判断，instanceof关键字是从实例继承的角度去判断。
     *  isAssignableFrom()方法是判断是否为某个类的父类，instanceof关键字是判断是否某个类的子类。
     */
    if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
      throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface");
    }
    @SuppressWarnings("unchecked") // already verified it is a TypeHandler
    Class<? extends TypeHandler<?>> typeHandlerType = (Class<? extends TypeHandler<?>>) type;
    return resolveTypeHandler(javaType, typeHandlerType);
  }

  /**
   * 从 typeHandlerRegistry 中获得或创建对应的 TypeHandler 对象
   * @param javaType
   * @param typeHandlerType
   * @return
   */
  protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
    if (typeHandlerType == null) {
      return null;
    }
    // javaType ignored for injected handlers see issue #746 for full detail
    TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
    if (handler == null) {
      // not in registry, create a new one
      handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
    }
    return handler;
  }

  /**
   * 获取一个对象
   * @param alias
   * @param <T>
   * @return
   */
  protected <T> Class<? extends T> resolveAlias(String alias) {
    return typeAliasRegistry.resolveAlias(alias);
  }
}

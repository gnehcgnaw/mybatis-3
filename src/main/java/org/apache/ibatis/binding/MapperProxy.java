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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * 当前类和Mybatis3.4有很大的差别：
 *    在Mybatis3.4版本中java.lang.reflect.*的方法来进行对象的动态代理的，没有配合java.lang.invoke中的{@link MethodHandle}
 *    在Mybatis3.5版本的时候，当前类定义了{@link MapperProxy.MapperMethodInvoker}借口，并引入了{@link MethodHandle}
 *  这么做的原因是为了提供框架的性能和安全性。
 *    具体请参看：java官方给出的解释：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandle.html
 * 因为MapperProxy实现了InvocationHandler接口，那么该类的实现就是代理对象的核心逻辑
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -4724728412955527868L;
  /**
   * MethodHandles.Lookup 允许查找的模式
   */
  private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
      | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;
  /**
   * MethodHandles.Lookup的Constructor对象，针对Java8
   */
  private static final Constructor<Lookup> lookupConstructor;
  /**
   * 针对Java9
   */
  private static final Method privateLookupInMethod;
  /**
   * 记录了关联的SqlSession对象
   */
  private final SqlSession sqlSession;
  /**
   * mapperInterface接口对应的class对象
   */
  private final Class<T> mapperInterface;
  /**
   * 缓存
   *    key 是 mapperInterface接口中某方法对应的Method对象；
   *    value 是 对应的MappedMethodInvoker
   */
  private final Map<Method, MapperMethodInvoker> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  static {
    Method privateLookupIn;
    try {
      // privateLookupIn 是java9中才有的（该方法可以模拟目标类上所有受支持的字节码行为，包括私有访问）
      // 参见：https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/MethodHandles.html
      privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
    } catch (NoSuchMethodException e) {
      privateLookupIn = null;
    }
    privateLookupInMethod = privateLookupIn;

    Constructor<Lookup> lookup = null;
    //判断privateLookupInMethod是不是为空，如果为空表明当前的jdk版本低于jdk9，大于等于jdk1.7，
    //因为MethodHandles是jdk1.7才提供的功能
    if (privateLookupInMethod == null) {
      // JDK 1.8
      try {
        //获取MethodHandles.Lookup中参数列表为(Class<?> lookupClass, int allowedModes)的构造器
        lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        //将构造器设置为可访问的
        lookup.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
            e);
      } catch (Exception e) {
        lookup = null;
      }
    }
    lookupConstructor = lookup;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {

      if (Object.class.equals(method.getDeclaringClass())) {
        //如果当前方法是Object中的方法，直接invoke就可以了
        return method.invoke(this, args);
      } else {
        //如果当前方法不属于Object对象的方法，
        //那么：
        //    1. 从缓存中查找
        //    2. 调用invoke方法进行执行
        // 参见： red.reksai.javabase.MethodHandlesTest
        return cachedInvoker(proxy, method, args).invoke(proxy, method, args, sqlSession);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  private MapperMethodInvoker cachedInvoker(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      //从缓存中查找method是否存在，如果不存在就创建一个并存入集合
      return methodCache.computeIfAbsent(method, m -> {
        //判断当前方法是不是default类型的方法
        if (m.isDefault()) {
          //是default类型的方法
          try {
            if (privateLookupInMethod == null) {
              //Java8
              return new DefaultMethodInvoker(getMethodHandleJava8(method));
            } else {
              //Java9
              return new DefaultMethodInvoker(getMethodHandleJava9(method));
            }
          } catch (IllegalAccessException | InstantiationException | InvocationTargetException
              | NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        } else {
          //不是default类型的方法，就创建一个PlainMethodInvoker对象，并返回
          return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
        }
      });
    } catch (RuntimeException re) {
      Throwable cause = re.getCause();
      throw cause == null ? re : cause;
    }
  }

  private MethodHandle getMethodHandleJava9(Method method)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Class<?> declaringClass = method.getDeclaringClass();
    return ((Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup())).findSpecial(
        declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
        declaringClass);
  }

  private MethodHandle getMethodHandleJava8(Method method)
      throws IllegalAccessException, InstantiationException, InvocationTargetException {
    final Class<?> declaringClass = method.getDeclaringClass();
    return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
  }

  /**
   * MapperMethod调用
   */
  interface MapperMethodInvoker {
    Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
  }

  /**
   * 普通方法调用
   */
  private static class PlainMethodInvoker implements MapperMethodInvoker {
    //创建MapperMethod对象
    private final MapperMethod mapperMethod;

    public PlainMethodInvoker(MapperMethod mapperMethod) {
      super();
      this.mapperMethod = mapperMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      //调用MapperMethod.execute()进行方法的执行
      return mapperMethod.execute(sqlSession, args);
    }
  }

  /**
   * 默认方法调用
   */
  private static class DefaultMethodInvoker implements MapperMethodInvoker {
    private final MethodHandle methodHandle;

    public DefaultMethodInvoker(MethodHandle methodHandle) {
      super();
      this.methodHandle = methodHandle;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      return methodHandle.bindTo(proxy).invokeWithArguments(args);
    }
  }
}

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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.ibatis.reflection.invoker.AmbiguousMethodInvoker;
import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.invoker.SetFieldInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

/**
 * {@link Reflector}是mybatis中反射模块的基础，每个Reflector都对应一个类，在Reflector中缓存了反射操作需要使用的类的元信息。
 *
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 *
 * @author Clinton Begin
 */
public class Reflector {

  /**
   * 对应类的类型
   */
  private final Class<?> type;
  /**
   * 可读属性的名称集合，可读属性就是存在相应的getter方法的属性，初始值为空数组
   */
  private final String[] readablePropertyNames;
  /**
   * 可写属性的名称集合，可写属性就是存在相应setter方法的属性，初始值为空数组
   */
  private final String[] writablePropertyNames;
  /**
   * 记录了属性相应的setter方法，key是属性名称，value是Invoke对象，它是对setter方法对应Method对象的封装，后面会详细介绍
   */
  private final Map<String, Invoker> setMethods = new HashMap<>();
  /**
   * 记录了属性相应的getter方法集合，key是属性名称，value是Invoke对象，
   */
  private final Map<String, Invoker> getMethods = new HashMap<>();
  /**
   * 记录了属性相应的setter方法的参数值类型，key是属性名称，value是setter方法的参数类型
   */
  private final Map<String, Class<?>> setTypes = new HashMap<>();
  /**
   * 记录了属性相应的getter方法的返回值类型，key是属性名称，value是getter方法的返回值类型
   */
  private final Map<String, Class<?>> getTypes = new HashMap<>();
  /**
   * 记录了默认的构造方法
   */
  private Constructor<?> defaultConstructor;
  /**
   * 记录了所有属性名称的集合
   */
  private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

  /**
   * 此构造方法中会解析指定的Class对象，并填充上述集合
   * @param clazz 需要解析的Class对象
   */
  public Reflector(Class<?> clazz) {
    //初始化type字段
    type = clazz;
    //查找clazz的默认构造方法（无参构造方法），具体实现是通过反射遍历所有构造方法
    addDefaultConstructor(clazz);
    //处理clazz中的getter方法，填充getMethods集合和getTypes集合
    addGetMethods(clazz);
    //处理clazz中的setter方法，填充setMethods集合和setTypes集合
    addSetMethods(clazz);
    //处理没有getter/setter方法的字段
    addFields(clazz);
    //根据getMethods和setMethods集合，初始化可读、可写属性的名称集合
    readablePropertyNames = getMethods.keySet().toArray(new String[0]);
    writablePropertyNames = setMethods.keySet().toArray(new String[0]);
    //初始化caseInsensitivePropertyMap，其中记录了所有大写格式的属性名称
    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }

  private void addDefaultConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    Arrays.stream(constructors).filter(constructor -> constructor.getParameterTypes().length == 0)
      .findAny().ifPresent(constructor -> this.defaultConstructor = constructor);
  }

  /**
   * 负责解析类中的get方法
   * @param clazz
   */
  private void addGetMethods(Class<?> clazz) {
    Map<String, List<Method>> conflictingGetters = new HashMap<>();
    /**
     * 1. 调用{@link Reflector#getClassMethods(Class)} 方法获取当前类以及其父类中定义的所有方法的唯一签名以及相应的Method对象。
     */
    Method[] methods = getClassMethods(clazz);
    /**
     * 2. 按照JavaBean的规范，从Reflector#getClassMethods(Class)方法返回的Methods数组中查找该类中定义的getter方法，
     *    将其记录在conflictingGetters集合中，conflictingGetters集合（ Map<String, List<Method>>类型）的key为属性名称，value是该属性对应的getter方法集合。
     *
     *    2.1. 具体步骤
     *      2.1.1. 得到所有的get方法，（参数类别为空，标志是get的方法）；
     *      2.1.2. 将得到的get方法添加到方法冲突集合中；
     *          例如：父类 public List<User> getUserList(); 子类 public ArrayList<User> getUserList();
     *               在进行{@link Reflector#getClassMethods(Class)}中的{@link Reflector#getSignature(Method)}返回结果是：
     *               java.util.List#getUserList和java.util.ArrayList#getUserList，即得到两个方法签名，在{@link Reflector#addUniqueMethods(Map, Method[])}
     *               方法中会被认为是两个不同的方法添加到 uniqueMethods集合中，这显然不是我们想要的结果。
     *
     *  所以后续步骤3 会去解决这种Getter方法的冲突。
     *
     *  (lambda表达式 :filter forEach )
     */
    Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 0 && PropertyNamer.isGetter(m.getName()))
      .forEach(m -> addMethodConflict(conflictingGetters, PropertyNamer.methodToProperty(m.getName()), m));
    /**
     * 3. 解决Getter冲突
     *   1. 为什么会产生冲突呢？
     *      步骤2已经解释过为什么会产生冲突了。
     *   2. 解决方式是什么？
     */
    resolveGetterConflicts(conflictingGetters);
  }

  /**
   * 解决get方法的冲突，同时会将处理得到的getter方法记录到getMethods集合中，并将其返回值类型填充到getTypes集合中
   * @param conflictingGetters
   */
  private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
    //遍历conflictingGetters集合
    for (Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
      //优胜Method对象
      Method winner = null;
      //方法名称
      String propName = entry.getKey();
      boolean isAmbiguous = false;
      // candidate 候选Method对象
      for (Method candidate : entry.getValue()) {
        //如果优胜对象为空，这时候将候选对象复制给优胜对象
        if (winner == null) {
          winner = candidate;
          // continue是跳过当次循环中剩下的语句，执行下一次循环
          continue;
        }
        //获取优胜者返回值类型
        Class<?> winnerType = winner.getReturnType();
        //获取候选者返回值类型
        Class<?> candidateType = candidate.getReturnType();
        /**
         * 如果返回值类型相同，就要判断返回值是不是boolean？为什么要判断是不是boolean呢？
         */
        if (candidateType.equals(winnerType)) {
          //如果返回值不是boolean直接
          if (!boolean.class.equals(candidateType)) {
            isAmbiguous = true;
            //break只能跳出1层循环
            break;
          } else if (candidate.getName().startsWith("is")) {
            winner = candidate;
          }
        }
        /**
         * @see red.reksai.javabase.IsAssignableFromTest
         * 判断返回值类型有三种情况：
         *  1. 候选者是优胜者的父类，不做任何操作，最终返回子类就行
         *  2. 优胜者是候选者的父类，这时候先要将候选者赋值给优胜者，然后返回
         *  3. 返回值相同，二义性，
         */
        else if (candidateType.isAssignableFrom(winnerType)) {
          // OK getter type is descendant
        } else if (winnerType.isAssignableFrom(candidateType)) {
          winner = candidate;
        } else {
          isAmbiguous = true;
          break;
        }
      }
      //该字段只有一个getter方法，直接添加到getMethods集合并填充getTypes集合
      addGetMethod(propName, winner, isAmbiguous);
    }
  }

  private void addGetMethod(String name, Method method, boolean isAmbiguous) {
    /**
     * 验证：
     *    1. 如果有含糊不清的直接报错，
     *    2. 如果验证通过，则进行方法的封装
     */
    MethodInvoker invoker = isAmbiguous
        ? new AmbiguousMethodInvoker(method, MessageFormat.format(
            "Illegal overloaded getter method with ambiguous type for property ''{0}'' in class ''{1}''. This breaks the JavaBeans specification and can cause unpredictable results.",
            name, method.getDeclaringClass().getName()))
        : new MethodInvoker(method);

    getMethods.put(name, invoker);
    /**
     * 获取返回值的Type ，{@link TypeParameterResolver}
     */
    Type returnType = TypeParameterResolver.resolveReturnType(method, type);
    getTypes.put(name, typeToClass(returnType));
  }

  private void addSetMethods(Class<?> clazz) {
    Map<String, List<Method>> conflictingSetters = new HashMap<>();
    Method[] methods = getClassMethods(clazz);
    Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 1 && PropertyNamer.isSetter(m.getName()))
      .forEach(m -> addMethodConflict(conflictingSetters, PropertyNamer.methodToProperty(m.getName()), m));
    resolveSetterConflicts(conflictingSetters);
  }

  /**
   * 添加方法冲突
   * @param conflictingMethods
   * @param name
   * @param method
   *
   */
  private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
    if (isValidPropertyName(name)) {
      /**
       * {@link Map#computeIfAbsent(Object, Function)}
       * @see red.reksai.reflection.ComputeIfAbsentTest   map jdk1.8新特性
       */
      List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
      list.add(method);
    }
  }

  private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
    for (String propName : conflictingSetters.keySet()) {
      List<Method> setters = conflictingSetters.get(propName);
      Class<?> getterType = getTypes.get(propName);
      boolean isGetterAmbiguous = getMethods.get(propName) instanceof AmbiguousMethodInvoker;
      boolean isSetterAmbiguous = false;
      Method match = null;
      for (Method setter : setters) {
        if (!isGetterAmbiguous && setter.getParameterTypes()[0].equals(getterType)) {
          // should be the best match
          match = setter;
          break;
        }
        if (!isSetterAmbiguous) {
          match = pickBetterSetter(match, setter, propName);
          isSetterAmbiguous = match == null;
        }
      }
      if (match != null) {
        addSetMethod(propName, match);
      }
    }
  }

  private Method pickBetterSetter(Method setter1, Method setter2, String property) {
    if (setter1 == null) {
      return setter2;
    }
    Class<?> paramType1 = setter1.getParameterTypes()[0];
    Class<?> paramType2 = setter2.getParameterTypes()[0];
    if (paramType1.isAssignableFrom(paramType2)) {
      return setter2;
    } else if (paramType2.isAssignableFrom(paramType1)) {
      return setter1;
    }
    MethodInvoker invoker = new AmbiguousMethodInvoker(setter1,
        MessageFormat.format(
            "Ambiguous setters defined for property ''{0}'' in class ''{1}'' with types ''{2}'' and ''{3}''.",
            property, setter2.getDeclaringClass().getName(), paramType1.getName(), paramType2.getName()));
    setMethods.put(property, invoker);
    Type[] paramTypes = TypeParameterResolver.resolveParamTypes(setter1, type);
    setTypes.put(property, typeToClass(paramTypes[0]));
    return null;
  }

  private void addSetMethod(String name, Method method) {
    MethodInvoker invoker = new MethodInvoker(method);
    setMethods.put(name, invoker);
    Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
    setTypes.put(name, typeToClass(paramTypes[0]));
  }

  private Class<?> typeToClass(Type src) {
    Class<?> result = null;
    if (src instanceof Class) {
      result = (Class<?>) src;
    } else if (src instanceof ParameterizedType) {
      result = (Class<?>) ((ParameterizedType) src).getRawType();
    } else if (src instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) src).getGenericComponentType();
      if (componentType instanceof Class) {
        result = Array.newInstance((Class<?>) componentType, 0).getClass();
      } else {
        Class<?> componentClass = typeToClass(componentType);
        result = Array.newInstance(componentClass, 0).getClass();
      }
    }
    if (result == null) {
      result = Object.class;
    }
    return result;
  }

  private void addFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (!setMethods.containsKey(field.getName())) {
        // issue #379 - removed the check for final because JDK 1.5 allows
        // modification of final fields through reflection (JSR-133). (JGB)
        // pr #16 - final static can only be set by the classloader
        int modifiers = field.getModifiers();
        if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
          addSetField(field);
        }
      }
      if (!getMethods.containsKey(field.getName())) {
        addGetField(field);
      }
    }
    if (clazz.getSuperclass() != null) {
      addFields(clazz.getSuperclass());
    }
  }

  private void addSetField(Field field) {
    if (isValidPropertyName(field.getName())) {
      setMethods.put(field.getName(), new SetFieldInvoker(field));
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
      setTypes.put(field.getName(), typeToClass(fieldType));
    }
  }

  private void addGetField(Field field) {
    if (isValidPropertyName(field.getName())) {
      getMethods.put(field.getName(), new GetFieldInvoker(field));
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
      getTypes.put(field.getName(), typeToClass(fieldType));
    }
  }

  /**
   * 检查是否有效的属性名称
   * @param name
   * @return
   */
  private boolean isValidPropertyName(String name) {
    return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
  }

  /**
   * This method returns an array containing all methods
   * declared in this class and any superclass.
   * We use this method, instead of the simpler <code>Class.getMethods()</code>,
   * because we want to look for private methods as well.
   *
   * @param clazz The class
   * @return An array containing all methods in this class
   */
  private Method[] getClassMethods(Class<?> clazz) {
    Map<String, Method> uniqueMethods = new HashMap<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      //记录currentClass这个类中定义的全部方法
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

      // we also need to look for interface methods -
      // because the class may be abstract
      // 记录接口中定义的方法
      Class<?>[] interfaces = currentClass.getInterfaces();
      for (Class<?> anInterface : interfaces) {
        addUniqueMethods(uniqueMethods, anInterface.getMethods());
      }
      //获取父类，继续while循环
      currentClass = currentClass.getSuperclass();
    }

    Collection<Method> methods = uniqueMethods.values();
    //转换成Methods方法数组返回
    return methods.toArray(new Method[0]);
  }

  /**
   * 为每个方法生成一个唯一签名，并记录到uniqueMethods集合中
   * @param uniqueMethods
   * @param methods
   */
  private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
    for (Method currentMethod : methods) {
      if (!currentMethod.isBridge()) {
        /**
         * 通过{@link Reflector#getSignature(Method)}方法得到方法的签名是：返回值类型#方法名称：参数类型列表。
         * 例如： Reflector.getSignature(Method)的方法签名是：java.lang.String#getSignature:java.lang.reflect.Method
         * 通过Reflector.getSignature(Method)方法得到的方法签名是全局唯一的，可以作为该方法的唯一标识
         */
        String signature = getSignature(currentMethod);
        // check to see if the method is already known
        // if it is known, then an extended class must have
        // overridden a method
        /**
         * 检查是否添加过该方法，如果添加过，就无须在向uniqueMethods中添加该方法了。
         * 其实这个段代码，如果细品有另一种意思：
         *    因为addUniqueMethods方法在{@link Reflector#getClassMethods(Class)}中的while循环被调用了，一次循环被调用两次，
         *    这两次调用：先是子类调用，然后是父类接口调用，
         * 所以这里检查是否添加过该方法的另一层含义是：
         *    检测是否在子类中已经添加过该方法，如果在子类中添加过，则表示子类覆盖了该方法，无须再向uniqueMethods集合中添加该方法了。
         *
         */
        if (!uniqueMethods.containsKey(signature)) {
          //记录该签名和方法的对应关系
          uniqueMethods.put(signature, currentMethod);
        }
      }
    }
  }

  /**
   * 生成方法签名
   * @param method
   * @return
   */
  private String getSignature(Method method) {
    StringBuilder sb = new StringBuilder();
    Class<?> returnType = method.getReturnType();
    if (returnType != null) {
      sb.append(returnType.getName()).append('#');
    }
    sb.append(method.getName());
    Class<?>[] parameters = method.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      sb.append(i == 0 ? ':' : ',').append(parameters[i].getName());
    }
    return sb.toString();
  }

  /**
   * Checks whether can control member accessible.
   *
   * @return If can control member accessible, it return {@literal true}
   * @since 3.5.0
   */
  public static boolean canControlMemberAccessible() {
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (null != securityManager) {
        securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
      }
    } catch (SecurityException e) {
      return false;
    }
    return true;
  }

  /**
   * Gets the name of the class the instance provides information for.
   *
   * @return The class name
   */
  public Class<?> getType() {
    return type;
  }

  public Constructor<?> getDefaultConstructor() {
    if (defaultConstructor != null) {
      return defaultConstructor;
    } else {
      throw new ReflectionException("There is no default constructor for " + type);
    }
  }

  public boolean hasDefaultConstructor() {
    return defaultConstructor != null;
  }

  public Invoker getSetInvoker(String propertyName) {
    Invoker method = setMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }

  public Invoker getGetInvoker(String propertyName) {
    Invoker method = getMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }

  /**
   * Gets the type for a property setter.
   *
   * @param propertyName - the name of the property
   * @return The Class of the property setter
   */
  public Class<?> getSetterType(String propertyName) {
    Class<?> clazz = setTypes.get(propertyName);
    if (clazz == null) {
      throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
    }
    return clazz;
  }

  /**
   * Gets the type for a property getter.
   *
   * @param propertyName - the name of the property
   * @return The Class of the property getter
   */
  public Class<?> getGetterType(String propertyName) {
    Class<?> clazz = getTypes.get(propertyName);
    if (clazz == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return clazz;
  }

  /**
   * Gets an array of the readable properties for an object.
   *
   * @return The array
   */
  public String[] getGetablePropertyNames() {
    return readablePropertyNames;
  }

  /**
   * Gets an array of the writable properties for an object.
   *
   * @return The array
   */
  public String[] getSetablePropertyNames() {
    return writablePropertyNames;
  }

  /**
   * Check to see if a class has a writable property by name.
   *
   * @param propertyName - the name of the property to check
   * @return True if the object has a writable property by the name
   */
  public boolean hasSetter(String propertyName) {
    return setMethods.keySet().contains(propertyName);
  }

  /**
   * Check to see if a class has a readable property by name.
   *
   * @param propertyName - the name of the property to check
   * @return True if the object has a readable property by the name
   */
  public boolean hasGetter(String propertyName) {
    return getMethods.keySet().contains(propertyName);
  }

  public String findPropertyName(String name) {
    return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
  }
}

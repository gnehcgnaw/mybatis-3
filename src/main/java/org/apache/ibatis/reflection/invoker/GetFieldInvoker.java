/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * {@link GetFieldInvoker}/{@link SetFieldInvoker} 通过filed字段封装了对应的Filed对象，
 * 两者的invoke()方法是通过调用{@link Field#get(Object)} / {@link Field#set(Object, Object)}方法实现的。
 * @author Clinton Begin
 */
public class GetFieldInvoker implements Invoker {
  private final Field field;

  public GetFieldInvoker(Field field) {
    this.field = field;
  }

  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException {
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      if (Reflector.canControlMemberAccessible()) {
        field.setAccessible(true);
        return field.get(target);
      } else {
        throw e;
      }
    }
  }

  @Override
  public Class<?> getType() {
    return field.getType();
  }
}

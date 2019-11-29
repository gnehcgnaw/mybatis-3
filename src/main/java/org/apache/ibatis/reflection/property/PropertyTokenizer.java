/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * `PropertyTokenizer`是一个属性分词器工具，它继承了`Iterator`接口，它可以迭代处理嵌套的多层表达式。
 * 由“`*`”和“`[]`”组成的表达式是由`PropertyTokenizer`进行解析的。
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
  // fullName = "order[0].items[0].name"
  // String[] split = {"order[0]" ,"item[0]" ,"name"}
  /**
   * 当前表达式的名称:
   * e.g. order
   *      items
   *      name
   */
  private String name;
  /**
   * 当前表达式的索引名
   * e.g. order[0]
   *      items[0]
   *      name
   */
  private final String indexedName;
  /**
   * 索引下标
   * e.g. [0]
   *      [0]
   *      null
   */
  private String index;
  /**
   * 子表达式
   * e.g. items[0].name
   *      name
   *      null
   */
  private final String children;

  /**
   * 解析表达式
   * @param fullname  要解析的表达式
   *                  e.g. order[0].items[0].name
   */
  public PropertyTokenizer(String fullname) {
    //查找"."的位置
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      //初始化name
      name = fullname.substring(0, delim);
      //初始化children
      children = fullname.substring(delim + 1);
    } else {
      name = fullname;
      children = null;
    }
    //初始化indexName
    indexedName = name;
    //查找"["的位置，如果存在，最后要把上面步骤赋值给name中的"[]"去掉
    delim = name.indexOf('[');
    if (delim > -1) {
      //初始化index
      index = name.substring(delim + 1, name.length() - 1);
      //重新赋值给name
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  /**
   * 判断是否还有children
   * @return
   */
  @Override
  public boolean hasNext() {
    return children != null;
  }

  /**
   * 继续解析孩子节点
   * @return
   */
  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}

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
package org.apache.ibatis.scripting.xmltags;

import java.util.Map;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.session.Configuration;

/**
 * <foreach>对应的SqlNode实现
 *
 * 在动态SQL语句中构建in条件语句的时候，通常需要对一个集合进行迭代，Mybatis提供了<foreach>标签实现该功能。
 * 在使用<foreach>标签迭代集合时，不仅可以使用集合的元素和索引值，还可以在循环的开始之前或结束之后添加指定的字符串，
 * 也允许在迭代的过程中添加指定的分隔符。
 * @author Clinton Begin
 */
public class ForEachSqlNode implements SqlNode {
  public static final String ITEM_PREFIX = "__frch_";

  /**
   * 用于判断循环的终止条件，ForEachSqlNode构造方法中会创建该对象
   */
  private final ExpressionEvaluator evaluator;
  /**
   * 迭代的集合表达式
   */
  private final String collectionExpression;
  /**
   * 记录了ForEachSqlNode节点的子节点
   */
  private final SqlNode contents;
  /**
   * 在循环开始前添加的字符串
   */
  private final String open;
  /**
   * 在循环结束后要添加的字符串
   */
  private final String close;
  /**
   * 在循环过程中，每项之间的分隔符
   */
  private final String separator;
  /**
   * index当前迭代的次数 ，item本次迭代的元素，
   *    如迭代的集合时map，则index是间，item是值
   */
  private final String item;
  private final String index;

  private final Configuration configuration;

  public ForEachSqlNode(Configuration configuration, SqlNode contents, String collectionExpression, String index, String item, String open, String close, String separator) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
    this.configuration = configuration;
  }

  @Override
  public boolean apply(DynamicContext context) {
    Map<String, Object> bindings = context.getBindings();
    final Iterable<?> iterable = evaluator.evaluateIterable(collectionExpression, bindings);
    if (!iterable.iterator().hasNext()) {
      return true;
    }
    boolean first = true;
    applyOpen(context);
    int i = 0;
    for (Object o : iterable) {
      DynamicContext oldContext = context;
      if (first || separator == null) {
        context = new PrefixedContext(context, "");
      } else {
        context = new PrefixedContext(context, separator);
      }
      int uniqueNumber = context.getUniqueNumber();
      // Issue #709
      if (o instanceof Map.Entry) {
        @SuppressWarnings("unchecked")
        Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) o;
        applyIndex(context, mapEntry.getKey(), uniqueNumber);
        applyItem(context, mapEntry.getValue(), uniqueNumber);
      } else {
        applyIndex(context, i, uniqueNumber);
        applyItem(context, o, uniqueNumber);
      }
      contents.apply(new FilteredDynamicContext(configuration, context, index, item, uniqueNumber));
      if (first) {
        first = !((PrefixedContext) context).isPrefixApplied();
      }
      context = oldContext;
      i++;
    }
    applyClose(context);
    context.getBindings().remove(item);
    context.getBindings().remove(index);
    return true;
  }

  private void applyIndex(DynamicContext context, Object o, int i) {
    if (index != null) {
      context.bind(index, o);
      context.bind(itemizeItem(index, i), o);
    }
  }

  private void applyItem(DynamicContext context, Object o, int i) {
    if (item != null) {
      context.bind(item, o);
      context.bind(itemizeItem(item, i), o);
    }
  }

  private void applyOpen(DynamicContext context) {
    if (open != null) {
      context.appendSql(open);
    }
  }

  private void applyClose(DynamicContext context) {
    if (close != null) {
      context.appendSql(close);
    }
  }

  private static String itemizeItem(String item, int i) {
    return ITEM_PREFIX + item + "_" + i;
  }

  /**
   * 负责处理#{}占位符，但它并未完全解析#{}
   */
  private static class FilteredDynamicContext extends DynamicContext {
    /**
     * 底层封装的DynamicContext对象
     */
    private final DynamicContext delegate;
    private final int index;
    private final String itemIndex;
    private final String item;

    public FilteredDynamicContext(Configuration configuration,DynamicContext delegate, String itemIndex, String item, int i) {
      super(configuration, null);
      this.delegate = delegate;
      this.index = i;
      this.itemIndex = itemIndex;
      this.item = item;
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    /**
     *
     * @param sql
     */
    @Override
    public void appendSql(String sql) {
      //创建GenericTokenParser解析器，注意这里匿名实现了TokenHandler对象
      GenericTokenParser parser = new GenericTokenParser("#{", "}", content -> {
        //对item进行处理
        //#{item} ---> #{__frch_item_1}
        String newContent = content.replaceFirst("^\\s*" + item + "(?![^.,:\\s])", itemizeItem(item, index));
        //对itemIndex进行处理
        if (itemIndex != null && newContent.equals(content)) {
          //例如： #{itemIndex} ---> #{__frch_itemIndex_1}
          newContent = content.replaceFirst("^\\s*" + itemIndex + "(?![^.,:\\s])", itemizeItem(itemIndex, index));
        }
        return "#{" + newContent + "}";
      });

      delegate.appendSql(parser.parse(sql));
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }

  }

  /**
   * 处理前缀
   */
  private class PrefixedContext extends DynamicContext {
    /**
     * 底层封装的DynamicContext对象
     */
    private final DynamicContext delegate;
    /**
     * 指定的前缀
     */
    private final String prefix;
    /**
     * 是否已经处理过前缀
     */
    private boolean prefixApplied;

    public PrefixedContext(DynamicContext delegate, String prefix) {
      super(configuration, null);
      this.delegate = delegate;
      this.prefix = prefix;
      this.prefixApplied = false;
    }

    public boolean isPrefixApplied() {
      return prefixApplied;
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public void appendSql(String sql) {
      //判断是否需要追加前缀
      if (!prefixApplied && sql != null && sql.trim().length() > 0) {
        //追加前缀
        delegate.appendSql(prefix);
        //表示已经处理前缀
        prefixApplied = true;
      }
      //追加SQL片段
      delegate.appendSql(sql);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }
  }

}

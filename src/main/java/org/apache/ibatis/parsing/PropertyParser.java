/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.parsing;

import java.util.Properties;

/**
 * 属性解析器
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * 在mybatis-config.xml中的<properties></properties>节点下配置是否开启默认值功能的对应配置项
   * The special property key that indicate whether enable a default value on placeholder.
   * <p>
   *   The default value is {@code false} (indicate disable a default value on placeholder)
   *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * 配置占位符与默认值之间的默认分隔符的对应配置项
   * The special property key that specify a separator for key and default value on placeholder.
   * <p>
   *   The default separator is {@code ":"}.
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  /**
   * 默认情况下，关闭默认值选项
   */
  private static final String ENABLE_DEFAULT_VALUE = "false";
  /**
   * 默认分隔符是冒号
   */
  private static final String DEFAULT_VALUE_SEPARATOR = ":";

  private PropertyParser() {
    // Prevent Instantiation
  }

  /**
   * 当前方法中会创建{@link GenericTokenParser}解析器，并将默认值的处理委托给{@link GenericTokenParser#parse(String)}
   * @param string
   * @param variables
   * @return
   */
  public static String parse(String string, Properties variables) {
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    //创建GenericTokenParser对象，并指定其处理的占位符格式为"${}"
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    return parser.parse(string);
  }

  /**
   * 私有静态内部类
   */
  private static class VariableTokenHandler implements TokenHandler {
    /**
     * <properties><properties/>节点下定义的键值对，用于替换占位符
     */
    private final Properties variables;
    /**
     * 是否支持占位符中使用默认值的功能
     */
    private final boolean enableDefaultValue;
    /**
     * 指定占位符和默认值之间的分隔符
     */
    private final String defaultValueSeparator;

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
    }

    /**
     * 实现了{@link TokenHandler}接口中的{@link TokenHandler#handleToken(String)}方法，该实现：
     *  1. 首先会按照{@link #defaultValueSeparator}字段指定的分隔符对整个占位符切分，得到占位符的名称和默认值；
     *  2. 然后按照切分得到的占位符名称查找对应的值，如果在<properties><properties/>节点中未定义对应的键值对，
     *     则将切分得到的默认值作为解析结果返回。
     * @param content
     * @return
     */
    @Override
    public String handleToken(String content) {
      // 检测variable集合是否为空
      if (variables != null) {
        String key = content;
        //检测是否支持占位符中使用默认值的功能
        if (enableDefaultValue) {
          // 查找分隔符
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          if (separatorIndex >= 0) {
            //获取分隔符名称
            key = content.substring(0, separatorIndex);
            //获取默认值
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          if (defaultValue != null) {
            //在variables集合中查找指定的占位符
            return variables.getProperty(key, defaultValue);
          }
        }
        // 不支持默认值的功能，则直接查找variables集合
        if (variables.containsKey(key)) {
          return variables.getProperty(key);
        }
      }
      //variables集合为空，直接返回
      return "${" + content + "}";
    }
  }

}

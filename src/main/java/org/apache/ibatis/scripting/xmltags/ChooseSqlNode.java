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
package org.apache.ibatis.scripting.xmltags;

import java.util.List;

/**
 * 如果在编写动态SQL语句时，需要类似java中的switch语句的功能，可以考虑使用<choose>、<when>、<otherwise>三个标签组合，
 * Mybatis会将<chose>标签解析成ChooseSqlNode，将<when>标签解析成IfSqlNode,将<otherwise>解析成MixedSqlNode。
 * @author Clinton Begin
 */
public class ChooseSqlNode implements SqlNode {

  /**
   * <otherwise>节点对应的SqlNode
   */
  private final SqlNode defaultSqlNode;
  /**
   * <when>节点对应的IfSqlNode集合
   */
  private final List<SqlNode> ifSqlNodes;

  public ChooseSqlNode(List<SqlNode> ifSqlNodes, SqlNode defaultSqlNode) {
    this.ifSqlNodes = ifSqlNodes;
    this.defaultSqlNode = defaultSqlNode;
  }

  @Override
  public boolean apply(DynamicContext context) {
    for (SqlNode sqlNode : ifSqlNodes) {
      if (sqlNode.apply(context)) {
        return true;
      }
    }
    if (defaultSqlNode != null) {
      defaultSqlNode.apply(context);
      return true;
    }
    return false;
  }
}

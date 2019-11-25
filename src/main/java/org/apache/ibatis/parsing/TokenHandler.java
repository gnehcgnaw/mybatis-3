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
package org.apache.ibatis.parsing;

/**
 * 占位符解析器接口
 * mybatis提供的实现如下所示：
 * @see org.apache.ibatis.parsing.PropertyParser.VariableTokenHandler
 * @see org.apache.ibatis.builder.SqlSourceBuilder.ParameterMappingTokenHandler
 * @see org.apache.ibatis.scripting.xmltags.TextSqlNode.DynamicCheckerTokenParser
 * @see org.apache.ibatis.scripting.xmltags.TextSqlNode.BindingTokenParser
 * @author Clinton Begin
 */
public interface TokenHandler {
  String handleToken(String content);
}


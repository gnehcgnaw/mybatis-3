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
package org.apache.ibatis.builder.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class XMLIncludeTransformer {

  private final Configuration configuration;
  private final MapperBuilderAssistant builderAssistant;

  public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
    this.configuration = configuration;
    this.builderAssistant = builderAssistant;
  }

  public void applyIncludes(Node source) {
    Properties variablesContext = new Properties();
    //获取mybatis-config.xml中，<properties>节点中定义的变量集合
    Properties configurationVariables = configuration.getVariables();
    /*
     *  下面的一行代码其实就是说，如果configurationVariables不为null，那么就把值赋给variablesContext。
     *  if(configurationVariables!=null){
     *      variablesContext.putAll(configurationVariables)
     *  }
     */
    Optional.ofNullable(configurationVariables).ifPresent(variablesContext::putAll);
    //处理<include>节点
    applyIncludes(source, variablesContext, false);
  }

  /**
   * Recursively apply includes through all SQL fragments.
   * @param source Include node in DOM tree
   * @param variablesContext Current context for static variables with values
   * @param included 如果为true，表明找到了refid对应的sql节点，
   */
  private void applyIncludes(Node source, final Properties variablesContext, boolean included) {
    // 判断此节点是不是<include>节点
    if (source.getNodeName().equals("include")) {
      //是<include>节点
      //通过refid的属性值，获取<sql>节点的深度克隆对象
      Node toInclude = findSqlFragment(getStringAttribute(source, "refid"), variablesContext);
      //获取<include>标签下的<property>的name 和 value，然后把其添加到variablesContext中
      Properties toIncludeContext = getVariablesContext(source, variablesContext);
      //然后把 included属性设置为 true
      applyIncludes(toInclude, toIncludeContext, true);
      if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
        toInclude = source.getOwnerDocument().importNode(toInclude, true);
      }
      //将<include>节点替换成<sql>节点
      source.getParentNode().replaceChild(toInclude, source);
      while (toInclude.hasChildNodes()) {
        //将<sql>节点的子节点添加到<sql>节点前面
        toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
      }
      //删除<sql>节点
      toInclude.getParentNode().removeChild(toInclude);
    }
    //判断节点是不是元素节点（Element 对象表示 XML 文档中的元素。元素可包含属性、其他元素或文本。）
    else if (source.getNodeType() == Node.ELEMENT_NODE) {
      //是元素节点
      if (included && !variablesContext.isEmpty()) {
        // replace variables in attribute values
        NamedNodeMap attributes = source.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
          Node attr = attributes.item(i);
          //没有必要使用attr.setNodeValue(PropertyParser.parse(attr.getNodeValue(), variablesContext));
          //直接使用"PropertyParser.parse(attr.getNodeValue(),variablesContext);"即可
         attr.setNodeValue(PropertyParser.parse(attr.getNodeValue(), variablesContext));
        }
      }
      //获取当前节点的子节点
      // 如果如下所示：那么得到子节点就是文本节点
      // 1. <select id="selectAuthor" resultMap="authorResultMap">
      //      select author_id ,author_username , author_password ,author_email from tb_author where author_id = #{id}
      //    </select>


      // 2.  <sql id="fromSqlElement">
      //    from ${tablename}
      //    <include refid="whereSqlElement">
      //      <property name="idValue" value="1"/>
      //    </include>
      //  </sql>
      // <sql id="whereSqlElement">
      //    where blog_id = ${idValue}
      //  </sql>
      NodeList children = source.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        applyIncludes(children.item(i), variablesContext, included);
      }
    }

    else if (included && (source.getNodeType() == Node.TEXT_NODE || source.getNodeType() == Node.CDATA_SECTION_NODE)
        && !variablesContext.isEmpty()) {
      // replace variables in text node
      // 如果是included 并且这是一个文本节点或者是一个不转换类型的节点，并且参数值不是空的
      source.setNodeValue(PropertyParser.parse(source.getNodeValue(), variablesContext));
    }
  }

  private Node findSqlFragment(String refid, Properties variables) {
    refid = PropertyParser.parse(refid, variables);
    refid = builderAssistant.applyCurrentNamespace(refid, true);
    try {
      //查找refid对应的sql片段
      XNode nodeToInclude = configuration.getSqlFragments().get(refid);
      //返回其深度克隆的Node对象
      return nodeToInclude.getNode().cloneNode(true);
    } catch (IllegalArgumentException e) {
      throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
    }
  }

  private String getStringAttribute(Node node, String name) {
    return node.getAttributes().getNamedItem(name).getNodeValue();
  }

  /**
   * Read placeholders and their values from include node definition.
   * @param node Include node instance
   * @param inheritedVariablesContext Current context used for replace variables in new variables values
   * @return variables context from include instance (no inherited values)
   */
  private Properties getVariablesContext(Node node, Properties inheritedVariablesContext) {
    Map<String, String> declaredProperties = null;
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        String name = getStringAttribute(n, "name");
        // Replace variables inside
        String value = PropertyParser.parse(getStringAttribute(n, "value"), inheritedVariablesContext);
        if (declaredProperties == null) {
          declaredProperties = new HashMap<>();
        }
        if (declaredProperties.put(name, value) != null) {
          throw new BuilderException("Variable " + name + " defined twice in the same include definition");
        }
      }
    }
    if (declaredProperties == null) {
      return inheritedVariablesContext;
    } else {
      Properties newProperties = new Properties();
      newProperties.putAll(inheritedVariablesContext);
      newProperties.putAll(declaredProperties);
      return newProperties;
    }
  }
}

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
package org.apache.ibatis.parsing;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XPath解析器
 *    mybatis提供的XPathParser类封装了: {@link XPath} {@link Document} {@link EntityResolver}
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XPathParser {

  /**
   * xml document 对象
   */
  private final Document document;
  /**
   * 是否开启校验xml
   */
  private boolean validation;
  /**
   * 用于加载本地DTD文件
   *
   * xml实体解析器，默认情况下，对XML校验是，会基于xml文档开始位置定义的DTD文件或者XSD文件，
   * 例如：
   *    解析mybatis-config.xml配置文件时，会加载http://mybatis.org/dtd/mybatis-3-config.dtd这个DTD文件。
   *    但是，如果如果每个应用启动都从网络加载该DTD文件，势必在弱网络下体验非常下，甚至说应用部署在无网络的环境下，
   *    还会导致下载不下来，那么就会出现XML校验失败的情况 ，所以，在实际场景下，MyBatis自定义了EntityResolver
   *    的实现，达到使用本地DTD文件，从而避免下载网络 DTD 文件的效果。
   *
   * @see org.apache.ibatis.builder.xml.XMLMapperEntityResolver
   */
  private EntityResolver entityResolver;
  /**
   * 变量 Properties对象
   */
  private Properties variables;
  /**
   * java XPath对象
   *    用于查询xml中的节点和元素
   */
  private XPath xpath;

  public XPathParser(String xml) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document) {
    commonConstructor(false, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(reader));
  }

  /**
   *
   * @param inputStream
   * @param validation  true 默认是启动验证状态的
   * @param variables   properties参数
   * @param entityResolver  mybatis指定的 {@link org.apache.ibatis.builder.xml.XMLMapperEntityResolver}
   */
  public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = document;
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  // -------------eval方法----start-------------------
  /**
   *   XPathParser提供了一些了的eval*()方法用于解析boolean、short、long、int 、String 、node 等类型的信息，
   *   它通过{@link XPath#evaluate(String, InputSource)}方法查找指定路径的节点和属性，并进行相应的类型转换，
   *   具体的代码比较简单。
   *
   *   值得注意的是 {@link XPathParser#evalString(String)} 方法调用了{@link PropertyParser#parse(String, Properties)}方法处理节点相应的默认值。
   */

  public String evalString(String expression) {
    return evalString(document, expression);
  }

  public String evalString(Object root, String expression) {
    String result = (String) evaluate(expression, root, XPathConstants.STRING);
    //处理节点中相应的默认值
    result = PropertyParser.parse(result, variables);
    return result;
  }

  public Boolean evalBoolean(String expression) {
    return evalBoolean(document, expression);
  }

  public Boolean evalBoolean(Object root, String expression) {
    return (Boolean) evaluate(expression, root, XPathConstants.BOOLEAN);
  }

  public Short evalShort(String expression) {
    return evalShort(document, expression);
  }

  public Short evalShort(Object root, String expression) {
    return Short.valueOf(evalString(root, expression));
  }

  public Integer evalInteger(String expression) {
    return evalInteger(document, expression);
  }

  public Integer evalInteger(Object root, String expression) {
    return Integer.valueOf(evalString(root, expression));
  }

  public Long evalLong(String expression) {
    return evalLong(document, expression);
  }

  public Long evalLong(Object root, String expression) {
    return Long.valueOf(evalString(root, expression));
  }

  public Float evalFloat(String expression) {
    return evalFloat(document, expression);
  }

  public Float evalFloat(Object root, String expression) {
    return Float.valueOf(evalString(root, expression));
  }

  public Double evalDouble(String expression) {
    return evalDouble(document, expression);
  }

  public Double evalDouble(Object root, String expression) {
    return (Double) evaluate(expression, root, XPathConstants.NUMBER);
  }

  public List<XNode> evalNodes(String expression) {
    return evalNodes(document, expression);
  }

  public List<XNode> evalNodes(Object root, String expression) {
    List<XNode> xnodes = new ArrayList<>();
    NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);
    for (int i = 0; i < nodes.getLength(); i++) {
      xnodes.add(new XNode(this, nodes.item(i), variables));
    }
    return xnodes;
  }

  public XNode evalNode(String expression) {
    return evalNode(document, expression);
  }

  public XNode evalNode(Object root, String expression) {
    Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
    if (node == null) {
      return null;
    }
    return new XNode(this, node, variables);
  }

  private Object evaluate(String expression, Object root, QName returnType) {
    try {
      return xpath.evaluate(expression, root, returnType);
    } catch (Exception e) {
      throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
    }
  }
  // -------------eval方法----end-------------------

  /**
   * 调用这个方法之前一定要先调用{@link #commonConstructor(boolean, Properties, EntityResolver)}方法完成初始化。
   * 将xml解析为Document对象
   * @param inputSource
   * @return
   */
  private Document createDocument(InputSource inputSource) {
    // important: this must only be called AFTER common constructor
    try {
      //创建DocumentBuilderFactory对象
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      //对DocumentBuilderFactory进行一系列的配置
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setValidating(validation);

      factory.setNamespaceAware(false);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(false);
      factory.setCoalescing(false);
      factory.setExpandEntityReferences(true);

      //创建DocumentBuilder对象并进行配置
      DocumentBuilder builder = factory.newDocumentBuilder();
      //设置EntityResolver接口对象
      builder.setEntityResolver(entityResolver);
      builder.setErrorHandler(new ErrorHandler() {
        //其中实现的ErrorHandler接口的方法都是空实现
        @Override
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          // NOP
        }
      });
      //加载xml文件
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

  /**
   * 通用构造：
   *    就是通用的赋值操作，主要是的得到一个xpath解析器对document对象进行解析
   * @param validation
   * @param variables
   * @param entityResolver
   */
  private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
  }

}

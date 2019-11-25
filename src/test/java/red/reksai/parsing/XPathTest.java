package red.reksai.parsing;

import org.apache.ibatis.io.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * XPath解析测试 {@link javax.xml.xpath.XPath}
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/25 10:11
 */
public class XPathTest {
  public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    //创建DOM解析器工厂
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    //开启验证
    documentBuilderFactory.setValidating(true);
    documentBuilderFactory.setNamespaceAware(false);
    documentBuilderFactory.setIgnoringComments(true);
    documentBuilderFactory.setIgnoringElementContentWhitespace(false);
    documentBuilderFactory.setCoalescing(false);
    documentBuilderFactory.setExpandEntityReferences(true);
    //得到一个DOM解析器对象
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    documentBuilder.setErrorHandler(new ErrorHandler() {
      @Override
      public void warning(SAXParseException exception) throws SAXException {
        System.out.println("warning" + exception.getMessage());
      }

      @Override
      public void error(SAXParseException exception) throws SAXException {
        System.out.println("error" + exception.getMessage());
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXException {
        System.out.println("fatalError" + exception.getMessage());
      }
    });
    String resource = "resources/xpath-demo.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    //解析xml
    Document document = documentBuilder.parse(inputStream);

    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xPath = xPathFactory.newXPath();

    XPathExpression xPathExpression = xPath.compile("/configuration/mappers/mapper");
    NodeList evaluate = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
    for (int i = 0; i < evaluate.getLength() ; i++) {
      Node node = evaluate.item(i);
      String nodeName = node.getNodeName();
      if (("mapper").equals(node.getNodeName())){
        Node attributeNode = node.getAttributes().getNamedItem("resource");
        String nodeValue1 = attributeNode.getNodeValue();
        System.out.println(nodeName +"......"+nodeValue1);
      }
    }
  }
}

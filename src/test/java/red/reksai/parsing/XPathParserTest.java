package red.reksai.parsing;

import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;

import java.io.IOException;
import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/25 16:11
 */
public class XPathParserTest {
  public static void main(String[] args) throws IOException {
    String resource = "resources/xpath-demo.xml";
    XPathParser xPathParser = new XPathParser(Resources.getResourceAsReader(resource),true,null,new XMLMapperEntityResolver());
    XNode xNode = xPathParser.evalNode("/configuration");
    List<XNode> children = xNode.getChildren();
    for (int i = 0; i < children.size(); i++) {
      System.out.println(children.get(i).getName());
    }
  }
}

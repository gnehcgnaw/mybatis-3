package red.reksai.reflection.propertytokenizer;

import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/29 11:12
 */
public class PropertyTokenizerTest {
  public static void main(String[] args) {
    String fullName = "orders[0.items[0].name" ;
    doTokenizer(fullName);
  }

  private static void doTokenizer(String name){
    PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
    System.out.println(propertyTokenizer.getIndexedName());
    System.out.println(propertyTokenizer.getName());
    System.out.println(propertyTokenizer.getIndex());
    System.out.println(propertyTokenizer.getChildren());
    System.out.println(".................");
    String children = propertyTokenizer.getChildren();
    if (propertyTokenizer.hasNext()){
      doTokenizer(children);
    }
  }
}


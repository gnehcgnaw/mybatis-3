package red.reksai.reflection;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.junit.jupiter.api.Test;
import red.reksai.reflection.entity.User;
/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/29 10:07
 */

public class MetaClassTest {

  @Test
  public  void test() {
    MetaClass metaClass = MetaClass.forClass(User.class, new DefaultReflectorFactory());
    System.out.println(metaClass.findProperty("tele.country"));     //tele.country
    System.out.println(metaClass.getGetterType("tele.country"));    // class java.lang.String
    System.out.println(metaClass.hasGetter("tete.country"));    //true
    System.out.println(metaClass.getGetterType("orders[0].items[0]"));    // class red.reksai.reflection.entity.Item
  }
}

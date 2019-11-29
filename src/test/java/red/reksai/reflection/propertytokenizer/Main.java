package red.reksai.reflection.propertytokenizer;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import red.reksai.reflection.entity.User;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/28 17:55
 */
public class Main {
  public static void main(String[] args) throws IOException {
    String resources = "resources/mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resources);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    UserMapper mapper = sqlSessionFactory.openSession().getMapper(UserMapper.class);
    User user = mapper.selectOrderByUserId();
    System.out.println(user);
  }
}

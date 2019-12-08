package red.reksai.session;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/8 15:11
 */
public class SqlSessionTest {
  @Test
  public void testqueryForObject() throws IOException {
    SqlSession sqlSession = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("resources/mybatis-config.xml")).openSession();

  }
}

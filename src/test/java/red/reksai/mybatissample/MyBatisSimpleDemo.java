package red.reksai.mybatissample;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import red.reksai.mybatissample.mapper.UserMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 11:08
 */
public class MyBatisSimpleDemo {
  public static void main(String[] args) throws IOException {
    String resource = "resources/mybatis-config.xml" ;
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    UserMapper userMapper = sqlSessionFactory.openSession().getMapper(UserMapper.class);
    System.out.println(userMapper.selectUser(1));


  }
}

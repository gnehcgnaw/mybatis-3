package red.reksai.bingding;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import red.reksai.bingding.mapper.CommentMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/8 16:21
 */
public class MapperRegistryTest {
  public static void main(String[] args) throws IOException {
    String resource = "resources/mybatis-config.xml" ;
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    //sqlSessionFactory.getConfiguration().getMapperRegistry().addMappers("red.reksai.bingding.mapper");
    CommentMapper mapper = sqlSessionFactory.openSession().getMapper(CommentMapper.class);
    System.out.println(mapper.selectTbComment(1));
  }
}

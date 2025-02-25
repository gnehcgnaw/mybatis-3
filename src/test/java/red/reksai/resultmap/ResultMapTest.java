package red.reksai.resultmap;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import red.reksai.resultmap.entity.TbAuthor;
import red.reksai.resultmap.entity.TbBlog;
import red.reksai.resultmap.entity.TbComment;
import red.reksai.resultmap.mapper.TbAuthorMapper;
import red.reksai.resultmap.mapper.TbBlogMapper;
import red.reksai.resultmap.mapper.TbCommentMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 11:08
 */

public class ResultMapTest {
  private  static TbBlogMapper tbBlogMapper ;
  private static TbCommentMapper tbCommentMapper ;
  private static TbAuthorMapper tbAuthorMapper ;

  @BeforeClass
  public static void setUpMybatisDatabase() throws IOException {
    SqlSessionFactory builder = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("resources/mybatis-config.xml"));
    tbBlogMapper = builder.getConfiguration().getMapper(TbBlogMapper.class, builder.openSession(true));
    tbCommentMapper = builder.getConfiguration().getMapper(TbCommentMapper.class, builder.openSession(true));
    tbAuthorMapper = builder.getConfiguration().getMapper(TbAuthorMapper.class, builder.openSession(true));
  }

  @Test
  public void testSelectAuthor() {
    System.out.println(JSON.toJSONString(tbAuthorMapper.selectAuthor(1)));

  }

  @Test
  public void testSelectComment(){
    System.out.println(JSON.toJSONString(tbCommentMapper.selectComment(1)));
  }


  @Test
  public void testSelectBlogDetails2(){
    List<TbBlog> tbBlogs = tbBlogMapper.selectBlogDetails2(1);
    System.out.println(JSON.toJSONString(tbBlogs));
  }

  @Test
  public void testSelectBlogDetails3(){
    List<TbBlog> tbBlogs = tbBlogMapper.selectBlogDetails3(1);
    System.out.println(JSON.toJSONString(tbBlogs));
  }

  @Test
  public void testSelectBlogList(){
    System.out.println(tbBlogMapper.selectBlogList());
  }


  @Test
  public void testInsertAuthor(){
    TbAuthor tbAuthor = new TbAuthor();
    tbAuthor.setAuthorUsername("lisi");
    tbAuthor.setAuthorPassword("123456");
    tbAuthor.setAuthorEmail("lisi@gmail.com");
    System.out.println(tbAuthorMapper.insertAuthor(tbAuthor));
  }

  @Test
  public void testSelectAuthorByName(){
    System.out.println(tbAuthorMapper.selectAuthorByName("lisi"));
  }

  @Test
  public void testSelectByAuthorById(){
    System.out.println(tbAuthorMapper.selectByAuthorById(Arrays.asList(1,2,3)));
  }

  @Test
  public void testSelectByAuthorById2(){
    System.out.println(tbAuthorMapper.selectByAuthorById2(1));
  }
}

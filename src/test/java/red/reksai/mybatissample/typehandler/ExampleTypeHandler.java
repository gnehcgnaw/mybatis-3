package red.reksai.mybatissample.typehandler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import red.reksai.mybatissample.entity.Blog;
import red.reksai.mybatissample.mapper.BlogMapper;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * 自定义一个TypeHandler用来将javatype的日期类型和jdbctype的VARCHAR进行转换
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 22:01
 */
public class ExampleTypeHandler extends BaseTypeHandler {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ps.setString(i,sdf.format(parameter));
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String columnValue = rs.getString(columnName);
    if (columnName!=null){
      return new Date(Long.valueOf(columnValue));
    }
    return null;
  }

  @Override
  public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String columnValue = rs.getString(columnIndex);
    if (null != columnValue) {
      return new Date(Long.valueOf(columnValue));
    }
    return null;
  }

  @Override
  public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String columnValue = cs.getString(columnIndex);
    if (null != columnValue) {
      return new Date(Long.valueOf(columnValue));
    }
    return null;
  }
}



class Main{
  public static void main(String[] args) throws IOException {
    String resources = "resources/mybatis-config.xml";
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream(resources));
    SqlSession sqlSession = sqlSessionFactory.openSession();
    TypeHandlerRegistry typeHandlerRegistry = sqlSession.getConfiguration().getTypeHandlerRegistry();
    Collection<TypeHandler<?>> typeHandlers = typeHandlerRegistry.getTypeHandlers();
    for (TypeHandler typeHandler : typeHandlers){
      System.out.println(typeHandler.getClass().getName());
    }
    BlogMapper blogMapper = sqlSessionFactory.openSession().getMapper(BlogMapper.class);
    Blog blog = new Blog();
    blog.setBlogTitle("mybatis_learning");
    blog.setBlogContext("typeHandler learning");
    blog.setCreateTime(new Date());
    blog.setModifyTime(new Date());
    //数据没有添加到数据库，//todo 问题需要解决
    int insertCount = blogMapper.insertBlog(blog);
    System.out.println("添加条数为："+insertCount);
  }
}

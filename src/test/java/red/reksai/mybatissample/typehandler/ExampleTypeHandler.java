package red.reksai.mybatissample.typehandler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import red.reksai.mybatissample.entity.Blog;
import red.reksai.mybatissample.mapper.BlogMapper;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

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
    /**
     *  这里得到的sqlSessionFactory的实现是DefaultSqlSessionFactory
     *  参看：{@link SqlSessionFactoryBuilder#build(Configuration)}}
     */
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream(resources));
    /**
     *  这里得到的sqlSession的实现是DefaultSqlSession，并且当前的事务默认是不会自动提交的，所以通过此sqlSession进行的操作要想成功，最后都要使用{@link SqlSession#commit()}
     *  参看：{@link org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(ExecutorType, TransactionIsolationLevel, boolean)}
     */
    SqlSession sqlSession = sqlSessionFactory.openSession();
    TypeHandlerRegistry typeHandlerRegistry = sqlSession.getConfiguration().getTypeHandlerRegistry();
    Collection<TypeHandler<?>> typeHandlers = typeHandlerRegistry.getTypeHandlers();
    for (TypeHandler typeHandler : typeHandlers){
      System.out.println(typeHandler.getClass().getName());
    }
    /**
     * 有代码分析以及上面的注释，可以知道这里的sqlSession是{@link org.apache.ibatis.session.defaults.DefaultSqlSession},
     * 那么调用的方法就是：{@link org.apache.ibatis.session.defaults.DefaultSqlSession#getMapper(Class)}
     *
     * 这里返回的其实是一个BlogMapper的代理对象，即MapperProxy创建出来的对象。
     */
    BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);
    Blog blog = new Blog();
    blog.setBlogTitle("mybatis_learning"+new Date());
    blog.setBlogContext("typeHandler learning"+new Date());
    blog.setCreateTime(new Date());
    blog.setModifyTime(new Date());
    /**
     * 代理对象执行方法，其实执行的是 {@link org.apache.ibatis.binding.MapperProxy#invoke(Object, Method, Object[])}
     */
    int insertCount = blogMapper.insertBlog(blog);
    sqlSession.commit();
    System.out.println("添加条数为："+insertCount);
  }
}

package red.reksai.datasource;

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/1 01:15
 */
public class PooledDataSourceFactoryTest {
  @Test
  public void test1() throws IOException, SQLException {
    String resources = "resources/config.properties";
    InputStream inputStream = Resources.getResourceAsStream(resources);
    Properties properties = new Properties();
    properties.load(inputStream);
    PooledDataSourceFactory pooledDataSourceFactory = new PooledDataSourceFactory();
    pooledDataSourceFactory.setProperties(properties);
    Connection connection = pooledDataSourceFactory.getDataSource().getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement("select * from blog where blog_id = 1");
    ResultSet resultSet = preparedStatement.executeQuery();
    while (resultSet.next()){
      System.out.println(resultSet.getString(1));
    }
  }
}

package red.reksai.datasource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/30 13:27
 */
public class UnpooledDataSourceFactoryTest {

  @Test
  public void test1() throws IOException {
    String resources = "resources/config.properties";
    InputStream inputStream = Resources.getResourceAsStream(resources);
    Properties properties = new Properties();
    properties.load(inputStream);
    UnpooledDataSourceFactory unpooledDataSourceFactory = new UnpooledDataSourceFactory();
    unpooledDataSourceFactory.setProperties(properties);
  }
}

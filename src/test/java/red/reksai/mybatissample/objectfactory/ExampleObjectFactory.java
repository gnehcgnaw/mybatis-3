package red.reksai.mybatissample.objectfactory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * 自定义对象工厂
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 15:46
 */
public class ExampleObjectFactory  extends DefaultObjectFactory {

  private Properties properties;
  /**
   * 改变默认对象工厂的行为，在对象创建的时候添加默认值。
   * @param type
   * @return
   */
  @Override
  public Object create(Class type) {
    if (type.equals(Person.class)){
      Person person = (Person) super.create(type);
      person.setAge(26);
      person.setName("MartinAdam");
      return person;
    }else {
      return super.create(type);
    }
  }

  @Override
  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return super.create(type, constructorArgTypes, constructorArgs);
  }

  @Override
  public <T> boolean isCollection(Class<T> type) {
    return super.isCollection(type);
  }

  @Override
  protected Class<?> resolveInterface(Class<?> type) {
    return super.resolveInterface(type);
  }

  /**
   * 设置configuration参数和值
   * @param properties configuration properties
   */
  @Override
  public void setProperties(Properties properties) {
    this.properties = properties ;
  }

  public Properties getProperties(){
    return this.properties ;
  }

}

class Person{
  private int age ;
  private String name ;

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Person() {
  }

  public Person(int age, String name) {
    this.age = age;
    this.name = name;
  }

  @Override
  public String toString() {
    return "Person{" +
      "age=" + age +
      ", name='" + name + '\'' +
      '}';
  }
}

class Main{
  public static void main(String[] args) throws IOException {
    String resource = "resources/mybatis-config.xml" ;
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    Configuration configuration = sqlSession.getConfiguration();
    ObjectFactory objectFactory =configuration.getObjectFactory();
    System.out.println(((ExampleObjectFactory) objectFactory).getProperties().get("someProperty"));   //100
    Person person = objectFactory.create(Person.class);
    System.out.println(objectFactory); //red.reksai.mybatissample.objectfactory.ExampleObjectFactory@1a407d53
    System.out.println(person);   // Person{age=26, name='MartinAdam'}

  }
}


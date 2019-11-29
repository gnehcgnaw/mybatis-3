package red.reksai.reflection;

import org.apache.ibatis.reflection.TypeParameterResolver;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/28 10:20
 */
public class TypeParameterResolverTest {

  SubClassA<Long> sa = new SubClassA<>();

  public static void main(String[] args) throws NoSuchFieldException {
    Field field = ClassA.class.getDeclaredField("map");
    System.out.println(field.getGenericType());
    System.out.println(field.getGenericType() instanceof ParameterizedType);
    //解析SubClassA<Long> 中的map字段
    Type type = TypeParameterResolver.resolveFieldType(field, ParameterizedTypeImpl.make(SubClassA.class,new Type[]{Long.class},TypeParameterResolverTest.class));

    //  Type type2 = TypeParameterResolver.resolveFieldType(field, TypeParameterResolverTest.class.getDeclaredField("sa").getGenericType());
    System.out.println(type.getClass());

    ParameterizedType parameterizedType = (ParameterizedType) type;
    System.out.println(parameterizedType.getRawType());
    System.out.println(parameterizedType.getOwnerType());
    for (Type t : parameterizedType.getActualTypeArguments()){
      System.out.println(t);
    }
  }
}

class ClassA <K ,V>{
  protected Map<K, V> map ;

  public Map<K, V> getMap() {
    return map;
  }

  public void setMap(Map<K, V> map) {
    this.map = map;
  }
}

class SubClassA<T> extends ClassA<T,T>{


}

package red.reksai.javabase;

import org.apache.ibatis.binding.MapperProxy;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/8 21:18
 */
public class MethodHandlesTest {

  @Test
  public void test1() throws Throwable{
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle replace = lookup.findVirtual(String.class, "replace", MethodType.methodType(String.class, char.class, char.class));
    System.out.println((String) replace.invoke("zhangsan", Character.valueOf('g'), '_'));

  }

  @Test
  public void test2()  {
    Method privateLookupIn = null;
    try {
      privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
    } catch (NoSuchMethodException e) {
      privateLookupIn =null ;
    }

    if (privateLookupIn==null){
      Constructor<MethodHandles.Lookup> declaredConstructor = null;
      try {
        declaredConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        declaredConstructor.setAccessible(true);
      } catch (NoSuchMethodException e) {

      }
    }
  }
  @Test
  public void test3(){
  }
}



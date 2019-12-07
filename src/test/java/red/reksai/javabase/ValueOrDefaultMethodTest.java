package red.reksai.javabase;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/2 23:35
 */
public class ValueOrDefaultMethodTest {
  public static void main(String[] args) {
    System.out.println(valueOrDefault(null, 2));
  }

 /* private static Object valueOrDefault(Object value, Object defaultValue){
    return value == null ? defaultValue : value ;
  }*/
  // 销售  —————— 服装销售   锻炼自己的抽象能力
  /**
   *
   * @param value
   * @param defaultValue
   * @param <T>
   * @return
   */
  private static <T> T valueOrDefault(T value ,T defaultValue){
    return value == null ? defaultValue :value ;
  }
}



package red.reksai.javabase;

import org.junit.jupiter.api.Test;

import java.util.StringTokenizer;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/16 11:10
 */
public class StringTokenizerTest {
  @Test
  public void test1(){
    StringTokenizer stringTokenizer = new StringTokenizer("root|12345|admin|user" ,"|" ,true);
    while (stringTokenizer.hasMoreTokens()){
      System.out.println(stringTokenizer.nextToken());
    }
  }
}

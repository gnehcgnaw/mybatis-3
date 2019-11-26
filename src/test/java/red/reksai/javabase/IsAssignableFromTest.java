package red.reksai.javabase;

/**
 * A.class.isAssignableFrom(B.class) 判断的是A是不是B的父类
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/26 15:27
 */
public class IsAssignableFromTest {
  public static void main(String[] args) {
    System.out.println(WinnerType.class.isAssignableFrom(Candidate.class));   // false
    System.out.println(Candidate.class.isAssignableFrom(WinnerType.class));   // true
  }
}

class WinnerType extends Candidate {

}

class  Candidate {

}

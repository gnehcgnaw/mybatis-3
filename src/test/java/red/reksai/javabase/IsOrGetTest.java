package red.reksai.javabase;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/26 15:02
 */
public class IsOrGetTest {
  public static void main(String[] args) {
      B b = new B();
      System.out.println(b.status);
      b.setStatus(true);
      System.out.println(b.isStatus());

      C c = new C();
      System.out.println(c.status);
      c.setStatus(true);
      System.out.println(c.getStatus());
      System.out.println(c.isStatus());

  }
}

class A {
  boolean status ;

  Boolean isUsing ;

  public Boolean getUsing() {
    return isUsing;
  }

  public void setUsing(Boolean using) {
    isUsing = using;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }
}

class B extends A{
  @Override
  public boolean isStatus() {
    return super.isStatus();
  }

  @Override
  public void setStatus(boolean status) {
    super.setStatus(status);
  }
}

class C extends  A{
  public boolean getStatus() {
    return super.isStatus();
  }

  @Override
  public void setStatus(boolean status) {
    super.setStatus(status);
  }
}





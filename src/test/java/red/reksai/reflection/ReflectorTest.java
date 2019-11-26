package red.reksai.reflection;

import org.apache.ibatis.reflection.Reflector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/26 10:19
 */
public class ReflectorTest {
  public static void main(String[] args) {
    Reflector reflector = new Reflector(User.class);
    System.out.println(reflector);
  }
}

class  User extends Person{

  public User() {
  }

  public User(String userName, String passWord) {
    this.userName = userName;
    this.passWord = passWord;
  }

  private String userName ;
  private String passWord ;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassWord() {
    return passWord;
  }

  public void setPassWord(String passWord) {
    this.passWord = passWord;
  }

  public boolean isStatus() {
    return super.getStatus() ;
  }

  @Override
  public void setStatus(boolean status) {
    super.setStatus(status);
  }

  @Override
  public ArrayList<Items> getList() {
    return (ArrayList<Items>) super.getList();
  }

  @Override
  public void setList(List<Items> list) {
    super.setList(list);
  }
}

class Person {

  private List<Items> list;

  private boolean status ;

  public boolean getStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public List<Items> getList() {
    return list;
  }

  public void setList(List<Items> list) {
    this.list = list;
  }

  public Person() {
  }

  public Person(List<Items> list) {
    this.list = list;
  }
}
class Items{
  private String itmeId;
  private String itmeName ;
}

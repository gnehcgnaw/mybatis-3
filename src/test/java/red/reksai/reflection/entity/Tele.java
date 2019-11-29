package red.reksai.reflection.entity;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/29 15:10
 */
public class Tele {
  private int id;
  private String country ;
  private String type ;
  private String num ;

  public Tele(int id, String country, String type, String num) {
    this.id = id;
    this.country = country;
    this.type = type;
    this.num = num;
  }

  public Tele() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getNum() {
    return num;
  }

  public void setNum(String num) {
    this.num = num;
  }
}

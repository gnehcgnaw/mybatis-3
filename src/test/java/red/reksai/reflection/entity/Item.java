package red.reksai.reflection.entity;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/28 18:02
 */
public class Item {
  private int id ;
  private String name ;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Item() {
  }

  public Item(int id, String name) {
    this.id = id;
    this.name = name;
  }
}

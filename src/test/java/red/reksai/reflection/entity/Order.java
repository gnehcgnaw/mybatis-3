package red.reksai.reflection.entity;

import red.reksai.reflection.entity.Item;

import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/28 18:01
 */
public class Order {

  private int id ;
  private String name ;
  private List<Item> items ;

  public Order(int id, List<Item> items) {
    this.id = id;
    this.items = items;
  }

  public Order() {

  }

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


  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }

}

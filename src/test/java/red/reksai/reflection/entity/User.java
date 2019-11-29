package red.reksai.reflection.entity;

import red.reksai.reflection.entity.Order;

import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/28 18:00
 */
public class User {
  private int id ;
  private List<Order> orders ;
  private Tele tele ;
  public User(int id, List<Order> orders) {
    this.id = id;
    this.orders = orders;
  }

  public User() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<Order> getOrders() {
    return orders;
  }

  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  public Tele getTele() {
    return tele;
  }

  public void setTele(Tele tele) {
    this.tele = tele;
  }
}

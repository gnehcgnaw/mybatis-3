package red.reksai;

import java.sql.*;

/**
 * 原生JDBC查询数据库
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 10:31
 */
public class JDBCDemo {
  static {
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
  }

  public static void main(String[] args) {
    Connection connection = null;
    ResultSet resultSet = null;
    try {
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/exam?serverTimezone=Asia/Shanghai&characterEncoding=utf8" ,"root","1qaz2wsx!@#");
      PreparedStatement preparedStatement = connection.prepareStatement("select * from t_user");
      resultSet = preparedStatement.executeQuery();
      while (resultSet.next()){
        System.out.println(resultSet.getString(2));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }finally {
      try {
        resultSet.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}

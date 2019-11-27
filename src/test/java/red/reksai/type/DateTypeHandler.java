package red.reksai.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/27 11:42
 */
public class DateTypeHandler extends BaseTypeHandler {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ps.setString(i,sdf.format(parameter));
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String columnValue = rs.getString(columnName);
    if (columnName!=null){
      return new Date(Long.valueOf(columnValue));
    }
    return null;
  }

  @Override
  public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String columnValue = rs.getString(columnIndex);
    if (null != columnValue) {
      return new Date(Long.valueOf(columnValue));
    }
    return null;
  }

  @Override
  public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String columnValue = cs.getString(columnIndex);
    if (null != columnValue) {
      return new Date(Long.valueOf(columnValue));
    }
    return null;
  }
}

class  Main {
  public static void main(String[] args) {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(DateTypeHandler.class);
    typeHandlerRegistry.getTypeHandlers().forEach(e -> System.out.println(e));

  }
}

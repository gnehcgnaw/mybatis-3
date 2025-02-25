/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 *
 * 每个<resultMap>节点都会被解析成一个{@link ResultMap}对象，而每个节点所定义的映射关系，则使用{@link ResultMapping}对象表示。
 * e.g. :
 *   <resultMap id="BaseResultMap" type="red.reksai.mybatissample.entity.Blog">
 *     <id column="blog_id" jdbcType="INTEGER" property="blogId" />
 *     <result column="blog_title" jdbcType="VARCHAR" property="blogTitle" />
 *     <result column="blog_content" jdbcType="VARCHAR" property="blogContext" />
 *     <result column="create_time" jdbcType="VARCHAR" property="createTime" />
 *     <result column="modify_time" typeHandler="red.reksai.mybatissample.typehandler.ExampleTypeHandler" property="modifyTime" />
 *   </resultMap>
 *
 *
 * 每个ResultMapping对象记录了结果集中的一列与JavaBean中一个属性之间的映射关系。
 * @author Clinton Begin
 */
public class ResultMapping {
  /**
   * Configuration对象
   */
  private Configuration configuration;
  /**
   * 对应节点的property属性，表示的是与该列映射的属性
   */
  private String property;
  /**
   * 对应节点的column属性，表示的是从数据库中得到的列名或列名的别名
   */
  private String column;
  /**
   * 对应节点的javaType属性，表示的是一个JavaBean的完全限定名，或一个类型的别名
   */
  private Class<?> javaType;
  /**
   * 对应节点的jdbcType属性，表示的是进行映射的列的JDBC类型
   */
  private JdbcType jdbcType;
  /**
   * 对应节点的typeHandler属性， 表示的是类型处理器，它会覆盖默认的类型处理器，
   */
  private TypeHandler<?> typeHandler;
  /**
   * 对应节点的resultMap属性，该属性通过id引用另一个<resultMap>节点定义，它负责将结果集中的一部分分列映射成其他关联的结果对象。
   * 这样我们就可以通过join方式进行关联查询，然后直接映射多个对象，并同时设置这些对象之间的关系。
   */
  private String nestedResultMapId;
  /**
   * 对应节点的select属性，该属性通过id引用了另一个<select>节点定义，它会把指定的列的值传入select属性指定的select语句中作为参数进行查询。
   * 使用select属性可能会造成N+1问题。
   */
  private String nestedQueryId;
  /**
   * 对应节点的notNullColumns属性拆分后的结果
   */
  private Set<String> notNullColumns;
  /**
   * 对应节点的columnPrefix属性
   */
  private String columnPrefix;
  /**
   * 处理后的标志，标志有两个：id和constructor
   */
  private List<ResultFlag> flags;
  /**
   * 对应节点的column属性拆分后生成的结果，composites.size()>0会是column为null
   */
  private List<ResultMapping> composites;
  /**
   * 对应节点的resultSet属性
   */
  private String resultSet;
  /**
   * 对应节点的foreignColumn属性
   */
  private String foreignColumn;
  /**
   * 对应节点的lazy属性，是否延迟加载
   */
  private boolean lazy;

  ResultMapping() {
  }

  /**
   * 应用了建造者欧式，
   * 该Builder的主要作用是：数据整理和数据校验
   */
  public static class Builder {
    private ResultMapping resultMapping = new ResultMapping();

    public Builder(Configuration configuration, String property, String column, TypeHandler<?> typeHandler) {
      this(configuration, property);
      resultMapping.column = column;
      resultMapping.typeHandler = typeHandler;
    }

    public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
      this(configuration, property);
      resultMapping.column = column;
      resultMapping.javaType = javaType;
    }

    public Builder(Configuration configuration, String property) {
      resultMapping.configuration = configuration;
      resultMapping.property = property;
      resultMapping.flags = new ArrayList<>();
      resultMapping.composites = new ArrayList<>();
      resultMapping.lazy = configuration.isLazyLoadingEnabled();
    }

    public Builder javaType(Class<?> javaType) {
      resultMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      resultMapping.jdbcType = jdbcType;
      return this;
    }

    public Builder nestedResultMapId(String nestedResultMapId) {
      resultMapping.nestedResultMapId = nestedResultMapId;
      return this;
    }

    public Builder nestedQueryId(String nestedQueryId) {
      resultMapping.nestedQueryId = nestedQueryId;
      return this;
    }

    public Builder resultSet(String resultSet) {
      resultMapping.resultSet = resultSet;
      return this;
    }

    public Builder foreignColumn(String foreignColumn) {
      resultMapping.foreignColumn = foreignColumn;
      return this;
    }

    public Builder notNullColumns(Set<String> notNullColumns) {
      resultMapping.notNullColumns = notNullColumns;
      return this;
    }

    public Builder columnPrefix(String columnPrefix) {
      resultMapping.columnPrefix = columnPrefix;
      return this;
    }

    public Builder flags(List<ResultFlag> flags) {
      resultMapping.flags = flags;
      return this;
    }

    public Builder typeHandler(TypeHandler<?> typeHandler) {
      resultMapping.typeHandler = typeHandler;
      return this;
    }

    public Builder composites(List<ResultMapping> composites) {
      resultMapping.composites = composites;
      return this;
    }

    public Builder lazy(boolean lazy) {
      resultMapping.lazy = lazy;
      return this;
    }

    public ResultMapping build() {
      // lock down collections
      resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
      resultMapping.composites = Collections.unmodifiableList(resultMapping.composites);
      resolveTypeHandler();
      //进行校验
      validate();
      return resultMapping;
    }

    private void validate() {
      // Issue #697: cannot define both nestedQueryId and nestedResultMapId
      // 其实意思是：无法同时使用 resultMap和select
      if (resultMapping.nestedQueryId != null && resultMapping.nestedResultMapId != null) {
        throw new IllegalStateException("Cannot define both nestedQueryId and nestedResultMapId in property " + resultMapping.property);
      }
      // Issue #5: there should be no mappings without typehandler
      // 没有类型处理程序，就不应有任何映射
      if (resultMapping.nestedQueryId == null && resultMapping.nestedResultMapId == null && resultMapping.typeHandler == null) {
        throw new IllegalStateException("No typehandler found for property " + resultMapping.property);
      }
      // Issue #4 and GH #39: column is optional only in nested resultmaps but not in the rest
      // column仅在嵌套的结果映射中是可选的，而在其余的结果中则不是可选的
      if (resultMapping.nestedResultMapId == null && resultMapping.column == null && resultMapping.composites.isEmpty()) {
        throw new IllegalStateException("Mapping is missing column attribute for property " + resultMapping.property);
      }
      if (resultMapping.getResultSet() != null) {
        int numColumns = 0;
        if (resultMapping.column != null) {
          numColumns = resultMapping.column.split(",").length;
        }
        int numForeignColumns = 0;
        if (resultMapping.foreignColumn != null) {
          numForeignColumns = resultMapping.foreignColumn.split(",").length;
        }
        //属性中的列数和foreignColumns应该相同
        if (numColumns != numForeignColumns) {
          throw new IllegalStateException("There should be the same number of columns and foreignColumns in property " + resultMapping.property);
        }
      }
    }

    private void resolveTypeHandler() {
      if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
        Configuration configuration = resultMapping.configuration;
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
      }
    }

    public Builder column(String column) {
      resultMapping.column = column;
      return this;
    }
  }

  public String getProperty() {
    return property;
  }

  public String getColumn() {
    return column;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public JdbcType getJdbcType() {
    return jdbcType;
  }

  public TypeHandler<?> getTypeHandler() {
    return typeHandler;
  }

  public String getNestedResultMapId() {
    return nestedResultMapId;
  }

  public String getNestedQueryId() {
    return nestedQueryId;
  }

  public Set<String> getNotNullColumns() {
    return notNullColumns;
  }

  public String getColumnPrefix() {
    return columnPrefix;
  }

  public List<ResultFlag> getFlags() {
    return flags;
  }

  public List<ResultMapping> getComposites() {
    return composites;
  }

  public boolean isCompositeResult() {
    return this.composites != null && !this.composites.isEmpty();
  }

  public String getResultSet() {
    return this.resultSet;
  }

  public String getForeignColumn() {
    return foreignColumn;
  }

  public void setForeignColumn(String foreignColumn) {
    this.foreignColumn = foreignColumn;
  }

  public boolean isLazy() {
    return lazy;
  }

  public void setLazy(boolean lazy) {
    this.lazy = lazy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ResultMapping that = (ResultMapping) o;

    return property != null && property.equals(that.property);
  }

  @Override
  public int hashCode() {
    if (property != null) {
      return property.hashCode();
    } else if (column != null) {
      return column.hashCode();
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ResultMapping{");
    //sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
    sb.append("property='").append(property).append('\'');
    sb.append(", column='").append(column).append('\'');
    sb.append(", javaType=").append(javaType);
    sb.append(", jdbcType=").append(jdbcType);
    //sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
    sb.append(", nestedResultMapId='").append(nestedResultMapId).append('\'');
    sb.append(", nestedQueryId='").append(nestedQueryId).append('\'');
    sb.append(", notNullColumns=").append(notNullColumns);
    sb.append(", columnPrefix='").append(columnPrefix).append('\'');
    sb.append(", flags=").append(flags);
    sb.append(", composites=").append(composites);
    sb.append(", resultSet='").append(resultSet).append('\'');
    sb.append(", foreignColumn='").append(foreignColumn).append('\'');
    sb.append(", lazy=").append(lazy);
    sb.append('}');
    return sb.toString();
  }

}

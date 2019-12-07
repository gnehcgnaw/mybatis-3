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
package org.apache.ibatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 *
 *
 * 负责解析映射配置文件
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XMLMapperBuilder extends BaseBuilder {

  private final XPathParser parser;
  private final MapperBuilderAssistant builderAssistant;
  private final Map<String, XNode> sqlFragments;
  private final String resource;

  @Deprecated
  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
    this(reader, configuration, resource, sqlFragments);
    this.builderAssistant.setCurrentNamespace(namespace);
  }

  @Deprecated
  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
        configuration, resource, sqlFragments);
  }

  public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
    this(inputStream, configuration, resource, sqlFragments);
    this.builderAssistant.setCurrentNamespace(namespace);
  }

  public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
        configuration, resource, sqlFragments);
  }

  private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    super(configuration);
    this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    this.parser = parser;
    this.sqlFragments = sqlFragments;
    this.resource = resource;
  }

  /**
   * 解析映射文件的入口
   */
  public void parse() {
    //首先判断是否已经加载过该映射文件
    if (!configuration.isResourceLoaded(resource)) {
      //处理<mapper>节点
      configurationElement(parser.evalNode("/mapper"));
      //将resource添加到Configuration.loadedResource结合中保存，这是一个HashSet<String>类型的集合，用于记录已加载过的映射文件
      configuration.addLoadedResource(resource);
      //为mapper绑定命名空间
      bindMapperForNamespace();
    }

    parsePendingResultMaps();
    parsePendingCacheRefs();
    parsePendingStatements();
  }

  public XNode getSqlFragment(String refid) {
    return sqlFragments.get(refid);
  }

  private void configurationElement(XNode context) {
    try {
      //获取<mapper>节点的namespace属性
      String namespace = context.getStringAttribute("namespace");
      if (namespace == null || namespace.equals("")) {
        throw new BuilderException("Mapper's namespace cannot be empty");
      }
      //设置BuilderAssistant的currentNamespace字段，记录当前命名空间
      builderAssistant.setCurrentNamespace(namespace);
      //解析<cache-red>节点
      cacheRefElement(context.evalNode("cache-ref"));
      //解析<cache>节点
      cacheElement(context.evalNode("cache"));
      //解析<parameterMap>节点
      parameterMapElement(context.evalNodes("/mapper/parameterMap"));
      //解析<resultMap>节点
      resultMapElements(context.evalNodes("/mapper/resultMap"));
      //解析<sql>节点
      sqlElement(context.evalNodes("/mapper/sql"));
      //解析<select>、<insert>、<update>、<delete>节点
      buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
    }
  }

  private void buildStatementFromContext(List<XNode> list) {
    if (configuration.getDatabaseId() != null) {
      buildStatementFromContext(list, configuration.getDatabaseId());
    }
    buildStatementFromContext(list, null);
  }

  private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
    for (XNode context : list) {
      final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
      try {
        statementParser.parseStatementNode();
      } catch (IncompleteElementException e) {
        configuration.addIncompleteStatement(statementParser);
      }
    }
  }

  private void parsePendingResultMaps() {
    Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
    synchronized (incompleteResultMaps) {
      Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
      while (iter.hasNext()) {
        try {
          iter.next().resolve();
          iter.remove();
        } catch (IncompleteElementException e) {
          // ResultMap is still missing a resource...
        }
      }
    }
  }

  private void parsePendingCacheRefs() {
    Collection<CacheRefResolver> incompleteCacheRefs = configuration.getIncompleteCacheRefs();
    synchronized (incompleteCacheRefs) {
      Iterator<CacheRefResolver> iter = incompleteCacheRefs.iterator();
      while (iter.hasNext()) {
        try {
          iter.next().resolveCacheRef();
          iter.remove();
        } catch (IncompleteElementException e) {
          // Cache ref is still missing a resource...
        }
      }
    }
  }

  private void parsePendingStatements() {
    Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
    synchronized (incompleteStatements) {
      Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
      while (iter.hasNext()) {
        try {
          iter.next().parseStatementNode();
          iter.remove();
        } catch (IncompleteElementException e) {
          // Statement is still missing a resource...
        }
      }
    }
  }

  /**
   * <cache>节点是为每一个namespace创建一个对应的Cache对象，并在Configuration.caches集合中记录了namespace和Cache对象之间的对应关系，
   * 但是我们如果希望多个namespace共用一个二级缓存，即同一个Cache对象，则可以使用<cache-ref>节点进行配置。
   * @param context
   */
  private void cacheRefElement(XNode context) {
    if (context != null) {
      //将当前Mapper配置文件的namespace与被引用的Cache所在namespace之间的对应关系，记录到Configuration.cacheRefMap集合中
      configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));
      //创建CacheRefResolver对象
      CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));
      try {
        //解析cache引用，该过程主要是设置MapperBuilderAssistant中的currentCache和unresolvedRef字段
        cacheRefResolver.resolveCacheRef();
      } catch (IncompleteElementException e) {
        //如果解析过程中出现了异常，则添加到Configuration.incompleteCache集合，稍后再解析
        configuration.addIncompleteCacheRef(cacheRefResolver);
      }
    }
  }

  private void cacheElement(XNode context) {
    if (context != null) {
      //获取<cache>节点的type属性，（处理缓存的方式）默认值是PERPETUAL 即 PerpetualCache
      String type = context.getStringAttribute("type", "PERPETUAL");
      //查找type属性对应的Cache接口实现
      Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
      //获取<cache>节点的eviction属性 （缓存清理方式）默认值是 LRU 即 LruCache
      String eviction = context.getStringAttribute("eviction", "LRU");
      Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
      //获取<cache>节点的flushInterval属性，（刷新间隔），默认值为null
      Long flushInterval = context.getLongAttribute("flushInterval");
      //获取<cache>节点的size属性，默认值为null
      Integer size = context.getIntAttribute("size");
      //获取<cache>节点的readOnly属性，默认值为false
      boolean readWrite = !context.getBooleanAttribute("readOnly", false);
      //获取<cache>节点的blocking属性，默认值为false
      boolean blocking = context.getBooleanAttribute("blocking", false);
      //获取<cache>节点下的子节点，将用于初始化二级缓存
      Properties props = context.getChildrenAsProperties();
      //通过MapperBuilderAssistant对象创建Cache对象，并添加到Configuration.caches集合中保存
      builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }
  }

  private void parameterMapElement(List<XNode> list) {
    for (XNode parameterMapNode : list) {
      String id = parameterMapNode.getStringAttribute("id");
      String type = parameterMapNode.getStringAttribute("type");
      Class<?> parameterClass = resolveClass(type);
      List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
      List<ParameterMapping> parameterMappings = new ArrayList<>();
      for (XNode parameterNode : parameterNodes) {
        String property = parameterNode.getStringAttribute("property");
        String javaType = parameterNode.getStringAttribute("javaType");
        String jdbcType = parameterNode.getStringAttribute("jdbcType");
        String resultMap = parameterNode.getStringAttribute("resultMap");
        String mode = parameterNode.getStringAttribute("mode");
        String typeHandler = parameterNode.getStringAttribute("typeHandler");
        Integer numericScale = parameterNode.getIntAttribute("numericScale");
        ParameterMode modeEnum = resolveParameterMode(mode);
        Class<?> javaTypeClass = resolveClass(javaType);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
        ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
        parameterMappings.add(parameterMapping);
      }
      builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
    }
  }

  private void resultMapElements(List<XNode> list) throws Exception {
    //list是<resultMap>节点的结合，有几对个<resultMap></resultMap> ，list的size()就等于几。
    for (XNode resultMapNode : list) {
      try {
        /**
         * 进行遍历,一对一对解析，一个resultMap存入{@link Configuration#resultMaps}是两个,因为这里的resultMaps是{@link Configuration.StrictMap}类型的，
         * 这个类中的{@link Configuration.StrictMap#put(String, Object)}方法会put两次，一个是简单的key，一次是全路径key。
         * 例如： <resultMap id="authorResultMap" type="TbAuthor">
         *    一个是简单名字： authorResultMap -> {ResultMap@1816}
         *    一个是全路径名称： red.reksai.resultmap.mapper.TbAuthorMapper.authorResultMap -> {ResultMap@1816}
         */
        resultMapElement(resultMapNode);
      } catch (IncompleteElementException e) {
        // ignore, it will be retried
      }
    }
  }

  private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
    return resultMapElement(resultMapNode, Collections.emptyList(), null);
  }

  /**
   *<!ELEMENT resultMap (constructor?,id*,result*,association*,collection*, discriminator?)>
   * <!ATTLIST resultMap
   * id CDATA #REQUIRED
   * type CDATA #REQUIRED
   * extends CDATA #IMPLIED
   * autoMapping (true|false) #IMPLIED
   * >
   * @param resultMapNode   <resultMap><resultMap/>
   * @param additionalResultMappings  ResultMapping的集合
   * @param enclosingType 封装类型
   * @return
   * @throws Exception
   */
  private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings, Class<?> enclosingType) throws Exception {
    ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
    /**
     * 看到代码的第一印象：
     *    获取<resultMap type >中type string
     *    我不知道为什么这么写 ,因为resultMap的属性只有： id ,type ,autoMapping , id , extends 而没有ofType ,resultType , javaType ,并且此时是获取type的属性的值。
     *  ----------------------   这留下了我的一个疑问？希望在跟后续步骤的时候可以解决掉。
     *
     *
     */
    String type = resultMapNode.getStringAttribute("type",
        resultMapNode.getStringAttribute("ofType",
            resultMapNode.getStringAttribute("resultType",
                resultMapNode.getStringAttribute("javaType"))));
    /**
     * 因为以上代码的第一印象，以及 mybatis-3-mappper.dtd中 resultMap的type CDATA #REQUIRED ，那也就表明 resolveClass(type)只有两种结果：
     *    1. 正常返回一个typeClass ,例如 tbAuthor ----> {@link red.reksai.resultmap.entity.TbAuthor};
     *    2. 直接保存，出现一个resolveClass中的一个{@link org.apache.ibatis.binding.BindingException} , Error resolving class. Cause: e
     *  所以接下来的一步if(typeClass=null)，就显得没有必要了，
     *  --------------------    这留下了我第二个疑问，希望在跟后续步骤的时候可以解决掉。
     */
    //获取resultMap映射的Class类型
    Class<?> typeClass = resolveClass(type);
    //这步骤的意义何在？
    if (typeClass == null) {
      typeClass = inheritEnclosingType(resultMapNode, enclosingType);
    }
    //初始化一个Discriminator，用于存放把<discriminator>节点解析的属性
    Discriminator discriminator = null;
    //初始化一个集合，该集合用于记录解析的结果
    List<ResultMapping> resultMappings = new ArrayList<>();
    resultMappings.addAll(additionalResultMappings);
    //处理<resultMap>的子节点
    List<XNode> resultChildren = resultMapNode.getChildren();
    for (XNode resultChild : resultChildren) {
      if ("constructor".equals(resultChild.getName())) {
        //处理<constructor>节点
        processConstructorElement(resultChild, typeClass, resultMappings);
      } else if ("discriminator".equals(resultChild.getName())) {
        //处理<discriminator>节点
        discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
      } else {
        //处理<id>、<result>、<association>、<collection> 节点
        List<ResultFlag> flags = new ArrayList<>();
        //如果是<id>节点，则向flags集合中添加ResultFlag.ID
        if ("id".equals(resultChild.getName())) {
          flags.add(ResultFlag.ID);
        }
        //创建ResultMapping对象，并添加到resultMappings集合
        resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
      }
    }
    //获取<resultMap id  extends autoMapping>中的 id、extends、autoMapping的属性值
    //获取resultMap的id属性的属性值，如果为null，则返回默认值，默认值的生成规则是XNode.getValueBasedIdentifier()方法
    //但是"id CDATA #REQUIRED"，即id属性是必须的，也就是用不上默认的生成规则，XNode.getValueBasedIdentifier()就在这显得的非常多余
    // todo
    String id = resultMapNode.getStringAttribute("id",
            resultMapNode.getValueBasedIdentifier());
    //获取<resultMap>节点的extends属性的值，该属性指定了<resultMap>节点的继承关系
    String extend = resultMapNode.getStringAttribute("extends");
    //获取<resultMap>节点的autoMapping属性的值
    //如果该属性设置为true，则启动自动映射功能，即自动查找与列名相同的属性名，并调用setter方法。
    //如果该属性设置为false，则需要在<resultMapping>节点内注明映射关系才能调用对应的setter方法。
    Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
    //创建一个ResultMapResolver，并为当前的ResultMapResolver设置属性初始值，这些初始值会在ResultMapResolver的resolve()方法中派上用场
    ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
    try {
      //创建ResultMap对象，并将其添加到Configuration.resultMap集合中，
      return resultMapResolver.resolve();
    } catch (IncompleteElementException  e) {
      configuration.addIncompleteResultMap(resultMapResolver);
      throw e;
    }
  }

  protected Class<?> inheritEnclosingType(XNode resultMapNode, Class<?> enclosingType) {
    if ("association".equals(resultMapNode.getName()) && resultMapNode.getStringAttribute("resultMap") == null) {
      String property = resultMapNode.getStringAttribute("property");
      if (property != null && enclosingType != null) {
        MetaClass metaResultType = MetaClass.forClass(enclosingType, configuration.getReflectorFactory());
        return metaResultType.getSetterType(property);
      }
    } else if ("case".equals(resultMapNode.getName()) && resultMapNode.getStringAttribute("resultMap") == null) {
      return enclosingType;
    }
    return null;
  }

  /**
   * 处理
   *<constructor>
   *    <idArg column="id" javaType="int"/>
   *    <arg column="username" javaType="String"/>
   *    <arg column="age" javaType="_int"/>
   * </constructor>
   * @param resultChild
   * @param resultType
   * @param resultMappings
   * @throws Exception
   */
  private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
    List<XNode> argChildren = resultChild.getChildren();
    for (XNode argChild : argChildren) {
      List<ResultFlag> flags = new ArrayList<>();
      flags.add(ResultFlag.CONSTRUCTOR);
      if ("idArg".equals(argChild.getName())) {
        flags.add(ResultFlag.ID);
      }
      resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
    }
  }

  private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    Class<?> javaTypeClass = resolveClass(javaType);
    Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    Map<String, String> discriminatorMap = new HashMap<>();
    for (XNode caseChild : context.getChildren()) {
      String value = caseChild.getStringAttribute("value");
      String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings, resultType));
      discriminatorMap.put(value, resultMap);
    }
    return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
  }

  private void sqlElement(List<XNode> list) {
    if (configuration.getDatabaseId() != null) {
      sqlElement(list, configuration.getDatabaseId());
    }
    sqlElement(list, null);
  }

  private void sqlElement(List<XNode> list, String requiredDatabaseId) {
    for (XNode context : list) {
      String databaseId = context.getStringAttribute("databaseId");
      String id = context.getStringAttribute("id");
      id = builderAssistant.applyCurrentNamespace(id, false);
      if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
        sqlFragments.put(id, context);
      }
    }
  }

  private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
    if (requiredDatabaseId != null) {
      return requiredDatabaseId.equals(databaseId);
    }
    if (databaseId != null) {
      return false;
    }
    if (!this.sqlFragments.containsKey(id)) {
      return true;
    }
    // skip this fragment if there is a previous one with a not null databaseId
    XNode context = this.sqlFragments.get(id);
    return context.getStringAttribute("databaseId") == null;
  }

  /**
   * 例子：
   * <resultMap id="selectBlogDetailsResultMap2" type="red.reksai.resultmap.entity.TbBlog">
   *     <id property="blogId" column="blog_id" />
   *     <result property="blogTitle" column="blog_title"/>
   *     <!--关联的嵌套结果映射resultMap="red.reksai.resultmap.mapper.TbAuthorMapper.authorResultMap"-->
   *     <association property="tbAuthor"  />
   *     <collection property="tbPosts" ofType="red.reksai.resultmap.entity.TbPost" resultMap="red.reksai.resultmap.mapper.TbPostMapper.postResultMap" column="post_blog_id" select="red.reksai.resultmap.mapper.TbPostMapper.selectPost">
   *         <collection property="tbComments" ofType="red.reksai.resultmap.entity.TbComment"/>
   *     </collection>
   *   </resultMap>
   *
   * 从上下文构建结果映射：
   *      这是一个通用的方法，针对的某一个标签，针对的是 constructor?,id*,result*,association*,collection*, discriminator?
   *      故而在下面的方法描述中，会看到针对constructor?,id*,result*,association*,collection*, discriminator? 这些标签属性的全集，就不足为奇了。
   * @param context  constructor?,id*,result*,association*,collection*, discriminator?
   * @param resultType  resultType属性
   * @param flags   {@link ResultFlag#ID} OR {@link ResultFlag#CONSTRUCTOR}
   * @return  resultMapping
   * @throws Exception
   */
  private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
    String property;

    if (flags.contains(ResultFlag.CONSTRUCTOR)) {
      // 如果是ID标识，则获取name属性的值 例如：constructor 下的idArg*,arg*，只有name，而没有property
      property = context.getStringAttribute("name");
    } else {
      //如果是ID标识，则获取property属性的值 例如：<id property="authorId" column="author_id"/> property = "authorId"
      property = context.getStringAttribute("property");
    }
    //获取column属性值
    String column = context.getStringAttribute("column");
    //获取javaType属性值
    String javaType = context.getStringAttribute("javaType");
    //获取jdbcType属性值
    String jdbcType = context.getStringAttribute("jdbcType");
    //获取select属性值
    String nestedSelect = context.getStringAttribute("select");
    //获取resultMap属性值，并处理其中嵌套的resultMapping
    String nestedResultMap = context.getStringAttribute("resultMap",
        processNestedResultMappings(context, Collections.emptyList(), resultType));
    //获取notNullColumn属性值
    String notNullColumn = context.getStringAttribute("notNullColumn");
    //获取columnPrefix属性值
    String columnPrefix = context.getStringAttribute("columnPrefix");
    //获取typeHandler属性值
    String typeHandler = context.getStringAttribute("typeHandler");
    //获取resultSet的属性值
    String resultSet = context.getStringAttribute("resultSet");
    //获取foreignColumn属性值
    String foreignColumn = context.getStringAttribute("foreignColumn");
    //设置lazy的值，会考虑全局的懒加载设置，如果局部属性没有设置fetchType的值，那么使用全部的设置
    boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
    Class<?> javaTypeClass = resolveClass(javaType);
    //获取当前标签使用的typeHandler的实现类
    Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
    //获取jdbcType对应的类
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    //利用MapperBuilderAssistant构建ResultMappings
    return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
  }
  /**
   *   处理嵌套的resultMappings
   *      这个方法会调用 {@link XMLMapperBuilder#resultMapElement(XNode, List, Class)} 处理嵌套的resultMapping
   *
   *  例如：
   *    <collection property="tbPosts" ofType="red.reksai.resultmap.entity.TbPost" resultMap="red.reksai.resultmap.mapper.TbPostMapper.postResultMap"/>
   *
   *
   *     <resultMap id="postResultMap" type="TbPost">
   *     <id property="postId" column="post_id"/>
   *     <result property="postContent" column="post_content"/>
   *   </resultMap>
   *
   */
  private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings, Class<?> enclosingType) throws Exception {
    //下面的判断其实表现出来一个问题：就是只有<association>、<collection>、<case>的属性中才可以定义一个resultMap，即可以嵌套resultMappings
    if ("association".equals(context.getName())
        || "collection".equals(context.getName())
        || "case".equals(context.getName())) {
      //判断属性select是否存在
      if (context.getStringAttribute("select") == null) {
        //如果不存在select属性，才去调用resultMapElement，解析嵌套
        //验证集合
        validateCollection(context, enclosingType);
        ResultMap resultMap = resultMapElement(context, resultMappings, enclosingType);
        return resultMap.getId();
      }
    }
    return null;
  }

  protected void validateCollection(XNode context, Class<?> enclosingType) {
    if ("collection".equals(context.getName()) && context.getStringAttribute("resultMap") == null
        && context.getStringAttribute("javaType") == null) {
      MetaClass metaResultType = MetaClass.forClass(enclosingType, configuration.getReflectorFactory());
      String property = context.getStringAttribute("property");
      if (!metaResultType.hasSetter(property)) {
        throw new BuilderException(
          "Ambiguous collection type for property '" + property + "'. You must specify 'javaType' or 'resultMap'.");
      }
    }
  }

  private void bindMapperForNamespace() {
    String namespace = builderAssistant.getCurrentNamespace();
    if (namespace != null) {
      Class<?> boundType = null;
      try {
        boundType = Resources.classForName(namespace);
      } catch (ClassNotFoundException e) {
        //ignore, bound type is not required
      }
      if (boundType != null) {
        if (!configuration.hasMapper(boundType)) {
          // Spring may not know the real resource name so we set a flag
          // to prevent loading again this resource from the mapper interface
          // look at MapperAnnotationBuilder#loadXmlResource
          configuration.addLoadedResource("namespace:" + namespace);
          configuration.addMapper(boundType);
        }
      }
    }
  }

}

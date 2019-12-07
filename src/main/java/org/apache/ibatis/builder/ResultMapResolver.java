/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.builder;

import java.util.List;

import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

/**
 * ResultMap解析器
 * @author Eduardo Macarron
 */
public class ResultMapResolver {
  /**
   * MapperBuilderAssistant
   */
  private final MapperBuilderAssistant assistant;
  /**
   * resultMap节点的id属性
   */
  private final String id;
  /**
   * resultMap节点的type属性
   */
  private final Class<?> type;
  /**
   * resultMap节点的extend属性
   */
  private final String extend;
  /**
   * resultMap节点下的discriminator节点对应的对象
   */
  private final Discriminator discriminator;
  /**
   * resultMap节点下的所有ResultMapping
   */
  private final List<ResultMapping> resultMappings;
  /**
   * resultMap是否自动映射
   */
  private final Boolean autoMapping;

  public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, String extend, Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) {
    this.assistant = assistant;
    this.id = id;
    this.type = type;
    this.extend = extend;
    this.discriminator = discriminator;
    this.resultMappings = resultMappings;
    this.autoMapping = autoMapping;
  }

  public ResultMap resolve() {
    //委托MapperBuilderAssistant添加resultMap到Configuration
    return assistant.addResultMap(this.id, this.type, this.extend, this.discriminator, this.resultMappings, this.autoMapping);
  }

}

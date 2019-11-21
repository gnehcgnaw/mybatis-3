/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.executor.keygen;

import java.sql.Statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

/**
 *
 * 主键生成器接口
 *
 * 由于不同的数据库对主键的生成是不一样的：
 *    1. 针对Sequence主键而言，在执行insert sql 前必须指定一个主键值给要插入的记录，如：Oracle 、DB2，KeyGenerator提供了{@link #processAfter(Executor, MappedStatement, Statement, Object)}
 *    2. 针对自增主键的表，在插入是不需要主键，而是在插入过程自动获取一个自增的主键，如：MySQL 、Postgresql ，KeyGenerator给提供了{@link #processAfter(Executor, MappedStatement, Statement, Object)}
 * @author Clinton Begin
 *
 * @see Jdbc3KeyGenerator  只实现了processAfter
 * @see NoKeyGenerator 没有任何实现
 * @see SelectKeyGenerator  实现了processBefore 和 processAfter
 */
public interface KeyGenerator {

  /**
   * 在执行insert之前
   * @param executor
   * @param ms
   * @param stmt
   * @param parameter
   */
  void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

  /**
   * 在执行insert之后
   * @param executor
   * @param ms
   * @param stmt
   * @param parameter
   */
  void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}

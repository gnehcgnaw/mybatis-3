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
package org.apache.ibatis.datasource.pooled;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

import java.util.Properties;

/**
 * 连接池模式
 * 继承了{@link UnpooledDataSourceFactory},但是并没有覆盖{@link UnpooledDataSourceFactory#setProperties(Properties)}和{@link UnpooledDataSourceFactory#getDataSource()}方法，
 * 唯一不同的是初始化的dataSource是不同的：
 *      {@link UnpooledDataSourceFactory} 初始化了{@link org.apache.ibatis.datasource.unpooled.UnpooledDataSource}
 *      {@link PooledDataSourceFactory} 初始化了{@link PooledDataSource}
 * @author Clinton Begin
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }

}

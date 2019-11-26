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
/**
 *
 * Mybatis在进行参数处理、结果集映射等操作时，会涉及大量的反射操作。Java中的反射虽然功能强大，
 * 但是代码编写起来比较复杂容易出错，为了简化反射操作的相关代码，Mybatis提供了专门的反射模块，
 * 该模块位于`org.apache.ibatis.reflection`包中，它对常见的反射操作做了封装，
 * 提供了更加简洁方便的反射API。
 *
 * Reflection utils.
 */
package org.apache.ibatis.reflection;

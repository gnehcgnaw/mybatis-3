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
package org.apache.ibatis.datasource.pooled;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * 连接池数据源
 * 这是一个简单，同步，线程安全的数据库连接池。
 * This is a simple, synchronous, thread-safe database connection pool.
 *
 * @author Clinton Begin
 */
public class PooledDataSource implements DataSource {

  private static final Log log = LogFactory.getLog(PooledDataSource.class);
  /**
   * 通过PoolState管理连接池的转台并记录统计信息
   */
  private final PoolState state = new PoolState(this);

  /**
   * 创建一个PooledDataSource需要一个UnpooledDataSource
   */
  private final UnpooledDataSource dataSource;

  // OPTIONAL CONFIGURATION FIELDS
  // 可选配置字段
  /**
   * 最大活跃连接数
   */
  protected int poolMaximumActiveConnections = 10;
  /**
   * 最大空闲连接数
   */
  protected int poolMaximumIdleConnections = 5;
  /**
   * 最大CheckoutTime时间（最大连接时间）
   */
  protected int poolMaximumCheckoutTime = 20000;
  /**
   * 在无法获取连接时，线程需要等待的时间
   */
  protected int poolTimeToWait = 20000;
  protected int poolMaximumLocalBadConnectionTolerance = 3;
  /**
   * 在检测一个数据库连接是否可用时，会给数据库发送一个测试SQL语句
   */
  protected String poolPingQuery = "NO PING QUERY SET";
  /**
   * 是否允许发送测试SQL
   */
  protected boolean poolPingEnabled;
  /**
   * 当poolPingConnectionsNotUsedFor毫秒未使用时，会发送一次测试SQL语句，检测连接是否正常
   */
  protected int poolPingConnectionsNotUsedFor;
  /**
   * 该hash用于标志着当前的连接池，在构造函数中初始化
   *    生成规则：{@link PooledDataSource#assembleConnectionTypeCode(String, String, String)}
   */
  private int expectedConnectionTypeCode;

  public PooledDataSource() {
    dataSource = new UnpooledDataSource();
  }

  public PooledDataSource(UnpooledDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public PooledDataSource(String driver, String url, String username, String password) {
    dataSource = new UnpooledDataSource(driver, url, username, password);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
  }

  public PooledDataSource(String driver, String url, Properties driverProperties) {
    dataSource = new UnpooledDataSource(driver, url, driverProperties);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
  }

  public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
    dataSource = new UnpooledDataSource(driverClassLoader, driver, url, username, password);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
  }

  public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
    dataSource = new UnpooledDataSource(driverClassLoader, driver, url, driverProperties);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
  }

  /**
   * 获取连接：
   *    首先通过{@link PooledDataSource#popConnection(String, String)}获取{@link PooledConnection}，
   *    因为PooledConnection只实现了{@link InvocationHandler} 接口，并未实现java.sql.Connection，故而这个PooledConnection不能使用，
   *    需要使用{@link PooledConnection#getProxyConnection()}获取一个JDK动态代理生成的实现了java.sql.Connection的代理对象。
   * @return
   * @throws SQLException
   */
  @Override
  public Connection getConnection() throws SQLException {
    return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
  }
  /**
   * 获取连接
   * @return
   * @throws SQLException
   */
  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return popConnection(username, password).getProxyConnection();
  }

  @Override
  public void setLoginTimeout(int loginTimeout) {
    DriverManager.setLoginTimeout(loginTimeout);
  }

  @Override
  public int getLoginTimeout() {
    return DriverManager.getLoginTimeout();
  }

  @Override
  public void setLogWriter(PrintWriter logWriter) {
    DriverManager.setLogWriter(logWriter);
  }

  @Override
  public PrintWriter getLogWriter() {
    return DriverManager.getLogWriter();
  }

  public void setDriver(String driver) {
    dataSource.setDriver(driver);
    forceCloseAll();
  }

  public void setUrl(String url) {
    dataSource.setUrl(url);
    forceCloseAll();
  }

  public void setUsername(String username) {
    dataSource.setUsername(username);
    forceCloseAll();
  }

  public void setPassword(String password) {
    dataSource.setPassword(password);
    forceCloseAll();
  }

  public void setDefaultAutoCommit(boolean defaultAutoCommit) {
    dataSource.setAutoCommit(defaultAutoCommit);
    forceCloseAll();
  }

  public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
    dataSource.setDefaultTransactionIsolationLevel(defaultTransactionIsolationLevel);
    forceCloseAll();
  }

  public void setDriverProperties(Properties driverProps) {
    dataSource.setDriverProperties(driverProps);
    forceCloseAll();
  }

  /**
   * Sets the default network timeout value to wait for the database operation to complete. See {@link Connection#setNetworkTimeout(java.util.concurrent.Executor, int)}
   *
   * @param milliseconds
   *          The time in milliseconds to wait for the database operation to complete.
   * @since 3.5.2
   */
  public void setDefaultNetworkTimeout(Integer milliseconds) {
    dataSource.setDefaultNetworkTimeout(milliseconds);
    forceCloseAll();
  }

  /**
   * The maximum number of active connections.
   *
   * @param poolMaximumActiveConnections The maximum number of active connections
   */
  public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
    this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    forceCloseAll();
  }

  /**
   * The maximum number of idle connections.
   *
   * @param poolMaximumIdleConnections The maximum number of idle connections
   */
  public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
    this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    forceCloseAll();
  }

  /**
   * The maximum number of tolerance for bad connection happens in one thread
   * which are applying for new {@link PooledConnection}.
   *
   * @param poolMaximumLocalBadConnectionTolerance
   * max tolerance for bad connection happens in one thread
   *
   * @since 3.4.5
   */
  public void setPoolMaximumLocalBadConnectionTolerance(
      int poolMaximumLocalBadConnectionTolerance) {
    this.poolMaximumLocalBadConnectionTolerance = poolMaximumLocalBadConnectionTolerance;
  }

  /**
   * The maximum time a connection can be used before it *may* be
   * given away again.
   *
   * @param poolMaximumCheckoutTime The maximum time
   */
  public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
    this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    forceCloseAll();
  }

  /**
   * The time to wait before retrying to get a connection.
   *
   * @param poolTimeToWait The time to wait
   */
  public void setPoolTimeToWait(int poolTimeToWait) {
    this.poolTimeToWait = poolTimeToWait;
    forceCloseAll();
  }

  /**
   * The query to be used to check a connection.
   *
   * @param poolPingQuery The query
   */
  public void setPoolPingQuery(String poolPingQuery) {
    this.poolPingQuery = poolPingQuery;
    forceCloseAll();
  }

  /**
   * Determines if the ping query should be used.
   *
   * @param poolPingEnabled True if we need to check a connection before using it
   */
  public void setPoolPingEnabled(boolean poolPingEnabled) {
    this.poolPingEnabled = poolPingEnabled;
    forceCloseAll();
  }

  /**
   * If a connection has not been used in this many milliseconds, ping the
   * database to make sure the connection is still good.
   *
   * @param milliseconds the number of milliseconds of inactivity that will trigger a ping
   */
  public void setPoolPingConnectionsNotUsedFor(int milliseconds) {
    this.poolPingConnectionsNotUsedFor = milliseconds;
    forceCloseAll();
  }

  public String getDriver() {
    return dataSource.getDriver();
  }

  public String getUrl() {
    return dataSource.getUrl();
  }

  public String getUsername() {
    return dataSource.getUsername();
  }

  public String getPassword() {
    return dataSource.getPassword();
  }

  public boolean isAutoCommit() {
    return dataSource.isAutoCommit();
  }

  public Integer getDefaultTransactionIsolationLevel() {
    return dataSource.getDefaultTransactionIsolationLevel();
  }

  public Properties getDriverProperties() {
    return dataSource.getDriverProperties();
  }

  /**
   * @since 3.5.2
   */
  public Integer getDefaultNetworkTimeout() {
    return dataSource.getDefaultNetworkTimeout();
  }

  public int getPoolMaximumActiveConnections() {
    return poolMaximumActiveConnections;
  }

  public int getPoolMaximumIdleConnections() {
    return poolMaximumIdleConnections;
  }

  public int getPoolMaximumLocalBadConnectionTolerance() {
    return poolMaximumLocalBadConnectionTolerance;
  }

  public int getPoolMaximumCheckoutTime() {
    return poolMaximumCheckoutTime;
  }

  public int getPoolTimeToWait() {
    return poolTimeToWait;
  }

  public String getPoolPingQuery() {
    return poolPingQuery;
  }

  public boolean isPoolPingEnabled() {
    return poolPingEnabled;
  }

  public int getPoolPingConnectionsNotUsedFor() {
    return poolPingConnectionsNotUsedFor;
  }

  /**
   * `PooledDataSource.forceCloseAll()`，当修改`PooledDataSource`的字段是，例如数据库的`URL`、`用户名`、`密码`、`autoCommit`配置等，
   * 都会调用`PooledDataSource.forceCloseAll()`方法将所有的数据库连接都关掉，同时也会将相应的`PooledConnection`对象都设置为无效，
   * 清空`activeConnections`集合和`idleConnections`集合。应用系统之后通过`PoolDataSource.getConnection()`获取连接时，
   * 会按照新的配置重新创建新的数据库连接以及对应的`PooledConnection`对象.
   * Closes all active and idle connections in the pool.
   */
  public void forceCloseAll() {
    synchronized (state) {
      expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
      for (int i = state.activeConnections.size(); i > 0; i--) {
        try {
          PooledConnection conn = state.activeConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
          realConn.close();
        } catch (Exception e) {
          // ignore
        }
      }
      for (int i = state.idleConnections.size(); i > 0; i--) {
        try {
          PooledConnection conn = state.idleConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
          realConn.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("PooledDataSource forcefully closed/removed all connections.");
    }
  }

  public PoolState getPoolState() {
    return state;
  }

  /**
   * 根据数据库连接的url+username+password生成hash值，这个值用来标识着当前的连接池
   * @param url
   * @param username
   * @param password
   * @return
   */
  private int assembleConnectionTypeCode(String url, String username, String password) {
    return ("" + url + username + password).hashCode();
  }

  /**
   * 放回连接
   * @param conn
   * @throws SQLException
   */
  protected void pushConnection(PooledConnection conn) throws SQLException {

    synchronized (state) {
      //从活跃连接集合中移除此连接
      state.activeConnections.remove(conn);
      //判断此连接是否有效
      if (conn.isValid()) {
        //判断空闲连接数是否小于最大空闲连接数  （即：判断空闲连接数是否达到上限） 以及此连接是否是该连接池的连接
        if (state.idleConnections.size() < poolMaximumIdleConnections && conn.getConnectionTypeCode() == expectedConnectionTypeCode) {
          //空闲连接数没有达到上限
          //累计checkOut时长
          state.accumulatedCheckoutTime += conn.getCheckoutTime();
          //回滚未提交的事务
          if (!conn.getRealConnection().getAutoCommit()) {
            conn.getRealConnection().rollback();
          }
          //为返还连接创造新的PooledConnection对象
          PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
          //然后将新对象添加到活跃集合
          state.idleConnections.add(newConn);
          //设置新连接创建时间戳
          newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
          //设置新连接最后使用时间戳
          newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
          //将老连接对象设置为无效
          conn.invalidate();
          if (log.isDebugEnabled()) {
            log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
          }
          //唤醒等待的线程
          state.notifyAll();
        } else {
          //空闲连接数已达到上限   或   PooledConnection对象不属于该连接池
          //累计checkOur时长
          state.accumulatedCheckoutTime += conn.getCheckoutTime();
          //回滚未提交的操作
          if (!conn.getRealConnection().getAutoCommit()) {
            conn.getRealConnection().rollback();
          }
          //因为这个PooledConnection对象不属于该连接池，所以直接关闭，而不是放回连接池
          conn.getRealConnection().close();
          if (log.isDebugEnabled()) {
            log.debug("Closed connection " + conn.getRealHashCode() + ".");
          }
          //最后再将对象设置为无效
          conn.invalidate();
        }
      } else {
        //如果此连接是无效连接，抛出异常，并且记录先关统计数据
        if (log.isDebugEnabled()) {
          log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
        }
        //统计无效的PooledConnection对象
        state.badConnectionCount++;
      }
    }
  }

  /**
   * 获取连接
   * @param username
   * @param password
   * @return   PooledConnection的代理对象
   * @throws SQLException
   */
  private PooledConnection popConnection(String username, String password) throws SQLException {
    //等待，默认是不等待
    boolean countedWait = false;
    PooledConnection conn = null;
    //创建或判断连接之前系统时间
    long t = System.currentTimeMillis();
    //本地错误连接数
    int localBadConnectionCount = 0;
    //1. 当连接为null的时候，去执行循环
    while (conn == null) {
      synchronized (state) {
        //2. 判断有没有空闲连接
        if (!state.idleConnections.isEmpty()) {
          // Pool has available connection
          //有空闲连接，就获取连接，然后把当前连接从空闲连接中移除
          conn = state.idleConnections.remove(0);
          if (log.isDebugEnabled()) {
            log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
          }
        } else {
          // Pool does not have available connection
          //如果没有空闲连接
          //首先判断活跃连接是不是小于最大活跃数，如果小于可以创建新连接
          if (state.activeConnections.size() < poolMaximumActiveConnections) {
            // Can create new connection
            // 创建一个新连接（这是一个代理对象）
            conn = new PooledConnection(dataSource.getConnection(), this);
            if (log.isDebugEnabled()) {
              log.debug("Created connection " + conn.getRealHashCode() + ".");
            }
          } else {
            // Cannot create new connection
            // 如果判断活跃连接数等于最大活跃数，获取最老的活跃连接
            PooledConnection oldestActiveConnection = state.activeConnections.get(0);
            // 获取此连接的连接时长（当前时间—取出连接的时间）
            long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
            // 然后判断是否超时（此连接的连接时间  和 运行连接的时间  比较）
            if (longestCheckoutTime > poolMaximumCheckoutTime) {
              // 当前连接超时
              // Can claim overdue connection
              //对超时连接进行统计

              //超时连接数+1
              state.claimedOverdueConnectionCount++;
              //总累计超时时间 = 原有总累计超时时间+当前连接时间（因为当前连接已经超时）
              state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
              //总累计连接时间 = 原有总累计连接时间+ 当前连接时间
              state.accumulatedCheckoutTime += longestCheckoutTime;
              //从活跃连接中移除最老的这个超时连接
              state.activeConnections.remove(oldestActiveConnection);
              //获取真正的数据库连接，判断数据库提交模式（自动提交事务还是手动）
              if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                try {
                  //如果是不自动提交事务的情况，那么就要回滚本次操作
                  oldestActiveConnection.getRealConnection().rollback();
                } catch (SQLException e) {
                  /*
                     Just log a message for debug and continue to execute the following
                     statement like nothing happened.
                     Wrap the bad connection with a new PooledConnection, this will help
                     to not interrupt current executing thread and give current thread a
                     chance to join the next competition for another valid/good database
                     connection. At the end of this loop, bad {@link @conn} will be set as null.
                   */
                  log.debug("Bad connection. Could not roll back");
                }
              }
              //重新创建连接
              conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
              //设置该连接创建的时间戳
              conn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
              //设置该连接的最后使用时间
              conn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());
              //然后作废之前的最老的超时连接，因为此前只是从集合中移除，并不表示它不能使用，而这一步就是确保这种情况不会出现。
              oldestActiveConnection.invalidate();
              if (log.isDebugEnabled()) {
                log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
              }
            } else {
              // 没有空闲连接、活跃的连接数又等于限定的最大连接数（即：无法创建连接）而且无超时连接、则只能阻塞等待
              // Must wait
              try {
                //如果此前没有等待的
                if (!countedWait) {
                  //先将等待数+1
                  state.hadToWaitCount++;
                  //然后将状态设置为等待状态
                  countedWait = true;
                }
                if (log.isDebugEnabled()) {
                  log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                }
                //获取当前系统时间
                long wt = System.currentTimeMillis();
                //获取需要等待的时间，利用Object.wait(需要等待的时间)，让当前线程进行等待
                state.wait(poolTimeToWait);
                //更新累计等待时间：累计等待时间=当得系统时间+当前时间-等待直接记录的系统时间
                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
              } catch (InterruptedException e) {
                break;
              }
            }
          }
        }
        // 再次判断连接是否为空
        // 如果不为空
        if (conn != null) {
          // ping to server and check the connection is valid or not
          //判断连接是否有效
          if (conn.isValid()) {
            //如果当前连接不是自动提交事务，那就回滚之前操作
            if (!conn.getRealConnection().getAutoCommit()) {
              conn.getRealConnection().rollback();
            }
            //然后重现设置用于标识该连接所在的连接池的标识码
            conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
            //设置连接时长
            conn.setCheckoutTimestamp(System.currentTimeMillis());
            //设置最后修改时间
            conn.setLastUsedTimestamp(System.currentTimeMillis());
            //把当前连接添加到活跃连接集合中
            state.activeConnections.add(conn);
            //然后把连接次数+1
            state.requestCount++;
            //累计请求连接时间
            state.accumulatedRequestTime += System.currentTimeMillis() - t;
          } else {
            //如果当前连接不为空，但是是失效的，那么表明此连接是一个坏连接（无效连接）
            if (log.isDebugEnabled()) {
              log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
            }
            //无效连接数+1
            state.badConnectionCount++;
            //本地错误连接数+1
            localBadConnectionCount++;
            //设置连接为空
            conn = null;
            if (localBadConnectionCount > (poolMaximumIdleConnections + poolMaximumLocalBadConnectionTolerance)) {
              if (log.isDebugEnabled()) {
                log.debug("PooledDataSource: Could not get a good connection to the database.");
              }
              throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
            }
          }
        }
      }

    }
    //此时连接为空，表明发生了未知错误
    if (conn == null) {
      if (log.isDebugEnabled()) {
        log.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
      }
      throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
    }
    //最后返回连接
    return conn;
  }

  /**
   * 用于测试连接
   * Method to check to see if a connection is still usable
   *
   * @param conn - the connection to check
   * @return True if the connection is still usable
   */
  protected boolean pingConnection(PooledConnection conn) {
    boolean result = true;

    try {
      //检测真正的连接是否已关闭
      result = !conn.getRealConnection().isClosed();
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
      }
      result = false;
    }

    if (result) {
      //判断要不要发不出测试语句
      if (poolPingEnabled) {
        //要
        //
        if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
          try {
            if (log.isDebugEnabled()) {
              log.debug("Testing connection " + conn.getRealHashCode() + " ...");
            }
            Connection realConn = conn.getRealConnection();
            try (Statement statement = realConn.createStatement()) {
              statement.executeQuery(poolPingQuery).close();
            }
            if (!realConn.getAutoCommit()) {
              realConn.rollback();
            }
            result = true;
            if (log.isDebugEnabled()) {
              log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
            }
          } catch (Exception e) {
            log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
            try {
              conn.getRealConnection().close();
            } catch (Exception e2) {
              //ignore
            }
            result = false;
            if (log.isDebugEnabled()) {
              log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Unwraps a pooled connection to get to the 'real' connection
   *
   * @param conn - the pooled connection to unwrap
   * @return The 'real' connection
   */
  public static Connection unwrapConnection(Connection conn) {
    if (Proxy.isProxyClass(conn.getClass())) {
      InvocationHandler handler = Proxy.getInvocationHandler(conn);
      if (handler instanceof PooledConnection) {
        return ((PooledConnection) handler).getRealConnection();
      }
    }
    return conn;
  }

  @Override
  protected void finalize() throws Throwable {
    forceCloseAll();
    super.finalize();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(getClass().getName() + " is not a wrapper.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return false;
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }

}

package red.reksai.cache;

import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.impl.PerpetualCache;


/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/2 00:44
 */
public class BlockingCacheTest {
  public static void main(String[] args) {

    MyThread myThread = new MyThread();
    Thread thread1 = new Thread(myThread);
    Thread thread2 = new Thread(myThread);
    thread1.start();
    thread2.start();
  }
}

class MyThread extends  Thread {
  public static BlockingCache blockingCache ;
  static {
    PerpetualCache perpetualCache = new PerpetualCache("namespace1");
    perpetualCache.putObject("key","aaa");
    blockingCache = new BlockingCache(perpetualCache);
  }

  @Override
  public void run() {
    System.out.println(Thread.currentThread().getName()+">>>>>>"+blockingCache.getObject("key1"));
  }

}

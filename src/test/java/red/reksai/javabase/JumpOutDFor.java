package red.reksai.javabase;

import java.util.Arrays;
import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/22 23:36
 */
public class JumpOutDFor {
  public static void main(String[] args) {
    List<VFS> vfsList = Arrays.asList(new MyVFS1(),new MyVFS2() ,new MyVFS3());
    VFS vfs = null ;
    for (int i = 0; vfs ==null || !vfs.isValid(); i++) {
      vfs = vfsList.get(i);
      if (!vfsList.get(i).isValid()){
        System.out.println("当前"+vfsList.get(i)+"是无效的。");
      }
    }
    System.out.println(vfs);
  }
}

abstract  class VFS {
  public abstract boolean isValid();
}

class MyVFS1 extends VFS {

  @Override
  public boolean isValid() {
    return false;
  }
}


class MyVFS2 extends VFS {

  @Override
  public boolean isValid() {
    return false;
  }
}

class MyVFS3 extends VFS {

  @Override
  public boolean isValid() {
    return true;
  }
}

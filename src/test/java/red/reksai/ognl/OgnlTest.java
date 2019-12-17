package red.reksai.ognl;

import ognl.OgnlContext;
import org.junit.Before;
import red.reksai.resultmap.entity.TbAuthor;
import red.reksai.resultmap.entity.TbBlog;
import red.reksai.resultmap.entity.TbPost;

import java.awt.*;
import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/16 10:29
 */
public class OgnlTest {
  private static TbBlog tbBlog ;
  private static TbAuthor tbAuthor ;
  private static List<TbPost> postList ;
  /**
   * 上下文对象
   */
  private static OgnlContext ognlContext ;

  @Before
  public void start(){
    TbBlog.staticField="static Field" ;
    tbAuthor = new TbAuthor(1,"username","password","email");
    TbPost tbPost = new TbPost() ;
  }
}

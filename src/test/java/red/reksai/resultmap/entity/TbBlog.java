package red.reksai.resultmap.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 博客  entity
 */
@Data
public class TbBlog implements Serializable{

  public TbBlog() {
  }

  public TbBlog(Integer blogId) {
    this.blogId = blogId;
  }

  private int blogId;

  private String blogTitle;

  private TbAuthor tbAuthor ;

  private List<TbPost> tbPosts ;
}


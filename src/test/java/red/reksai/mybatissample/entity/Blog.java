package red.reksai.mybatissample.entity;

import java.util.Date;

/**
 *
 * 创建POJO对象，将create_time和modify_time的类型定义为Date
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 23:21
 */
public class  Blog{
  private Integer blogId;
  private String blogTitle;
  private String blogContext ;
  private Date createTime ;
  private Date modifyTime ;

  public Integer getBlogId() {
    return blogId;
  }

  public void setBlogId(Integer blogId) {
    this.blogId = blogId;
  }

  public String getBlogTitle() {
    return blogTitle;
  }

  public void setBlogTitle(String blogTitle) {
    this.blogTitle = blogTitle;
  }

  public String getBlogContext() {
    return blogContext;
  }

  public void setBlogContext(String blogContext) {
    this.blogContext = blogContext;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(Date modifyTime) {
    this.modifyTime = modifyTime;
  }

  public Blog() {
  }

  public Blog(Integer blogId, String blogTitle, String blogContext, Date createTime, Date modifyTime) {
    this.blogId = blogId;
    this.blogTitle = blogTitle;
    this.blogContext = blogContext;
    this.createTime = createTime;
    this.modifyTime = modifyTime;
  }

  @Override
  public String toString() {
    return "Blog{" +
      "blogId=" + blogId +
      ", blogTitle='" + blogTitle + '\'' +
      ", blogContext='" + blogContext + '\'' +
      ", createTime=" + createTime +
      ", modifyTime=" + modifyTime +
      '}';
  }
}

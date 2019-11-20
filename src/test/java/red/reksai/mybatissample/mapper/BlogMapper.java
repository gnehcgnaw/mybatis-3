package red.reksai.mybatissample.mapper;

import org.apache.ibatis.annotations.Param;
import red.reksai.mybatissample.entity.Blog;

import java.util.Map;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/20 11:13
 */
public interface BlogMapper {
  Map selectBlog(int id);

  int insertBlog(@Param("blog") Blog blog);
}

package red.reksai.bingding.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/8 16:25
 */
@Mapper
public interface CommentMapper {
  @Select("select * from tb_comment where comment_id = #{id}")
  public Map selectTbComment(@Param("id")int id);
}

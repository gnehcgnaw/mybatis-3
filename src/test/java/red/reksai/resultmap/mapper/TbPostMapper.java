package red.reksai.resultmap.mapper;

import org.apache.ibatis.annotations.Param;
import red.reksai.resultmap.entity.TbPost;

import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/3 15:56
 */
public interface TbPostMapper {

  List<TbPost> selectPost(@Param("id")int id);
}

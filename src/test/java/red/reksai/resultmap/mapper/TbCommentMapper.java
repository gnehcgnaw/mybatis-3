package red.reksai.resultmap.mapper;


import org.apache.ibatis.annotations.Param;
import red.reksai.resultmap.entity.TbComment;

import java.util.List;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/3 15:56
 */
public interface TbCommentMapper {

  TbComment selectComment(@Param("id") int id);
}

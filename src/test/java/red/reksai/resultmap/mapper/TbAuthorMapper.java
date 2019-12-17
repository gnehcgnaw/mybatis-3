package red.reksai.resultmap.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import red.reksai.resultmap.entity.TbAuthor;
import red.reksai.resultmap.entity.TbBlog;
import red.reksai.resultmap.entity.TbComment;

import java.util.List;
import java.util.Map;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/3 15:56
 */
public interface TbAuthorMapper {

    List<TbAuthor> selectAuthor(@Param("id") int id);

    int insertAuthor(@Param("tbAuthor") TbAuthor tbAuthor);

    @Select("select author_id , author_username from tb_author where author_username = #{name} order by author_id desc")
    @MapKey("author_username")
    Map selectAuthorByName(@Param("name")String name);
}

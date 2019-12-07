package red.reksai.resultmap.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 帖子/博文 entity
 */
@Data
public class TbPost implements Serializable {

    private int postId;

    private String postContent;

    private int postDraft;

    private List<TbComment> tbComments ;

}


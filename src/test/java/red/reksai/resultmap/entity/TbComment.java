package red.reksai.resultmap.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论 entity
 */
@Data
public class TbComment implements Serializable {

    private int commentId;

    private String commentContent;

}


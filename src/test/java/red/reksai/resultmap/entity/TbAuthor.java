package red.reksai.resultmap.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 作者 entity
 */
@Data
public class TbAuthor implements Serializable {

    private int authorId;

    private String authorUsername;

    private String authorPassword;

    private String authorEmail;

}


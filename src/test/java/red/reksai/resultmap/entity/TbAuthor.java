package red.reksai.resultmap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 作者 entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbAuthor implements Serializable {

    private int authorId;

    private String authorUsername;

    private String authorPassword;

    private String authorEmail;

}


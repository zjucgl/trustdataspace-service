package sz.lab.entity.orga.user;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 账号人员信息表，用于记录账号分配的人员id。
 * </p>
 */
@Getter
@Setter
@TableName("orga_user_person")
public class UserPersonEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，外键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，外键
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 人员ID，外键
     */
    @TableField(value = "person_id")
    private Integer personId;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}

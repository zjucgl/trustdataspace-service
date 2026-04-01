package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 角色权限表
 **/
@Getter
@Setter
@TableName("system_permission")
public class SystemPermissionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID，主键
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;
    /**
     * 角色id，外键
     */
    @TableField(value = "role_id")
    private Integer roleId;
    /**
     * 菜单id，外键
     */
    @TableField(value = "menu_id")
    private Integer menuId;
    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}

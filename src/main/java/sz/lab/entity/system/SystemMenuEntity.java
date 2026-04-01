package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 系统菜单表，用于记录菜单信息。
 * </p>
 */
//要设置autoResultMap = true,否则查询数组时，返回结果为 null
@Getter
@Setter
@TableName(value = "system_menu", autoResultMap = true)
public class SystemMenuEntity implements Serializable  {
    private static final long serialVersionUID = 1L;
    /**
     * 菜单ID，主键
     */
    @TableId(value = "menu_id", type = IdType.INPUT)
    private Integer menuId;

    /**
     * 菜单路径
     */
    @TableField(value = "menu_code")
    private String menuCode;

    /**
     * 菜单名称
     */
    @TableField(value = "menu_name")
    private String menuName;

    /**
     * 菜单图标
     */
    @TableField(value = "menu_icon")
    private String menuIcon;

    /**
     * 菜单排序
     */
    @TableField(value = "menu_rank")
    private Integer menuRank;

    /**
     * 父菜单id
     */
    @TableField(value = "menu_father")
    private Integer menuFather;

    /**
     * 菜单级别
     */
    @TableField(value = "menu_type")
    private Integer menuType;

//    /**
//     * 菜单角色权限
//     */
//    @TableField(value = "menu_roles", typeHandler = JacksonTypeHandler.class)
//    private List<String> menuRoles;

    /**
     * 0为false，1为true
     */
    @TableField(value = "menu_show")
    private Integer menuShow;


}

package sz.lab.entity.orga.dept;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * <p>
 * 部门（实验室）信息表，公司ID和部门ID在配置文件中设置，以实验室为基本使用单位，设计实验室支持二级组织架构，不支持三级，程序中提示加锁定
 * </p>
 *
 * @author ckd
 * @since 2023-11-24
 */
@Getter
@Setter
@TableName("orga_dept")
public class DeptEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * 部门ID，主键
    */
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Integer deptId;
    /**
     * 参与者Id
     */
    @TableField(value = "participant_id")
    private String participantId;

    /**
    * 部门名称
    */
    @TableField(value = "dept_name")
    private String deptName;

    /**
    * 备注信息
    */
    @TableField(value = "dept_info")
    private String deptInfo;

    /**
    * 上级部门ID
    */
    @TableField(value = "dept_father")
    private Integer deptFather;

    /**
    * 部门排序
    */
    @TableField(value = "dept_sort")
    private Integer deptSort;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

    /**
     * 更新时间
     */
    @TableField(value = "gmt_modify", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;

    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;



}

package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 系统功能表
 **/
@Getter
@Setter
@TableName("system_function")
public class SystemFunctionEntity implements Serializable {
    /**
     * ID，主键
     */
    @TableId(value = "function_id", type = IdType.AUTO)
    private Long functionId;

    /**
     * 功能代码
     */
    @TableField(value = "function_code")
    private String functionCode;

    /**
     * 功能名称
     */
    @TableField(value = "function_name")
    private String functionName;

    /**
     * 功能状态，0为关闭，1为开启
     */
    @TableField(value = "function_status")
    private Integer functionStatus;
    /**
     * 功能介绍
     */
    @TableField(value = "function_info")
    private String functionInfo;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}

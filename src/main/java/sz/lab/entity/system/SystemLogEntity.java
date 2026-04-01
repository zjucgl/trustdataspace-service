package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 系统日志表
 **/
@Getter
@Setter
@TableName("system_log")
public class SystemLogEntity implements Serializable {

    /**
     * 日志ID，主键
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 用户id，外键
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 操作类型
     */
    @TableField(value = "log_type")
    private String logType;

    /**
     * 操作内容
     */
    @TableField(value = "log_content")
    private String logContent;
    /**
     * 操作结果
     */
    @TableField(value = "log_status")
    private String logStatus;
    /**
     * 数据来源
     */
    @TableField(value = "log_source")
    private String logSource;
    /**
     * 操作方法名称
     */
    @TableField(value = "log_method")
    private String logMethod;
    /**
     * ip地址
     */
    @TableField(value = "log_ip")
    private String logIp;
    /**
     * 异常报错
     */
    @TableField(value = "log_error")
    private String logError;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}

package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 敦煌文件表
 **/
@Getter
@Setter
@TableName("ip_file")
public class IpFileEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 文件链接
     */
    @TableField(value = "path")
    private String path;

    /**
     * 文件后缀
     */
    @TableField(value = "suffix")
    private String suffix;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}

package sz.lab.entity.basic;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 个人资产表
 **/
@Getter
@Setter
@TableName("basic_personal_asset")
public class PersonalAssetEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 个人资产ID，主键
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 资产名称，外键
     */
    @TableField(value = "asset_id")
    private String assetId;

    @TableField(value = "eth_asset_id")
    private String ethAssetId;
    /**
     * 提供者did
     */
    @TableField(value = "participant_id")
    private String participantId;
    /**
     * 资产端点链接
     */
    @TableField(value = "endpoint_url")
    private String endpointURL;
    /**
     * 账号id，外键
     */
    @TableField(value = "user_id")
    private Integer userId;
    /**
     * 下载请求头
     */
    @TableField(value = "authorization")
    private String authorization;
    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}

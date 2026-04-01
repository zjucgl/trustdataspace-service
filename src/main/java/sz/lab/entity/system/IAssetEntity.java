package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description: 数据从表
 **/
@Getter
@Setter
@TableName("system_asset")
public class IAssetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID，主键
     */
    @TableId(value = "system_asset_id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "edc_asset_id")
    private String assetId;

    @TableField(value = "system_asset_quantity")
    private Long assetQuantity;

    @TableField(value = "system_asset_price")
    private Long assetPrice;

    @TableField(value = "asset_description")
    private String assetDescription;

    @TableField(value = "user_id")
    private Integer userId;
    @TableField(value = "path")
    private String path;

    @TableField(value = "is_delete")
    private Integer isDelete;

    @TableField(value = "is_first")
    private Integer isFirst;

    @TableField(value = "has_policy")
    private Integer hasPolicy;


}

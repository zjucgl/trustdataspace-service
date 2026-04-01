package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author master
 * @since 2025-04-15
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("system_nodeInfo")
public class SystemNodeinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("node_id")
    private String nodeId;

    @TableField("en_code")
    private String enCode;

    @TableField("head_id")
    private String headId;

    @TableField("main_node")
    private Integer mainNode;

    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    @TableField("is_delete")
    private Integer isDelete;

    @TableField("block_number")
    private BigInteger blockNumber;

    @TableField("node_name")
    private String nodeName;

}

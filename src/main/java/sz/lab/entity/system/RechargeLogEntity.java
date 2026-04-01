package sz.lab.entity.system;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;


/**
 * @Description: 充钱日志
 **/
@Getter
@Setter
@Accessors(chain = true)
@TableName("recharge_log")
public class RechargeLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 矿工
     */
    @TableField(value = "from_id")
    private Integer fromId;

    /**
     * 矿工区块链账号
     */
    @TableField(value = "from_eth_account")
    private String fromEthAccount;

    /**
     * 给谁充钱
     */
    @TableField("user_id")
    private Integer userId;


    /**
     * 充钱用户姓名
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 充钱用户区块链账号
     */
    @TableField(value = "user_eth_account")
    private String userEthAccount;

    /**
     * 充钱金额
     */
    @TableField(value = "amount")
    private BigInteger amount;

    /**
     * 交易哈希值
     */
    @TableField(value = "tx_hash")
    private String txHash;

    /**
     * 充钱时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "recharge_time", fill = FieldFill.INSERT)
    private LocalDateTime rechargeTime;

    /**
     * 充值状态，0表示审核中，1表示审核通过，2表示审核不通过
     */
    @TableField(value = "status")
    private Integer status;

}

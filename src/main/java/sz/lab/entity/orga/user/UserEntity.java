package sz.lab.entity.orga.user;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * <p>
 * 用户信息表，用于记录用户账号信息。
 * </p>
 */
@Getter
@Setter
@TableName("orga_user")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
    * 用户ID，主键
    */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;
    /**
    * 登录账号
    */
    @TableField(value = "login_code")
    private String loginCode;
    /**
    * 登录密码
    */
    @TableField(value = "login_pwd")
    private String loginPwd;
    /**
    * 用户姓名
    */
    @TableField(value = "user_name")
    private String userName;
    /**
    * 部门ID，外键
    */
    @TableField(value = "dept_id")
    private Integer deptId;
    /**
    * 手机号码，必填
    */
    @TableField(value = "user_phone")
    private String userPhone;

    /**
     * 区块链账号，必填
     */
    @TableField(value = "eth_account")
    private String ethAccount;
    /**
    * 区块链密钥
    */
    @TableField(value = "eth_credentials")
    private String ethCredentials;
    /**
    * 备注信息
    */
    @TableField(value = "user_info")
    private String userInfo;
    /**
     * 审核状态，0未通过，1通过
     */
    @TableField(value = "verify_status")
    private Integer verifyStatus;
    /**
    * 上次登录时间
    */
    @TableField(value = "gmt_last_login")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtLastLogin;
    /**
     * 上次修改密码时间
     */
    @TableField(value = "pwd_last_update", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pwdLastUpdate;
    /**
     * 创建时间
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    /**
     * 修改时间
     */
    @TableField(value = "gmt_modify", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;
    /**
     * 是否删除，0-未删除，1-已删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    /**
     * 金额
     */
    @TableField(value = "user_deposits")
    private Integer userDeposits;

    /**
     *额外的钱
     */
    @TableField(value = "user_deposits_extra")
    private Integer userDepositsExtra;
}

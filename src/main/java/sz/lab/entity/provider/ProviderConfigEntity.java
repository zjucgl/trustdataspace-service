package sz.lab.entity.provider;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("provider_config")
public class ProviderConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_id")
    private Integer deptId;

    @TableField("provider_name")
    private String providerName;

    @TableField("provider_label")
    private String providerLabel;

    @TableField("participant_id")
    private String participantId;

    @TableField("controlplane_mgmt_port")
    private Integer controlplaneMgmtPort;

    @TableField("controlplane_protocol_port")
    private Integer controlplaneProtocolPort;

    @TableField("controlplane_public_port")
    private Integer controlplanePublicPort;

    @TableField("dataplane_public_port")
    private Integer dataplanePublicPort;

    @TableField("identity_hub_port")
    private Integer identityHubPort;

    @TableField("sts_port")
    private Integer stsPort;

    @TableField("db_host")
    private String dbHost;

    @TableField("db_port")
    private Integer dbPort;

    @TableField("db_name")
    private String dbName;

    @TableField("db_readonly_user")
    private String dbReadonlyUser;

    @TableField("db_readonly_pwd")
    private String dbReadonlyPwd;

    @TableField("deploy_host")
    private String deployHost;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

    @TableField(value = "gmt_modify", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;
}

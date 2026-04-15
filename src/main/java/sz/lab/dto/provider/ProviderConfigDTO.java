package sz.lab.dto.provider;

import lombok.Data;

@Data
public class ProviderConfigDTO {
    private Long id;
    private Integer deptId;
    private String providerName;
    private String providerLabel;
    private String participantId;
    private Integer controlplaneMgmtPort;
    private Integer controlplaneProtocolPort;
    private Integer controlplanePublicPort;
    private Integer dataplanePublicPort;
    private Integer identityHubPort;
    private Integer stsPort;
    private String dbHost;
    private Integer dbPort;
    private String dbName;
    private String dbReadonlyUser;
    private String dbReadonlyPwd;
    private String deployHost;
    private String status;
    private String remark;
    private String deptName;
}

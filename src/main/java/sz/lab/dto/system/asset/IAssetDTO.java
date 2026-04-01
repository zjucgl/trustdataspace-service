package sz.lab.dto.system.asset;

import lombok.Data;

@Data
public class IAssetDTO {
    private Integer id;
    private String assetId;
    private Long assetQuantity;
    private Long assetPrice;
    private String assetDescription;
    private Integer userId;
    private String userName;
    private String deptName;
    private String path;
    private Integer hasPolicy;
}

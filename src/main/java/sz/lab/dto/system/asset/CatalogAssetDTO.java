package sz.lab.dto.system.asset;

import lombok.Data;

@Data
public class CatalogAssetDTO {
    private String id;
    private String description;
    private String baseUrl;
    private Long assetQuantity;
    private Long assetPrice;
}

package sz.lab.dto.trace;


import lombok.Data;

import java.math.BigInteger;

@Data
public class AssetDTO {
    private String assetId;
    private String name;
    private String description;
    private BigInteger price;
    private String status;
}

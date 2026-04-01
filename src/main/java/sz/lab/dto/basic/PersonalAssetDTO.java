package sz.lab.dto.basic;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PersonalAssetDTO {
    private Long id;
    private String assetId;
    private String ethAssetId;
    private String participantId;
    private String endpointURL;
    private Integer userId;
    private String userName;
    private String authorization;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    private String deptName;
}

package sz.lab.dto.orga;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeptTreeDTO {
    private Integer deptId;
    private String participantId;
    private String deptName;
    private String deptInfo;
    private Integer deptFather;
    private Integer deptSort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    private boolean isLeaf;
    private List<DeptTreeDTO> children;
}

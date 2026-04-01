package sz.lab.dto.orga;

import lombok.Data;

@Data
public class DeptDTO {
    private Integer deptId;
    private String deptName;
    private String deptInfo;
    private Integer deptFather;
    private Integer deptSort;
}

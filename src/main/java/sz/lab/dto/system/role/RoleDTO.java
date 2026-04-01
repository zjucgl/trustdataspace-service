package sz.lab.dto.system.role;

import lombok.Data;

import java.util.List;

@Data
public class RoleDTO {
    private Integer roleId;
    private String roleName;
    private String roleCode;
    private String roleInfo;
    private List<Integer> menuList;
}

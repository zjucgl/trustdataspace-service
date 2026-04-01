package sz.lab.dto.system.menu;

import lombok.Data;

import java.util.List;

@Data
public class MenuTreeDTO {
    private Integer menuId;
    private String menuName;
    private List<MenuTreeDTO> children;
}

package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.role.RoleDTO;
import sz.lab.service.system.menu.SystemMenuService;
import sz.lab.service.system.role.RoleService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description: 角色管理
 */
@Validated
@RequestMapping("/role")
@RestController
public class RoleController extends BaseController {
    @Resource
    private RoleService roleService;
    @Resource
    private SystemMenuService menuService;
    /**
     * @Description: 分页查询
     **/
    @PostMapping("/list")
    public OperateResultDTO list(@RequestBody TableRequestDTO tableRequestDTO) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = roleService.pageList(tableRequestDTO);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 菜单树查询
     **/
    @PostMapping("/menuTree")
    public OperateResultDTO menuTree() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = menuService.getMenuTree();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 角色map查询
     **/
    @PostMapping("/roleMap")
    public OperateResultDTO getRoleMap() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = roleService.getRoleMap();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 新增角色
     **/
    @PostMapping("/add")
    public OperateResultDTO add(@RequestBody RoleDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = roleService.add(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 修改角色
     **/
    @PostMapping("/modify")
    public OperateResultDTO update(@RequestBody RoleDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = roleService.update(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 修改角色
     **/
    @PostMapping("/modifyPermission")
    public OperateResultDTO updatePermission(@RequestBody RoleDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = roleService.updatePermission(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 删除角色
     **/
    @PostMapping("/remove")
    public OperateResultDTO remove(@RequestBody Map<String, List<Integer>> map) {
        OperateResultDTO operateResultDTO = new OperateResultDTO(true,"成功",true);
        try {
            operateResultDTO.setResult(roleService.removeByIds(map.get("ids")));
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
}

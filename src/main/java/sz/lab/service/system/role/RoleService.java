package sz.lab.service.system.role;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.role.RoleDTO;
import sz.lab.entity.system.RoleEntity;

/**
 * <p>
 * 系统角色表，用于记录角色信息。 服务类
 * </p>
 */
public interface RoleService extends IService<RoleEntity> {
    /**
     * @Description: 分页查询
     **/
    OperateResultDTO pageList(TableRequestDTO tableRequestDTO);
    /**
     * @Description: 角色map
     **/
    OperateResultDTO getRoleMap();
    /**
     * @Description: 新增角色
     **/
    OperateResultDTO add(RoleDTO roleDTO);
    /**
     * @Description: 修改角色
     **/
    OperateResultDTO update(RoleDTO roleDTO);
    /**
     * @Description: 修改角色权限
     **/
    OperateResultDTO updatePermission(RoleDTO roleDTO);

}

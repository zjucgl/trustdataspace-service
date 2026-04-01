package sz.lab.service.system.permission;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.role.RoleDTO;
import sz.lab.entity.system.SystemPermissionEntity;

public interface SystemPermissionService extends IService<SystemPermissionEntity> {
    /**
     * @Description: 修改角色权限
     **/
    OperateResultDTO updatePermission(RoleDTO roleDTO);
}

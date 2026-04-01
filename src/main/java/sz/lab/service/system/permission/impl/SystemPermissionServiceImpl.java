package sz.lab.service.system.permission.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.role.RoleDTO;
import sz.lab.entity.system.SystemPermissionEntity;
import sz.lab.mapper.system.permission.SystemPermissionMapper;
import sz.lab.service.system.permission.SystemPermissionService;

import java.util.List;

@Service
public class SystemPermissionServiceImpl extends ServiceImpl<SystemPermissionMapper, SystemPermissionEntity> implements SystemPermissionService {
    @Override
    public OperateResultDTO updatePermission(RoleDTO roleDTO) {
        //删除原来的权限
        remove(Wrappers.lambdaQuery(SystemPermissionEntity.class)
                .eq(SystemPermissionEntity::getRoleId,roleDTO.getRoleId()));
        List<Integer> menuList = roleDTO.getMenuList();
        Integer roleId = roleDTO.getRoleId();
        //插入新权限
        for(Integer menuId:menuList){
            SystemPermissionEntity entity = new SystemPermissionEntity();
            entity.setRoleId(roleId);
            entity.setMenuId(menuId);
            save(entity);
        }
        return new OperateResultDTO(true,"成功",true);
    }

}

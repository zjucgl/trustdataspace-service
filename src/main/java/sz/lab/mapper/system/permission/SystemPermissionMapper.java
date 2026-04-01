package sz.lab.mapper.system.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sz.lab.entity.system.SystemPermissionEntity;

import java.util.List;

@Mapper
public interface SystemPermissionMapper extends BaseMapper<SystemPermissionEntity> {
    @Select({"SELECT permission_id, role_id, menu_id, gmt_create " +
            "FROM system_permission " +
            "WHERE menu_id = #{menuId}"})
    List<SystemPermissionEntity> selectPermissionsByMenuId(Integer menuId);
    @Select({"SELECT role_id " +
            "FROM system_permission " +
            "WHERE menu_id = #{menuId}"})
    List<Integer> selectPermissionRoleIdsByMenuId(Integer menuId);
}

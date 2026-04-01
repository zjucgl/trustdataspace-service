package sz.lab.mapper.system.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sz.lab.entity.system.RoleEntity;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
    /**
     * @Description: 查询所有角色信息
     **/
    @Select({"SELECT role_id, role_code, role_name, role_info, " +
            "gmt_create, gmt_modify " +
            "FROM system_role " +
            "WHERE is_deleted != 1 "})
    List<RoleEntity> selectRoleList();
}

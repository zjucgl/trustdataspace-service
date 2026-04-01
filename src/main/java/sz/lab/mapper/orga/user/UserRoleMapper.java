package sz.lab.mapper.orga.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sz.lab.entity.orga.user.UserRoleEntity;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {
    /**
     * @Description: 根据用户ID查询角色ID列表
     **/
    @Select({"SELECT role_id " +
            "FROM orga_user_role " +
            "WHERE user_id = #{userId}"})
    List<Integer> selectRoleIdsByUserId(@Param("userId") Integer userId);

    /**
     * @Description: 根据用户ID列表查询用户角色列表
     **/
    @Select({"<script>" +
            "SELECT user_id, role_id " +
            "FROM orga_user_role " +
            "WHERE user_id IN " +
            "<foreach item='userId' index='index' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>"})
    List<UserRoleEntity> selectRoleListByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * @Description: 插入用户角色关联
     **/
    @Insert({"INSERT INTO orga_user_role (user_id, role_id, gmt_create) " +
            "VALUES (#{userRoleEntity.userId}, #{userRoleEntity.roleId}, " +
            "CURRENT_TIMESTAMP)"})
    @Options(useGeneratedKeys = true, keyProperty = "userRoleEntity.userRoleId", keyColumn = "user_role_id")
    void insertUserRole(@Param("userRoleEntity") UserRoleEntity userRoleEntity);

    /**
     * @Description: 根据用户ID删除用户角色关联
     **/
    @Delete({"DELETE FROM orga_user_role " +
            "WHERE user_id = #{userId}"})
    void deleteByUserId(@Param("userId") Integer userId);
}

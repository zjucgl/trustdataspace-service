package sz.lab.mapper.orga.user;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sz.lab.entity.orga.user.UserEntity;

import java.util.List;

/**
 * <p>
 * 用户信息表，用于记录用户账号信息。 Mapper 接口
 * </p>
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    /**
     * @Description: 分页查询用户列表
     **/
    @Select({"<script>" +
            "SELECT user_id, login_code, login_pwd, " +
            "user_name, dept_id,  user_phone, user_info, " +
            "gmt_last_login, pwd_last_update, gmt_create, " +
            "gmt_modify,user_deposits, verify_status " +
            "FROM orga_user " +
            "WHERE is_deleted != 1 " +
            "<if test='queryParam.userName != null'> AND user_name LIKE CONCAT('%', #{queryParam.userName}, '%') </if>" +
            "<if test='queryParam.userPhone != null'> AND user_phone LIKE CONCAT('%', #{queryParam.userPhone}, '%') </if>" +
            "<if test='queryParam.deptIds != null and queryParam.deptIds.size() > 0'>" +
            "AND dept_id IN " +
            "<foreach item='deptId' index='index' collection='queryParam.deptIds' open='(' separator=',' close=')'>" +
            "#{deptId}" +
            "</foreach>" +
            "</if> " +
            "ORDER BY dept_id ASC, user_id DESC " +
            "</script>"})
    List<UserEntity> selectPageList(@Param("queryParam") JSONObject queryParam);
    /**
     * @Description: 根据id查询用户
     **/
    @Select({"SELECT user_id, login_code, login_pwd, " +
            "user_name, dept_id,  user_phone, user_info, eth_account,eth_credentials, " +
            "gmt_last_login, pwd_last_update, gmt_create, " +
            "gmt_modify,user_deposits " +
            "FROM orga_user " +
            "WHERE is_deleted != 1 " +
            "AND user_id = #{userId}"})
    UserEntity selectUserById(@Param("userId") Integer userId);
    /**
     * @Description: 根据登录代码查询用户
     **/
    @Select({"SELECT user_id, login_code, login_pwd, " +
            "user_name, dept_id, user_phone, user_info, " +
            "gmt_last_login, pwd_last_update, gmt_create, " +
            "gmt_modify,user_deposits " +
            "FROM orga_user " +
            "WHERE is_deleted != 1 " +
            "AND login_code = #{loginCode}"})
    UserEntity selectUserByLoginCode(@Param("loginCode") String loginCode);
    /**
     * @Description: 根据登录代码查询其他用户（排除自己）
     **/
    @Select({"SELECT user_id, login_code, login_pwd, " +
            "user_name, dept_id, user_phone, user_info, " +
            "gmt_last_login, pwd_last_update, gmt_create, " +
            "gmt_modify,user_deposits " +
            "FROM orga_user " +
            "WHERE is_deleted != 1 " +
            "AND login_code = #{loginCode}" +
            "AND user_id != #{userId}"})
    UserEntity selectOtherUserLoginCode(@Param("loginCode") String loginCode,@Param("userId") Integer userId);

    /**
     * @Description: 查询用户选项列表，只包含 user_id 和 user_name 字段
     **/
    @Select({"SELECT user_id, user_name " +
            "FROM orga_user " +
            "WHERE is_deleted != 1 AND verify_status = 1 "})
    List<UserEntity> selectUserOptions();
    /**
     * @Description: 查询用户列表，根据用户id列表
     **/
    @Select({"<script>SELECT user_id, login_code, login_pwd, " +
            "user_name, dept_id, user_phone, user_info, " +
            "gmt_last_login, pwd_last_update, gmt_create, " +
            "gmt_modify,user_deposits " +
            "FROM orga_user " +
            "WHERE is_deleted != 1 " +
            "AND user_id IN " +
            "<foreach item='userId' index='index' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>"})
    List<UserEntity> selectListByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * @Description: 插入用户信息
     **/
    @Insert({"INSERT INTO orga_user " +
            "(user_name, login_code, login_pwd, " +
            "dept_id,  user_phone, user_info,user_deposits, " +
            "gmt_create, gmt_modify, is_deleted, eth_account, eth_credentials ) " +
            "VALUES (#{userEntity.userName}, #{userEntity.loginCode}, " +
            "#{userEntity.loginPwd}, #{userEntity.deptId}, " +
            "#{userEntity.userPhone}, #{userEntity.userInfo}, 0, " +
            "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, #{userEntity.ethAccount}, #{userEntity.ethCredentials})"})
    @Options(useGeneratedKeys = true, keyProperty = "userEntity.userId", keyColumn = "user_id")
    void insertUser(@Param("userEntity") UserEntity userEntity);
    /**
     * @Description: 更新用户信息
     **/
    @Update({"<script>" +
            "UPDATE orga_user SET " +
            "<if test='userEntity.userName != null'> user_name = #{userEntity.userName}, </if>" +
            "<if test='userEntity.loginCode != null'> login_code = #{userEntity.loginCode}, </if>" +
            "<if test='userEntity.loginPwd != null'> " +
            "login_pwd = #{userEntity.loginPwd}, " +
            "pwd_last_update = CURRENT_TIMESTAMP, " +
            "</if>" +
            "<if test='userEntity.deptId != null'> dept_id = #{userEntity.deptId}, </if>" +
            "<if test='userEntity.userPhone != null'> user_phone = #{userEntity.userPhone}, </if>" +
            "<if test='userEntity.userInfo != null'> user_info = #{userEntity.userInfo}, </if>" +
            "<if test='userEntity.userDeposits != null'> user_deposits = #{userEntity.userDeposits}, </if>" +
            "<if test='userEntity.verifyStatus != null'> verify_status = #{userEntity.verifyStatus}, </if>" +
            "gmt_modify = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userEntity.userId}" +
            "</script>"})
    void updateUser(@Param("userEntity") UserEntity userEntity);
    /**
     * @Description: 批量删除用户（逻辑删除）
     **/
    @Update({"<script>" +
            "UPDATE orga_user SET " +
            "is_deleted = 1 " +
            "WHERE user_id IN " +
            "<foreach item='id' index='index' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>"})
    void deleteUser(@Param("ids") List<Integer> ids);
    /**
     * @Description: 更改用户余额
     **/
    @Update("<script>" +
            "UPDATE orga_user " +
            "SET user_deposits = user_deposits - #{assetPrice} " +
            "WHERE user_id = #{userId} " +
            "</script>")
    int updateprice(@Param("assetPrice") Long assetPrice,@Param("userId") int userId);

    /**
     * @Description: 更改资产数量-1
     * @param edc_asset_id
     * @return
     */
    @Update("UPDATE system_asset SET system_asset_quantity = system_asset_quantity-1 WHERE edc_asset_id = #{edc_asset_id}")
    int updatequantity(@Param("edc_asset_id") String edc_asset_id);

}


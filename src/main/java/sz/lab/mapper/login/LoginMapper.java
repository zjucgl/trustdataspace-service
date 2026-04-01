package sz.lab.mapper.login;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import sz.lab.dto.login.LoginDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 用户登录
 * @Author: 宋光慧
 * @Date: 2023/7/11
 **/
@Mapper
public interface LoginMapper {

    /**
     * @Description: 根据账户登录名称获取账户信息
     * @Author: 宋光慧
     * @Date: 2023/7/11
     **/
//    @Select({"SELECT " +
//            "account_id, account_name, " +
//            "login_code, login_pwd, user_id, " +
//            "is_deleted, gmt_last_login, pwd_last_update " +
//            "FROM system_account " +
//            "WHERE login_code = #{loginCode} LIMIT 1"
//    })
//    LoginDTO getByLoginCode(@Param("loginCode") String loginCode);
    @Select({"SELECT " +
            "user_id, login_code, login_pwd, " +
            "user_name, dept_id, verify_status, " +
            "is_deleted, gmt_last_login,pwd_last_update " +
            "FROM orga_user " +
            "WHERE login_code = #{loginCode} LIMIT 1"
    })
    LoginDTO getByLoginCode(@Param("loginCode") String loginCode);
    /**
     * @Description: 更新用户登录信息
     **/
    @Update({"UPDATE orga_user SET " +
            "gmt_last_login = #{gmtLastLogin} " +
            "WHERE user_id = #{userId}"})
    void updateLastLogin(@Param("userId") Integer userId, @Param("gmtLastLogin") LocalDateTime gmtLastLogin);

}

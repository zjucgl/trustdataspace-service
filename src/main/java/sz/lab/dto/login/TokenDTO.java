package sz.lab.dto.login;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sz.lab.config.constants.Role;
import sz.lab.config.constants.RoleEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Token信息，需要自行添加
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    private Integer userId;
    /**
     * 其实token只写入单个角色
     */
    private String[] roleCodes;

    /**
     * 系统角色与框架角色对应表<br/>
     * 左边是系统角色，右边是框架角色
     */
    private static final Map<String, Role> ROLE_MAP = new HashMap<String, Role>() {
        {
            put(RoleEnum.Admin.getCode(), Role.Admin);
            put(RoleEnum.User.getCode(), Role.User);
        }
    };

    public Role[] getRoles() {
        Role[] ret = new Role[roleCodes.length];
        for (int i = 0; i < roleCodes.length; i++) {
            ret[i] = ROLE_MAP.get(roleCodes[i]);
        }
        return ret;
    }

    @Override
    public String toString() {
        // 检查roleCodes是否为null
        if (roleCodes == null) {
            this.roleCodes = new String[0]; // 或者你可以选择用空数组替代
        }
        return JSON.toJSONString(this);
    }

}

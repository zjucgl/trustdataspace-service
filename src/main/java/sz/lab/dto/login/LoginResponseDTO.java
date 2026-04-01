package sz.lab.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 登录返回
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String username;
    private Boolean pwdExpire;//密码是否需要重置
    private Integer userId;//用来重置密码的userId,登录成功后token记录的是roles中的userId
    private List<LoginRoleDTO> roles;

}

package sz.lab.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 登录请求
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String loginCode;
    private String loginPwd;
    private String imgUuid;
    private String verifyCode;
}

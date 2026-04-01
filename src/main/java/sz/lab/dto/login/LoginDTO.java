package sz.lab.dto.login;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description: 登录账号验证信息
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private Integer userId;
    private String loginCode;
    private String loginPwd;
    private String userName;
    private Integer deptId;
    private Integer isDeleted;
    private Integer pwdErrorNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtLastLogin;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pwdLastUpdate;
    private Integer verifyStatus;


}

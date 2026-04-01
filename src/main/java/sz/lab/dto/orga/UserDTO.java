package sz.lab.dto.orga;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserDTO {
    private Integer userId;
    @NotBlank(message = "登录账号不能为空")
    private String loginCode;
    @NotBlank(message = "登录密码不能为空")
    private String loginPwd;
    @NotBlank(message = "用户名称不能为空")
    private String userName;
    private String userPhone;
    private String userInfo;
    @NotNull(message = "部门id不能为空")
    private Integer deptId;
    private Integer participantKeyId;
    private List<Integer> roleIdList;
    private Integer userDeposits;
    private Integer userDepositsExtra;
    private Integer verifyStatus;
}

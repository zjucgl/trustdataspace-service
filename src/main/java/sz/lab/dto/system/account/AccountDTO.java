package sz.lab.dto.system.account;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AccountDTO {
    @NotNull(message = "账号id不能为空")
    private Integer accountId;
    @NotBlank(message = "登录账号不能为空")
    private String loginCode;
    @NotBlank(message = "登录密码不能为空")
    private String loginPwd;
    @NotBlank(message = "账号名称不能为空")
    private String accountName;
    private String accountInfo;
    private Integer userId;
    private List<Integer> roleIdList;
}

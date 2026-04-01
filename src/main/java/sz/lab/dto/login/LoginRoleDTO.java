package sz.lab.dto.login;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2023/11/28 13:56
 */
@Data
public class LoginRoleDTO {
    private List<String> roles;
    private String roleName;
    private Integer deptId;
    private String deptName;
    private Integer userId;
    private String username;
    private String nickname;
    private String avatar;
    private String participantId;

    private String accessToken;
    private String refreshToken;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expires;
}

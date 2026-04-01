package sz.lab.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 验证码
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KaptchaDTO {
    private String imgCode;
    private String imgUuid;
}

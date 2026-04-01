package sz.lab.dto.login;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description: Token刷新
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {
    private String accessToken;
    private String refreshToken;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expires;
}

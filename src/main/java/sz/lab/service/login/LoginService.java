package sz.lab.service.login;

import sz.lab.dto.login.LoginRequestDTO;
import sz.lab.dto.system.OperateResultDTO;

/**
 * @Description:
 * @Author: 宋光慧
 * @Date: 2023/07/16
 **/
public interface LoginService {

    /**
     * @Description: 获取验证码信息
     * @Author: 宋光慧
     * @Date: 2023/7/16
     */
    OperateResultDTO getKaptcha() throws Exception;

    /**
     * @Description: 账号登录验证
     * @Author: 宋光慧
     * @Date: 2023/7/16
     **/
    OperateResultDTO check(LoginRequestDTO loginRequestDTO) ;

    /**
     * @Description: 刷新验证码
     * @Author: 宋光慧
     * @Date: 2023/7/16
     **/
    OperateResultDTO refreshToken(String refreshToken) ;
    /**
     * @Description: 获取异步路由信息
     **/
    OperateResultDTO getAsyncRoutes(Integer userId);


}

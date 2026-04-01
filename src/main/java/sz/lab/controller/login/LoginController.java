package sz.lab.controller.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import sz.lab.config.annotation.SystemControllerLog;
import sz.lab.controller.BaseController;
import sz.lab.dto.login.LoginRequestDTO;
import sz.lab.dto.login.RefreshTokenDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.login.LoginService;

import javax.annotation.Resource;

/**
 * @Description: 登录模块处理
 * @Author: 宋光慧
 * @Date: 2023/07/16
 **/
@RestController
@RequestMapping(value = "/login")
public class LoginController extends BaseController {

    @Resource
    private LoginService loginService;
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * @Description: 获取验证码
     * @Author: 宋光慧
     * @Date: 2023/07/16
     */
    @RequestMapping(path = "/loginKaptcha", method = RequestMethod.GET)
    public OperateResultDTO getKaptcha() {
        OperateResultDTO opResultDTO = new OperateResultDTO();
        try {
            opResultDTO = loginService.getKaptcha();
        } catch (Exception e) {
            opResultDTO.setSuccess(false);
            opResultDTO.setMessage(e.getMessage());
        }
        return opResultDTO;
    }

    /**
     * @Description: 账号登录
     * @Author: 宋光慧
     * @Date: 2023/07/16
     **/
    @SystemControllerLog(type = "登录模块",content = "登录验证",login = "true")
    @RequestMapping(value = "/loginCheck", method = RequestMethod.POST)
    public OperateResultDTO check(@RequestBody LoginRequestDTO loginRequestDTO) {
        OperateResultDTO opResultDTO = new OperateResultDTO();
        try {
            opResultDTO = loginService.check(loginRequestDTO);
        } catch (Exception e) {
            opResultDTO.setSuccess(false);
            opResultDTO.setMessage(e.getMessage());
        }
        return opResultDTO;
    }

    /**
     * @Description: 刷新Token
     * @Author: 宋光慧
     * @Date: 2023/07/16
     **/
    @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
    public OperateResultDTO refreshToken(@RequestBody RefreshTokenDTO refreshToken) {
        OperateResultDTO opResultDTO = new OperateResultDTO();
        try {
            opResultDTO = loginService.refreshToken(refreshToken.getRefreshToken());
        } catch (Exception e) {
            opResultDTO.setSuccess(false);
            opResultDTO.setMessage(e.getMessage());
        }
        return opResultDTO;
    }

    /**
     * @Description: 后端动态生成路由
     **/
    @RequestMapping(value = "/getAsyncRoutes", method = RequestMethod.GET)
    public OperateResultDTO getAsyncRoutes(@RequestHeader(name = "Authorization") String authorization) {
        OperateResultDTO opResultDTO = new OperateResultDTO();
//        TokenDTO tokenDTO = JWTUtil.verifyToken(authorization);
//        Integer userId = tokenDTO.getUserId();
        try {
            opResultDTO = loginService.getAsyncRoutes(userId.get());
        } catch (Exception e) {
            opResultDTO.setSuccess(false);
            opResultDTO.setMessage(e.getMessage());
        }
        return opResultDTO;
//        return new OperateResultDTO(true,"成功",new String[0]);
    }
}

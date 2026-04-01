package sz.lab.config.cfg;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sz.lab.dto.login.TokenDTO;
import sz.lab.utils.JWTUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 适配前端代码
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2023/11/24 9:37
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        if (request.getRequestURL().indexOf("/login/loginKaptcha") != -1
                || request.getRequestURL().indexOf("/login/loginCheck") != -1
                || request.getRequestURL().indexOf("/login/refreshToken") != -1
                || request.getRequestURL().indexOf("/login/getAsyncRoutes") != -1) {
            response.addHeader("token_status", "ok");
            response.setHeader("Access-Control-Expose-Headers", "token_status");
            return true;
        } else {
            TokenDTO check = JWTUtil.verifyToken(request.getHeader(SystemConfig.TOKEN_KEY));
            if (null != check) {
                response.addHeader("token_status", "ok");
                response.setHeader("Access-Control-Expose-Headers", "token_status");
            } else {
                response.addHeader("token_status", "no");
            }
            return true;
        }
    }
}

package sz.lab.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.bind.annotation.ModelAttribute;
import sz.lab.config.cfg.SystemConfig;
import sz.lab.config.constants.RoleEnum;
import sz.lab.dto.login.TokenDTO;
import sz.lab.utils.JWTUtil;

import javax.servlet.http.HttpServletRequest;

public class BaseController {
//   初始化 userId 和 role 两个 ThreadLocal 变量。ThreadLocal 用于在多线程环境中存储线程局部变量。
//   ThreadUtil.createThreadLocal(false) 创建了一个 ThreadLocal 实例，参数 false 表示不使用弱引用。
    public BaseController() {
        userId = ThreadUtil.createThreadLocal(false);
        role = ThreadUtil.createThreadLocal(false);
    }

//    userId 是一个 ThreadLocal 变量，用于存储当前线程的用户ID。
//    role 是一个 ThreadLocal 变量，用于存储当前线程的用户角色。
    protected ThreadLocal<Integer> userId;
    protected ThreadLocal<RoleEnum> role;

//    @ModelAttribute 注解表示该方法在每个请求处理方法之前执行
//    init 方法接收一个 HttpServletRequest 对象，用于获取请求头中的 token
//    从请求头中获取 token，并去除前缀 Bearer
//    使用 JWTUtil.verifyToken 方法验证 token，返回一个 TokenDTO 对象
//    如果 TokenDTO 为空，将 userId 设置为 0
//    否则，从 TokenDTO 中获取用户ID和角色代码，并设置到 userId 和 role 中
    @ModelAttribute
    public void init(HttpServletRequest request) {
        String token = request.getHeader(SystemConfig.TOKEN_KEY);
        if (StrUtil.isNotBlank(token)) {
            token = token.replace("Bearer ", "");
        }
        TokenDTO tokenDTO = JWTUtil.verifyToken(token);
        if (null == tokenDTO) {
            userId.set(0);
        } else {
            userId.set(tokenDTO.getUserId());
            role.set(RoleEnum.getRoleByCode(tokenDTO.getRoleCodes()[0]));
        }
    }
}

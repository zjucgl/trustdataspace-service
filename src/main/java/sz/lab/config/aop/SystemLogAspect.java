package sz.lab.config.aop;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import sz.lab.config.annotation.SystemControllerLog;
import sz.lab.config.cfg.SystemConfig;
import sz.lab.dto.login.TokenDTO;
import sz.lab.entity.system.SystemLogEntity;
import sz.lab.service.system.log.SystemLogService;
import sz.lab.utils.JWTUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 切面处理类，记录操作日志到数据库
 */
@Aspect
@Component
public class SystemLogAspect {
    @Autowired
    private SystemLogService systemLogService;
    //记录方法的执行时间
    ThreadLocal<Long> startTime = new ThreadLocal<>();
    @Pointcut("@annotation(sz.lab.config.annotation.SystemControllerLog)")//在注解的位置切入代码
    //@Pointcut("execution(public * sz.lab.controller..*.*(..))")//从controller切入
    public void systemLogPointCut() {
    }
    @Before("systemLogPointCut()")
    public void beforeMethod(JoinPoint point){
        startTime.set(System.currentTimeMillis());
    }
    /**
     * 设置操作异常切入点记录异常日志 扫描所有controller包下操作
     */
    @Pointcut("execution(* sz.lab.controller..*.*(..))")
    public void systemExceptionLogPointCut() {
    }
    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     * @param result      返回结果
     */
    @AfterReturning(value = "systemLogPointCut()", returning = "result")
    public void saveSystemLog(JoinPoint joinPoint, Object result) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取操作
            SystemControllerLog logRecord = method.getAnnotation(SystemControllerLog.class);
            SystemLogEntity logEntity = new SystemLogEntity();
            String isLogin = "false";
            if (logRecord != null) {
                logEntity.setLogType(logRecord.type());//设置模块名称
                logEntity.setLogContent(logRecord.content());//设置日志内容
                isLogin = logRecord.login();
            }
            //参数
            Object[] args = joinPoint.getArgs();

            // 将入参转换成json
            String params = argsArrayToString(args);
            JSONObject paramsJson = JSONObject.parseObject(params);
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName + "()";
            //返回值
            Object resultObj = JSON.toJSON(result);
            JSONObject resultJson = JSONObject.from(resultObj);
            if(isLogin.equals("true")){
                if(paramsJson.containsKey("loginCode")){
                    String content = "账号"+paramsJson.get("loginCode")
                            +"进行"+ logEntity.getLogContent() + "。返回值:"+resultJson.get("message");
                    logEntity.setLogContent(content);
                }
            }else{
                logEntity.setUserId(getUserId(request));
            }
            logEntity.setLogMethod(methodName);//设置方法名称
            logEntity.setLogIp(getIp(request));
            if(resultJson.containsKey("success")){
                if(resultJson.get("success").equals(true)){
                    logEntity.setLogStatus("成功");
                }else{
                    logEntity.setLogStatus("失败");
                }
            }
            logEntity.setLogSource("web");
//            Long takeTime = System.currentTimeMillis() - startTime.get();//记录方法执行耗时时间（单位：毫秒）
            //插入数据库
            systemLogService.save(logEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 异常返回通知，用于拦截异常日志信息 连接点抛出异常后执行
     */
    //@AfterThrowing(pointcut = "systemExceptionLogPointCut()", throwing = "e")
    @AfterThrowing(pointcut = "systemLogPointCut()", throwing = "e")
    public void saveExceptionLog(JoinPoint joinPoint, Throwable e) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        SystemLogEntity logEntity = new SystemLogEntity();
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName + "()";
            // 获取操作
            SystemControllerLog logRecord = method.getAnnotation(SystemControllerLog.class);
            String isLogin = "false";
            if (logRecord != null) {
                logEntity.setLogType(logRecord.type());//设置模块名称
                logEntity.setLogContent(logRecord.content());//设置日志内容
                isLogin = logRecord.login();
            }
            // 将入参转换成json
            String params = argsArrayToString(joinPoint.getArgs());
            JSONObject json = JSONObject.parseObject(params);
            //返回值
            if(isLogin.equals("true")){
                if(json.containsKey("loginCode")){
                    String content = logEntity.getLogContent()+",账号:"+json.get("loginCode");
                    logEntity.setLogContent(content);
                }
            }else{
                logEntity.setUserId(getUserId(request));
            }
            logEntity.setUserId(getUserId(request));
            logEntity.setLogMethod(methodName);//设置方法名称
            logEntity.setLogIp(getIp(request));
            logEntity.setLogStatus("失败");
            logEntity.setLogSource("web");
            logEntity.setLogError(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace()));//记录异常信息
            //插入数据库
            systemLogService.save(logEntity);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
    /**
     * 转换异常信息为字符串
     */
    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuffer strbuff = new StringBuffer();
        for (StackTraceElement stet : elements) {
            strbuff.append(stet + "\n");
        }
        String message = exceptionName + ":" + exceptionMessage + "\n\t" + strbuff.toString();
        message = substring(message,0 ,2000);
        return message;
    }
    //获取userId
    private Integer getUserId(HttpServletRequest request){
        String token = request.getHeader(SystemConfig.TOKEN_KEY);
        if (StrUtil.isNotBlank(token)) {
            token = token.replace("Bearer ", "");
        }
        TokenDTO tokenDTO = JWTUtil.verifyToken(token);
        if (null == tokenDTO) {
            return null;
        } else {
            return tokenDTO.getUserId();
        }
    }
    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (o != null) {
                    try {
                        Object jsonObj = JSON.toJSON(o);
                        params += jsonObj.toString() + " ";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params.trim();
    }
    //字符串截取
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        } else {
            if (end < 0) {
                end += str.length();
            }
            if (start < 0) {
                start += str.length();
            }
            if (end > str.length()) {
                end = str.length();
            }
            if (start > end) {
                return "";
            } else {
                if (start < 0) {
                    start = 0;
                }
                if (end < 0) {
                    end = 0;
                }
                return str.substring(start, end);
            }
        }
    }
    //根据HttpServletRequest获取访问者的IP地址
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

package sz.lab.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:  自定义注解，拦截controller
 */

@Target({ElementType.PARAMETER, ElementType.METHOD})//作用在参数和方法上
@Retention(RetentionPolicy.RUNTIME)//运行时注解
//@Documented//表明这个注解应该被 javadoc工具记录
public @interface SystemControllerLog {
    /**
     * 模块类型
     */
    String type() default "未定义模块";
    /**
     * 模块内容
     */
    String content() default "未定义内容";
    /**
     * 是否为登录方法
     */
    String login() default "false";
}

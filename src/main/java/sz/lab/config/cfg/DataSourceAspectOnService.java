package sz.lab.config.cfg;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 0)
@Slf4j
@Aspect
public class DataSourceAspectOnService {
//    /**
//     * 匹配service所在的package所有类（及子类）中所有方法（所有返回类型，所有类型参数）的执行
//     */
//    @Pointcut("execution(* sz.lab.service.mvd..*.*(..))")
//    private void masterAspect() {
//    }
//    /**
//     * 执行Service方法前设置数据源
//     */
//    @Before("masterAspect()")
//    public void beforeMasterDb() throws Exception{
//        DynamicDataSourceContextHolder.push("slave");
//    }
//
//    @After("masterAspect()")
//    public void afterMasterDb() {
//        DynamicDataSourceContextHolder.poll();
//    }
}

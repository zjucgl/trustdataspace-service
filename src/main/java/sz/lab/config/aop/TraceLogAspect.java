package sz.lab.config.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import sz.lab.config.annotation.TraceLog;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.log.TraceLogDTO;
import sz.lab.service.system.log.ISysyemTraceLogService;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class TraceLogAspect {

    @Resource
    private ISysyemTraceLogService traceLogService;
    @Pointcut("@annotation(sz.lab.config.annotation.TraceLog)")
    public void traceLogPointCut() {
    }

    @Around("traceLogPointCut()")
    public Object traceLog(ProceedingJoinPoint joinPoint) throws Throwable {
        //反射机制获取方法的注解
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        TraceLog annotation = method.getAnnotation(TraceLog.class);

        if(annotation != null) {
            String value = annotation.value();
            //仅当方法上存在LogAnnotation注解时，记录日志
            System.out.println("方法描述:"+ value);
        }
        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        System.out.println("参数列表：" + Arrays.toString(args));
        String assetId = (String) args[0]; // 获取 assetId 参数
        Integer userId = (Integer) args[1]; // 获取 userId 参数
        Object result = new Object();
        // 获取方法返回值
        try {
            result = joinPoint.proceed();
            if (result instanceof OperateResultDTO) {
                OperateResultDTO operateResult = (OperateResultDTO) result;
                if (operateResult.isSuccess()) {
                    Object[] resultData = (Object[]) operateResult.getResult();
                    // 获取数组中的元素
                    String assetId_time = (String) resultData[0];
                    String transactionHash = (String) resultData[2];
                    BigInteger totalCostWei = (BigInteger) resultData[3];
                    Long asset_price = (Long) resultData[4];
                    String user_name = (String) resultData[5];
                    // 打印或者做其他操作
                    System.out.println("资产ID: " + assetId_time);
                    System.out.println("购买人ID: " + userId);
                    System.out.println("交易hash码: " + transactionHash);
                    System.out.println("总共花费(Wei): " + totalCostWei);
                    //存入数据库
                    TraceLogDTO traceLogDTO = new TraceLogDTO();
                    traceLogDTO.setUserId(Long.valueOf(userId));
                    traceLogDTO.setLogType("购买资产");
                    traceLogDTO.setLogStatus("交易完成");
                    traceLogDTO.setLogContent("资产ID: " + assetId_time + " 购买人ID: " + userId + " 交易hash码: " + transactionHash + " 总共花费(Wei): " + totalCostWei+" 合计手续花费: " + totalCostWei.divide(BigInteger.valueOf(1000000000L)));
                    Long totalCost = totalCostWei.longValueExact();
                    traceLogDTO.setLogError("");
                    traceLogDTO.setTotalAmount(asset_price+totalCost/1000000000L);
                    traceLogDTO.setUserName(user_name);
                    //记录时间
                    traceLogDTO.setGmtCreate(LocalDateTime.now());
                    traceLogService.saveTraceLog(traceLogDTO);
                } else {
                    TraceLogDTO traceLogDTO = new TraceLogDTO();
                    traceLogDTO.setUserId(Long.valueOf(userId));
                    traceLogDTO.setLogType("购买资产");
                    traceLogDTO.setLogStatus("交易失败");
                    traceLogDTO.setLogContent("");
                    traceLogDTO.setLogError("交易失败");
                    traceLogDTO.setTotalAmount(0L);
                    traceLogService.saveTraceLog(traceLogDTO);
                }
            }
        }  catch (Exception e) {
            // 处理异常并记录日志
            System.err.println("方法执行过程中出现异常: " + e.getMessage());

            TraceLogDTO traceLogDTO = new TraceLogDTO();
            traceLogDTO.setUserId(Long.valueOf(userId));
            traceLogDTO.setUserName("");
            traceLogDTO.setLogType("购买资产");
            traceLogDTO.setLogStatus("执行异常");
            traceLogDTO.setLogContent("userId: "+userId+" "+e.getMessage());
            traceLogDTO.setLogError(e.getMessage());
            traceLogDTO.setTotalAmount(0L);
            traceLogDTO.setGmtCreate(LocalDateTime.now());
            traceLogService.saveTraceLog(traceLogDTO);
            // 继续抛出异常（如果你不想吞掉）
            throw e;
        }

        return result;
    }
}

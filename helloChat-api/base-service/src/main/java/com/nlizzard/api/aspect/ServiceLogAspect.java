package com.nlizzard.api.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Aspect
@Slf4j
public class ServiceLogAspect {

    // 镇压idea报告service找不到的警告
    @SuppressWarnings("all")
    @Around("execution(* com.nlizzard.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        // 记录方法开始时间
        String pointName = joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() ;
        stopWatch.start(pointName);

        Object proceed = joinPoint.proceed();

        // 记录方法结束时间
        stopWatch.stop();

        // 拿到总耗时（毫秒）
        long takeTimes = stopWatch.getTotalTimeMillis();
        if (takeTimes > 3000) {
            log.error("执行位置{}，执行时间太长了，耗费了{}毫秒", pointName, takeTimes);
        } else if (takeTimes > 2000) {
            log.warn("执行位置{}，执行时间稍微有点长，耗费了{}毫秒", pointName, takeTimes);
        } else {
            log.info("执行位置{}，执行时间正常，耗费了{}毫秒", pointName, takeTimes);
        }
        return proceed;
    }
}

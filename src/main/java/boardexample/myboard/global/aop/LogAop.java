package boardexample.myboard.global.aop;

import boardexample.myboard.global.log.LogTrace;
import boardexample.myboard.global.log.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LogAop {

    private final LogTrace logTrace;


    @Pointcut("execution(* boardexample.myboard.domain..*Service*.*(..))")
    public void allService(){};

    @Pointcut("execution(* boardexample.myboard.domain..*Repository*.*(..))")
    public void allRepository(){};

    @Pointcut("execution(* boardexample.myboard.domain..*Controller*.*(..))")
    public void allController(){};


    @Around("allService() || allController() || allRepository()")
    public Object logTrace(ProceedingJoinPoint joinPoint) throws Throwable {

        TraceStatus status = null;

        try{
            status = logTrace.begin(joinPoint.getSignature().toShortString());
            Object result = joinPoint.proceed();

            logTrace.end(status);

            return result;
        }catch (Throwable e){
            e.printStackTrace();
            logTrace.exception(status, e);
            throw e;
        }
    }




}

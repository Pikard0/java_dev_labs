package com.java.carparts.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("@annotation(com.java.carparts.aop.TrackExecution)")
    public Object logAnnotatedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Calling annotated method {}", joinPoint.getSignature().toShortString());
        Object result = joinPoint.proceed();
        log.info("Annotated method {} completed", joinPoint.getSignature().toShortString());
        return result;
    }

    @Before("execution(* com.java.carparts.service..*(..))")
    public void logServiceCall(org.aspectj.lang.JoinPoint joinPoint) {
        log.debug("Service call: {}", joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(
            pointcut = "execution(* com.java.carparts.service..*(..))",
            throwing = "exception")
    public void logServiceFailure(org.aspectj.lang.JoinPoint joinPoint, Throwable exception) {
        log.warn("Service call {} failed: {}", joinPoint.getSignature().toShortString(), exception.getMessage());
    }
}

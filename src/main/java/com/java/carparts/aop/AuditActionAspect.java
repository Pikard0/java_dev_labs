package com.java.carparts.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditActionAspect {

    @AfterReturning("@annotation(auditAction)")
    public void auditSuccessfulAction(JoinPoint joinPoint, AuditAction auditAction) {
        log.info("Audit: {} ({})", auditAction.value(), joinPoint.getSignature().toShortString());
    }
}

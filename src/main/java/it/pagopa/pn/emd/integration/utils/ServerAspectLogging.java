package it.pagopa.pn.emd.integration.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public abstract class ServerAspectLogging {

    @Pointcut("execution(* it.pagopa.pn..generated.openapi.server..*.*(..))")
    public void serverApiPointcut() {}

    @Around("serverApiPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.debug("Handling server request: {}", method);
        try {
            Object result = joinPoint.proceed();
            log.debug("Server request completed: {}", method);
            return result;
        } catch (Throwable ex) {
            log.warn("Server request failed: {} - {}", method, ex.getMessage());
            throw ex;
        }
    }
}

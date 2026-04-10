package it.pagopa.pn.emd.integration.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public abstract class ClientAspectLogging {

    @Pointcut("execution(* it.pagopa.pn..generated.openapi.msclient..*.*(..))")
    public void msClientPointcut() {}

    @Around("msClientPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.debug("Invoking client method: {}", method);
        try {
            Object result = joinPoint.proceed();
            log.debug("Client method completed: {}", method);
            return result;
        } catch (Throwable ex) {
            log.warn("Client method failed: {} - {}", method, ex.getMessage());
            throw ex;
        }
    }
}

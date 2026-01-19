package com.training.coach.shared.logging;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect {

    private static final long WARN_THRESHOLD_MS = 1_000L;

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logServiceInvocation(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String method = joinPoint.getSignature().toShortString();
        long start = System.nanoTime();

        if (logger.isTraceEnabled()) {
            logger.trace("Enter {}", method);
        }
        if (logger.isDebugEnabled()) {
            Object[] args = joinPoint.getArgs();
            String argTypes = Arrays.stream(args)
                    .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                    .collect(Collectors.joining(", "));
            logger.debug("Arg types for {}: [{}]", method, argTypes);
        }

        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            if (logger.isInfoEnabled()) {
                logger.info("Exit {} ({} ms)", method, durationMs);
            }
            if (durationMs >= WARN_THRESHOLD_MS && logger.isWarnEnabled()) {
                logger.warn("Slow service call {} took {} ms", method, durationMs);
            }
            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            logger.error("Error in {} after {} ms", method, durationMs, ex);
            throw ex;
        }
    }
}

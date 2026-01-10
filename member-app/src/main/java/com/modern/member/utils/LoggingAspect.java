package com.modern.member.utils;

import com.modern.member.dto.LoginRequest;
import com.modern.member.dto.SignupRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.modern.member.controller..*(..)) || execution(* com.modern.member.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        if (log.isInfoEnabled()) {
            String sanitizedArgs = sanitizeArguments(args);
            log.info("ENTER: {}.{}() with args = {}", className, methodName, sanitizedArgs);
        }

        try {
            Object result = joinPoint.proceed();

            long elapsedTime = System.currentTimeMillis() - start;
            log.info("EXIT: {}.{}() executed in {} ms", className, methodName, elapsedTime);

            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(args), className, methodName);
            throw e;
        }
    }

    private String sanitizeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof LoginRequest) return "[PROTECTED: LoginRequest]";
                    if (arg instanceof SignupRequest) return "[PROTECTED: SignupRequest]";
                    return arg.toString();
                })
                .toList()
                .toString();
    }
}
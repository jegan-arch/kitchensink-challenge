package com.modern.member.utils;

import com.modern.member.dto.LoginRequest;
import com.modern.member.dto.SignupRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.getDeclaringTypeName()).thenReturn("com.modern.member.service.TestService");
        lenient().when(signature.getName()).thenReturn("testMethod");
    }

    @Test
    void logAround_Success_WithNormalArguments() throws Throwable {
        Object[] args = new Object[]{"someArg", 123};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("SuccessResult");

        Object result = loggingAspect.logAround(joinPoint);

        assertEquals("SuccessResult", result);
        verify(joinPoint).proceed();
    }

    @Test
    void logAround_Success_WithSensitiveArguments() throws Throwable {
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        SignupRequest signupRequest = new SignupRequest("user", "ROLE", "name", "email", "phone");
        Object[] args = new Object[]{loginRequest, signupRequest};

        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("SuccessResult");

        Object result = loggingAspect.logAround(joinPoint);

        assertEquals("SuccessResult", result);
        verify(joinPoint).proceed();
    }

    @Test
    void logAround_Success_WithNullArguments() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("SuccessResult");

        Object result = loggingAspect.logAround(joinPoint);

        assertEquals("SuccessResult", result);
        verify(joinPoint).proceed();
    }

    @Test
    void logAround_Exception_RethrowsException() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        IllegalArgumentException exception = new IllegalArgumentException("Bad Argument");
        when(joinPoint.proceed()).thenThrow(exception);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                loggingAspect.logAround(joinPoint)
        );

        assertEquals("Bad Argument", thrown.getMessage());
        verify(joinPoint).proceed();
    }
}
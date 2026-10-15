package com.java.carparts.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceLoggingAspectTest {

    private final ServiceLoggingAspect aspect = new ServiceLoggingAspect();

    @Test
    void annotatedAdviceProceedsAndReturnsResult() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("SampleService.run()");
        when(joinPoint.proceed()).thenReturn("done");

        Object result = aspect.logAnnotatedMethod(joinPoint);

        assertEquals("done", result);
        verify(joinPoint).proceed();
    }

    @Test
    void annotatedAdvicePropagatesExceptionWithoutSwallowingIt() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        RuntimeException expected = new RuntimeException("failure");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("SampleService.run()");
        when(joinPoint.proceed()).thenThrow(expected);

        RuntimeException actual = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, () -> aspect.logAnnotatedMethod(joinPoint));

        assertEquals(expected, actual);
        verify(joinPoint).proceed();
    }
}

package com.java.carparts.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void selfInvocationDoesNotPassThroughTheProxy() {
        SelfInvokingService target = new SelfInvokingService();
        TrackExecutionCounter counter = new TrackExecutionCounter();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(counter);
        SelfInvokingService proxy = proxyFactory.getProxy();

        assertEquals("tracked", proxy.callTrackedInternally());
        assertEquals(0, counter.invocations());

        assertEquals("tracked", proxy.tracked());
        assertEquals(1, counter.invocations());
    }

    public static class SelfInvokingService {

        public String callTrackedInternally() {
            return this.tracked();
        }

        @TrackExecution
        public String tracked() {
            return "tracked";
        }
    }

    @Aspect
    public static class TrackExecutionCounter {

        private final AtomicInteger invocations = new AtomicInteger();

        @Around("@annotation(com.java.carparts.aop.TrackExecution)")
        public Object count(ProceedingJoinPoint joinPoint) throws Throwable {
            invocations.incrementAndGet();
            return joinPoint.proceed();
        }

        int invocations() {
            return invocations.get();
        }
    }
}

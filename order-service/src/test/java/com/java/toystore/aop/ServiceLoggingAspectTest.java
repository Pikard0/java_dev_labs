package com.java.toystore.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
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
    @ExtendWith(OutputCaptureExtension.class)
    void proxyCallLogsAdviceButSelfInvocationDoesNot(CapturedOutput output) {
        SelfInvokingService target = new SelfInvokingService();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(new ServiceLoggingAspect());
        SelfInvokingService proxy = proxyFactory.getProxy();

        assertEquals("tracked", proxy.callTrackedInternally());
        assertThat(output.getAll()).doesNotContain("Calling annotated method");

        assertEquals("tracked", proxy.tracked());
        assertThat(output.getAll())
                .contains("Calling annotated method SelfInvokingService.tracked()")
                .contains("Annotated method SelfInvokingService.tracked() completed");
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

}

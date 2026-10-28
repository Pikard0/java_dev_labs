package com.java.toystore.quartz;

import com.java.toystore.configuration.QuartzConfiguration;
import com.java.toystore.service.InventoryReportService;
import com.java.toystore.service.OrderMaintenanceService;
import org.junit.jupiter.api.Test;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuartzJobsTest {

    @Test
    void staleOrderJob_readsMergedParametersAndCallsOneService() throws Exception {
        OrderMaintenanceService service = mock(OrderMaintenanceService.class);
        StaleOrderCleanupJob job = new StaleOrderCleanupJob();
        job.setOrderMaintenanceService(service);
        JobExecutionContext context = contextWith(QuartzConfiguration.STALE_HOURS, 48,
                QuartzConfiguration.BATCH_SIZE, 20);

        job.execute(context);

        verify(service).declineStalePlacedOrders(48, 20);
    }

    @Test
    void reportJob_readsMergedParametersAndCallsOneService() throws Exception {
        InventoryReportService service = mock(InventoryReportService.class);
        LowStockReportJob job = new LowStockReportJob();
        job.setInventoryReportService(service);
        JobExecutionContext context = contextWith(QuartzConfiguration.MINIMUM_STOCK, 3,
                QuartzConfiguration.REPORT_NAME, "daily-low-stock");

        job.execute(context);

        verify(service).publishLowStockReport(3, "daily-low-stock");
    }

    @Test
    void missingJobParameter_producesAnExplicitError() {
        StaleOrderCleanupJob job = new StaleOrderCleanupJob();
        job.setOrderMaintenanceService(mock(OrderMaintenanceService.class));
        JobExecutionContext context = contextWith(QuartzConfiguration.BATCH_SIZE, 20);

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining(QuartzConfiguration.STALE_HOURS);
    }

    @Test
    void failedJob_doesNotAskQuartzForAnInfiniteImmediateRetry() {
        OrderMaintenanceService service = mock(OrderMaintenanceService.class);
        doThrow(new IllegalStateException("database unavailable"))
                .when(service).declineStalePlacedOrders(24, 10);
        StaleOrderCleanupJob job = new StaleOrderCleanupJob();
        job.setOrderMaintenanceService(service);
        JobExecutionContext context = contextWith(QuartzConfiguration.STALE_HOURS, 24,
                QuartzConfiguration.BATCH_SIZE, 10);

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOfSatisfying(JobExecutionException.class,
                        exception -> assertThat(exception.refireImmediately()).isFalse());
    }

    @Test
    void jobs_disallowConcurrentExecution() {
        assertThat(StaleOrderCleanupJob.class).hasAnnotation(DisallowConcurrentExecution.class);
        assertThat(LowStockReportJob.class).hasAnnotation(DisallowConcurrentExecution.class);
    }

    private JobExecutionContext contextWith(Object... keyValues) {
        JobDataMap data = new JobDataMap();
        for (int index = 0; index < keyValues.length; index += 2) {
            data.put((String) keyValues[index], keyValues[index + 1]);
        }
        JobExecutionContext context = mock(JobExecutionContext.class);
        when(context.getMergedJobDataMap()).thenReturn(data);
        return context;
    }
}

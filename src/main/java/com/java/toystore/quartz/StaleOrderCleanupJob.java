package com.java.toystore.quartz;

import com.java.toystore.configuration.QuartzConfiguration;
import com.java.toystore.service.OrderMaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisallowConcurrentExecution
public class StaleOrderCleanupJob implements Job {

    private OrderMaintenanceService orderMaintenanceService;

    @Autowired
    public void setOrderMaintenanceService(OrderMaintenanceService orderMaintenanceService) {
        this.orderMaintenanceService = orderMaintenanceService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int staleHours = QuartzJobData.requiredPositiveInt(
                context.getMergedJobDataMap(), QuartzConfiguration.STALE_HOURS);
        int batchSize = QuartzJobData.requiredPositiveInt(
                context.getMergedJobDataMap(), QuartzConfiguration.BATCH_SIZE);

        try {
            orderMaintenanceService.declineStalePlacedOrders(staleHours, batchSize);
        } catch (RuntimeException exception) {
            log.error("Stale order cleanup failed; Quartz will wait for the next scheduled trigger", exception);
            throw new JobExecutionException(exception, false);
        }
    }
}

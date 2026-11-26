package com.java.toystore.quartz;

import com.java.toystore.configuration.QuartzConfiguration;
import com.java.toystore.service.InventoryReportService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisallowConcurrentExecution
public class LowStockReportJob implements Job {

    private InventoryReportService inventoryReportService;

    @Autowired
    public void setInventoryReportService(InventoryReportService inventoryReportService) {
        this.inventoryReportService = inventoryReportService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int minimumStock = QuartzJobData.requiredPositiveInt(
                context.getMergedJobDataMap(), QuartzConfiguration.MINIMUM_STOCK);
        String reportName = QuartzJobData.requiredText(
                context.getMergedJobDataMap(), QuartzConfiguration.REPORT_NAME);

        try {
            inventoryReportService.publishLowStockReport(minimumStock, reportName);
        } catch (RuntimeException exception) {
            log.error("Low stock report job failed; Quartz will wait for the next scheduled trigger", exception);
            throw new JobExecutionException(exception, false);
        }
    }
}

package com.java.toystore.configuration;

import com.java.toystore.quartz.LowStockReportJob;
import com.java.toystore.quartz.StaleOrderCleanupJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfiguration {

    public static final String JOB_GROUP = "toy-store-maintenance";
    public static final String STALE_ORDER_CLEANUP_JOB = "stale-order-cleanup";
    public static final String LOW_STOCK_REPORT_JOB = "low-stock-report";
    public static final String STALE_HOURS = "staleHours";
    public static final String BATCH_SIZE = "batchSize";
    public static final String MINIMUM_STOCK = "minimumStock";
    public static final String REPORT_NAME = "reportName";
    public static final TimeZone SCHEDULE_TIME_ZONE = TimeZone.getTimeZone("Europe/Kyiv");

    @Bean
    public AutowiringSpringBeanJobFactory quartzJobFactory() {
        return new AutowiringSpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBeanCustomizer quartzJobFactoryCustomizer(AutowiringSpringBeanJobFactory quartzJobFactory) {
        return schedulerFactoryBean -> schedulerFactoryBean.setJobFactory(quartzJobFactory);
    }

    @Bean
    public JobDetail staleOrderCleanupJobDetail() {
        return JobBuilder.newJob(StaleOrderCleanupJob.class)
                .withIdentity(STALE_ORDER_CLEANUP_JOB, JOB_GROUP)
                .usingJobData(STALE_HOURS, 24)
                .usingJobData(BATCH_SIZE, 100)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger staleOrderCleanupHourlyTrigger(JobDetail staleOrderCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity("stale-order-cleanup-hourly", JOB_GROUP)
                .forJob(staleOrderCleanupJobDetail.getKey())
                .usingJobData(new JobDataMap(java.util.Map.of(BATCH_SIZE, 25)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(1)
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithExistingCount())
                .build();
    }

    @Bean
    public Trigger staleOrderCleanupNightlyTrigger(JobDetail staleOrderCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity("stale-order-cleanup-nightly", JOB_GROUP)
                .forJob(staleOrderCleanupJobDetail.getKey())
                .usingJobData(new JobDataMap(java.util.Map.of(STALE_HOURS, 48)))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?")
                        .inTimeZone(SCHEDULE_TIME_ZONE)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    @Bean
    public JobDetail lowStockReportJobDetail() {
        return JobBuilder.newJob(LowStockReportJob.class)
                .withIdentity(LOW_STOCK_REPORT_JOB, JOB_GROUP)
                .usingJobData(MINIMUM_STOCK, 5)
                .usingJobData(REPORT_NAME, "low-stock-toys")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger lowStockReportDailyTrigger(JobDetail lowStockReportJobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity("low-stock-report-daily", JOB_GROUP)
                .forJob(lowStockReportJobDetail.getKey())
                .usingJobData(new JobDataMap(java.util.Map.of(MINIMUM_STOCK, 3)))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 18 * * ?")
                        .inTimeZone(SCHEDULE_TIME_ZONE)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}

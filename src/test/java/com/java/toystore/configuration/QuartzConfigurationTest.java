package com.java.toystore.configuration;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzConfigurationTest {

    private final QuartzConfiguration configuration = new QuartzConfiguration();

    @Test
    void jobsAndTriggers_haveExplicitNamesGroupsAndMatchingJobKeys() {
        JobDetail cleanupJob = configuration.staleOrderCleanupJobDetail();
        JobDetail reportJob = configuration.lowStockReportJobDetail();
        Trigger hourlyCleanup = configuration.staleOrderCleanupHourlyTrigger(cleanupJob);
        Trigger nightlyCleanup = configuration.staleOrderCleanupNightlyTrigger(cleanupJob);
        Trigger dailyReport = configuration.lowStockReportDailyTrigger(reportJob);

        assertThat(cleanupJob.getKey().getName()).isEqualTo(QuartzConfiguration.STALE_ORDER_CLEANUP_JOB);
        assertThat(reportJob.getKey().getName()).isEqualTo(QuartzConfiguration.LOW_STOCK_REPORT_JOB);
        assertThat(hourlyCleanup.getKey().getGroup()).isEqualTo(QuartzConfiguration.JOB_GROUP);
        assertThat(nightlyCleanup.getJobKey()).isEqualTo(cleanupJob.getKey());
        assertThat(dailyReport.getJobKey()).isEqualTo(reportJob.getKey());
    }

    @Test
    void triggerDataOverridesJobDefaultsButKeepsOtherDefaultValues() {
        JobDetail cleanupJob = configuration.staleOrderCleanupJobDetail();
        Trigger hourlyCleanup = configuration.staleOrderCleanupHourlyTrigger(cleanupJob);
        JobDataMap mergedData = new JobDataMap(cleanupJob.getJobDataMap());
        mergedData.putAll(hourlyCleanup.getJobDataMap());

        assertThat(mergedData.getIntValue(QuartzConfiguration.BATCH_SIZE)).isEqualTo(25);
        assertThat(mergedData.getIntValue(QuartzConfiguration.STALE_HOURS)).isEqualTo(24);
    }

    @Test
    void everyTrigger_hasAnExplicitMisfireInstruction() {
        JobDetail cleanupJob = configuration.staleOrderCleanupJobDetail();
        JobDetail reportJob = configuration.lowStockReportJobDetail();
        SimpleTrigger hourlyCleanup = (SimpleTrigger) configuration.staleOrderCleanupHourlyTrigger(cleanupJob);
        CronTrigger nightlyCleanup = (CronTrigger) configuration.staleOrderCleanupNightlyTrigger(cleanupJob);
        CronTrigger dailyReport = (CronTrigger) configuration.lowStockReportDailyTrigger(reportJob);

        assertThat(hourlyCleanup.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
        assertThat(nightlyCleanup.getMisfireInstruction())
                .isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        assertThat(dailyReport.getMisfireInstruction())
                .isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }

    @Test
    void cronTriggers_haveAnExplicitKyivTimeZone() {
        JobDetail cleanupJob = configuration.staleOrderCleanupJobDetail();
        JobDetail reportJob = configuration.lowStockReportJobDetail();
        CronTrigger nightlyCleanup = (CronTrigger) configuration.staleOrderCleanupNightlyTrigger(cleanupJob);
        CronTrigger dailyReport = (CronTrigger) configuration.lowStockReportDailyTrigger(reportJob);

        assertThat(nightlyCleanup.getTimeZone().getID()).isEqualTo(TimeZone.getTimeZone("Europe/Kyiv").getID());
        assertThat(dailyReport.getTimeZone().getID()).isEqualTo("Europe/Kyiv");
    }
}

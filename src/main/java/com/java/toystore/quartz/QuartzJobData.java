package com.java.toystore.quartz;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

final class QuartzJobData {

    private QuartzJobData() {
    }

    static int requiredPositiveInt(JobDataMap data, String key) throws JobExecutionException {
        if (!data.containsKey(key)) {
            throw new JobExecutionException("Required job parameter is missing: " + key, false);
        }

        try {
            int value = data.getIntValue(key);
            if (value <= 0) {
                throw new JobExecutionException("Job parameter must be positive: " + key, false);
            }
            return value;
        } catch (ClassCastException exception) {
            throw new JobExecutionException("Job parameter must be an integer: " + key, exception, false);
        }
    }

    static String requiredText(JobDataMap data, String key) throws JobExecutionException {
        String value = data.getString(key);
        if (value == null || value.isBlank()) {
            throw new JobExecutionException("Required job parameter is missing: " + key, false);
        }
        return value;
    }
}

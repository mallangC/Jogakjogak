package com.zb.jogakjogak.notification.schedule;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class NotificationScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void runFirstJob() throws Exception{
        JobParameters jobParameter = new JobParametersBuilder()
                .addString("timestamp", String.valueOf(new Date().getTime()))
                .toJobParameters();
        jobLauncher.run(jobRegistry.getJob("sendNotification"), jobParameter);
    }
}

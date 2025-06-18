package com.zb.jogakjogak.bizmessage.schedule;


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
public class BizMessageSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = " 0 0 10 * * *", zone = "Asia/Seoul")
    public void runFirstJob() throws Exception{
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());

        /*
        String runId = UUID.randomUUID().toString(); // 또는 System.currentTimeMillis()
        JobParameters jobParameter = new JobParametersBuilder()
                .addString("run.id", runId)
                .toJobParameters();

         */

        JobParameters jobParameter = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .addString("date", date)
                .toJobParameters();
        jobLauncher.run(jobRegistry.getJob("secondJob"), jobParameter);
    }
}

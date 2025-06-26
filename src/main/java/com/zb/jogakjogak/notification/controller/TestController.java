package com.zb.jogakjogak.notification.controller;


import com.zb.jogakjogak.notification.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/biz/send")
@RequiredArgsConstructor
public class TestController {

    /**
     * 테스용 이메일발송 컨트롤러
     */
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Hidden
    @PostMapping
    public ResponseDto bizMessageSend() throws Exception{
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("timestamp", String.valueOf(new Date().getTime()))
                .toJobParameters();
        jobLauncher.run(jobRegistry.getJob("sendNotification"), jobParameters);
        return new ResponseDto("완료");
    }
}

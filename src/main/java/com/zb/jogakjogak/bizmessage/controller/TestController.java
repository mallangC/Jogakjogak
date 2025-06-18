package com.zb.jogakjogak.bizmessage.controller;


import com.zb.jogakjogak.bizmessage.dto.ResponseDto;
import com.zb.jogakjogak.bizmessage.service.BizMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/biz/send")
@RequiredArgsConstructor
public class TestController {

    private final BizMessageService bizMessageService;
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    @PostMapping
    public ResponseDto bizMessageSend() throws Exception{
        jobLauncher.run(jobRegistry.getJob("secondJob"), new JobParameters());
        return new ResponseDto("완료");
    }
}

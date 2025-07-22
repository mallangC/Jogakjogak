package com.zb.jogakjogak.notification.config;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.notification.dto.NotificationDto;
import com.zb.jogakjogak.notification.service.NotificationService;
import com.zb.jogakjogak.security.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationBatchConfig {
    private static final int CHUNK_SIZE = 20;
    private static final int PAGE_SIZE = 20;
    private static final int RETRY_LIMIT = 3;
    private static final int SKIP_SIZE = 10;
    private static final int NOTIFICATION_THRESHOLD_DAYS = 3;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JDRepository jdRepository;
    private final NotificationService notificationService;

    @Bean
    public Job notificationJob(){

        return new JobBuilder("sendNotification", jobRepository)
                .start(sendNotification())
                .build();
    }

    @Bean
    public Step sendNotification(){

        return new StepBuilder("sendNotification", jobRepository)
                .<JD, JD> chunk(CHUNK_SIZE, platformTransactionManager)
                .startLimit(RETRY_LIMIT)
                .reader(jdReader())
                .processor(jdProcessor())
                .writer(jdWriter())
                .faultTolerant()
                .skipLimit(SKIP_SIZE)
                .skip(Exception.class)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<JD> jdReader(){
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(NOTIFICATION_THRESHOLD_DAYS);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

        return new RepositoryItemReaderBuilder<JD>()
                .name("jDReader")
                .pageSize(PAGE_SIZE)
                .methodName("findNotUpdatedJdByQueryDsl")
                .arguments(List.of(now, threeDaysAgo, todayStart))
                .repository(jdRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<JD, JD> jdProcessor(){
        return jd -> {
            jd.setNotificationCount(jd.getNotificationCount() + 1);
            jd.setLastNotifiedAt(LocalDateTime.now());
            return jd;
        };
    }

    @Bean
    public ItemWriter<JD> jdWriter() {
        return jds -> {
            Map<Member, List<JD>> groupedByMember = new HashMap<>();
            for (JD jd : jds) {
                Member member = jd.getMember();
                groupedByMember.computeIfAbsent(member, k -> new ArrayList<>()).add(jd);
            }

            if (!jds.isEmpty()) {
                jdRepository.saveAll(jds);
            }

            for (Map.Entry<Member, List<JD>> entry : groupedByMember.entrySet()) {
                Member member = entry.getKey();
                List<JD> jdList = entry.getValue();

                NotificationDto notificationDto = NotificationDto.builder()
                        .member(member)
                        .jdList(jdList)
                        .build();
                try {
                    notificationService.sendNotificationEmail(notificationDto);
                } catch (Exception e) {
                    log.warn("이메일 전송에 실패했습니다. memberId={}, error={}", member.getId(), e.getMessage());
                }
            }
        };
    }
}

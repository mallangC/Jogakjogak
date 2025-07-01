package com.zb.jogakjogak.notification.batch;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.notification.entity.Notification;
import com.zb.jogakjogak.notification.repository.NotificationRepository;
import com.zb.jogakjogak.notification.service.NotificationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class NotificationBatchConfig {
    private static final int CHUNK_SIZE = 20;
    private static final int PAGE_SIZE = 20;
    private static final int RETRY_LIMIT = 3;
    private static final int NOTIFICATION_THRESHOLD_DAYS = 3;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JDRepository jdRepository;
    private final ToDoListRepository toDoListRepository;
    private final NotificationRepository notificationRepository;
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
                .reader(JDReader())
                .processor(processor())
                .writer(JDWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<JD> JDReader(){
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(NOTIFICATION_THRESHOLD_DAYS);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

        return new RepositoryItemReaderBuilder<JD>()
                .name("JDReader")
                .pageSize(PAGE_SIZE)
                .methodName("findAll")
                //.methodName("findJdToNotify")
                //.arguments(List.of(now, threeDaysAgo, todayStart))
                .repository(jdRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<JD, JD> processor(){
        return jd -> {
            Notification notification = createNotification(jd);
            jd.setNotification(notification);

            notificationService.sendNotificationEmail(notification);

            jd.setNotificationCount(jd.getNotificationCount() + 1);
            jd.setLastNotifiedAt(LocalDateTime.now());
            return jd;
        };
    }

    @Bean
    public RepositoryItemWriter<JD> JDWriter(){
        return new RepositoryItemWriterBuilder<JD>()
                .repository(jdRepository)
                .methodName("save")
                .build();
    }

    private Notification createNotification(JD jd){
        return notificationRepository.findByMemberId(jd.getMember().getId())
                .orElseGet(() -> {
                    Notification newNotification = Notification.builder()
                            .member(jd.getMember())
                            .jdList(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .build();
                    return notificationRepository.save(newNotification);
                });
    }
}

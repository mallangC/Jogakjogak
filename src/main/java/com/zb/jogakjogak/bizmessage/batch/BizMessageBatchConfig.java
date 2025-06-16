package com.zb.jogakjogak.bizmessage.batch;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
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
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class BizMessageBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JDRepository jdRepository;


    @Bean
    public Job secondJob(){

        return new JobBuilder("secondJob", jobRepository)
                .start(secondStep())
                .build();
    }

    @Bean
    public Step secondStep(){

        return new StepBuilder("secondStep", jobRepository)
                .<JD, JD> chunk(10, platformTransactionManager)
                .reader(JDReader())
                .processor(processor())
                .writer(JDWriter())
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<JD> JDReader(){
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime test = LocalDateTime.now();
        return new RepositoryItemReaderBuilder<JD>()
                .name("JDReader")
                .pageSize(10)
                .methodName("findOutdatedJD")
                .arguments(List.of(test))
                .repository(jdRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<JD, JD> processor(){

        return item -> {
            return item;
        };
    }

    @Bean
    public RepositoryItemWriter<JD> JDWriter(){
        // 알림톡서비스 로직
        return new RepositoryItemWriterBuilder<JD>()
                .repository(jdRepository)
                .methodName("save")
                .build();
    }
}

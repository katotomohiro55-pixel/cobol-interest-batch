package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Spring Batch configuration for the interest calculation job.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, 
                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }
    
    @Bean
    public Job interestCalculationJob(Step calculateInterestStep) {
        return jobBuilderFactory.get("interestCalculationJob")
                .incrementer(new RunIdIncrementer())
                .start(calculateInterestStep)
                .build();
    }
    
    @Bean
    public Step calculateInterestStep(AccountItemReader reader,
                                      InterestCalculationProcessor processor,
                                      ReportItemWriter writer) {
        return stepBuilderFactory.get("calculateInterestStep")
                .<AccountRecord, DetailRecord>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    
    @Bean
    public AccountItemReader reader() {
        return new AccountItemReader("data/accounts.dat");
    }
    
    @Bean
    public InterestCalculationProcessor processor() {
        return new InterestCalculationProcessor();
    }
    
    @Bean
    public ReportItemWriter writer() {
        return new ReportItemWriter("output/java_output.dat");
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }
}

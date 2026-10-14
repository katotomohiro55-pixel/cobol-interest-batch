package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Main application to run the interest calculation batch job.
 */
public class Application {
    
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(BatchConfiguration.class)) {
            
            JobLauncher jobLauncher = context.getBean(JobLauncher.class);
            Job job = context.getBean(Job.class);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution execution = jobLauncher.run(job, jobParameters);
            
            System.out.println("Job Status: " + execution.getStatus());
            System.out.println("Exit Status: " + execution.getExitStatus());
            
            if (execution.getStatus().isUnsuccessful()) {
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

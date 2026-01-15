package com.training.coach.shared.config;

import java.util.concurrent.Executor;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
public class BootstrapExecutorConfiguration {

    @Bean(name = "bootstrapExecutor")
    public Executor bootstrapExecutor(ThreadPoolTaskExecutorBuilder builder) {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = builder.threadNamePrefix("bootstrap-")
                .corePoolSize(Math.max(2, processors))
                .maxPoolSize(Math.max(4, processors * 2))
                .queueCapacity(256)
                .build();
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}

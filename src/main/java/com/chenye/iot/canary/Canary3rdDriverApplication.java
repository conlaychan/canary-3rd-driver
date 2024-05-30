package com.chenye.iot.canary;

import com.chenye.iot.canary.utils.RedisChannel;
import com.google.common.base.Splitter;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@MapperScan(basePackages = "com.chenye.iot.canary.dao")
public class Canary3rdDriverApplication {
    public static void main(String[] args) {
        System.setProperty("LOG_FILE", "logs/Canary3rdDriver.log");
        SpringApplication.run(Canary3rdDriverApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources lettuceClientResources() {
        return DefaultClientResources.builder()
                .computationThreadPoolSize(2)
                .build();
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            Canary3rdDriverInstanceScheduler driverScheduler,
            RedisConnectionFactory connectionFactory
    ) {
        TaskQueue taskQueue = new TaskQueue(10_0000);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                1, 2, 1, TimeUnit.MINUTES, taskQueue,
                r -> {
                    Thread t = new Thread(r, "Redis消息监听执行器");
                    t.setDaemon(true);
                    return t;
                }
        );
        taskQueue.setParent(threadPoolExecutor);
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                driverScheduler,
                Arrays.asList(
                        new ChannelTopic(RedisChannel.DOWN_DEVICE_PROPERTY_VALUE),
                        new ChannelTopic(RedisChannel.DOWN_DEVICE_SERVICE_INTPUT)
                )
        );
        container.setSubscriptionExecutor(threadPoolExecutor);
        container.setTaskExecutor(threadPoolExecutor);
        return container;
    }

    @Bean
    public ApiInfo apiInfo(Environment environment) {
        String appName = environment.getProperty("spring.application.name");
        if (!StringUtils.hasText(appName)) {
            appName = ApiInfo.DEFAULT.getTitle();
        }
        return new ApiInfo(
                appName,
                appName,
                "0.0.0",
                ApiInfo.DEFAULT.getTermsOfServiceUrl(),
                ApiInfo.DEFAULT_CONTACT,
                null,
                null,
                new ArrayList<>()
        );
    }

    @Bean
    public Docket docket(@Value("${swagger-packages}") String swaggerPackages, ApiInfo apiInfo) {
        List<String> packs = Splitter.on(",").omitEmptyStrings().splitToList(swaggerPackages);
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(handler -> packs.stream().anyMatch(strPackage -> handler.declaringClass().getPackage().getName().startsWith(strPackage)))
                .paths(PathSelectors.any())
                .build();
    }
}

package com.lookfit.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 설정
 * ProductEventListener의 @Async 메서드를 위한 설정
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}

package com.modoospace.config.redis;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class CachePrefixEvictAdvisor {

  private final RedisTemplate redisTemplate;

  @Around("@annotation(com.modoospace.config.redis.CachePrefixEvict)")
  public Object processCachePrefixEvict(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    CachePrefixEvict annotation = methodSignature.getMethod().getAnnotation(CachePrefixEvict.class);

    // TODO : SpEL로 넘어온값 해석하는 Parser 만들어줘야함.
    log.info("cacheNames = {}, key = {} redis evict try", annotation.cacheNames(),
        annotation.key());
    Set<String> keys = redisTemplate.keys(annotation.key() + "*");
    if (keys != null) {
      log.info("{} clear complete", keys);
      redisTemplate.delete(keys);
    }

    return joinPoint.proceed();
  }
}

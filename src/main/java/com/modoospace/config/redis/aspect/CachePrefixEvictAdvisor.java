package com.modoospace.config.redis.aspect;

import com.modoospace.common.CustomSpELParser;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class CachePrefixEvictAdvisor {

  @Autowired
  StringRedisTemplate redisTemplate;

  @Around("@annotation(com.modoospace.config.redis.aspect.CachePrefixEvict)")
  public Object processCachePrefixEvict(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    CachePrefixEvict annotation = methodSignature.getMethod().getAnnotation(CachePrefixEvict.class);
    Object prefix = CustomSpELParser.extractValueFromExpression(
        methodSignature.getParameterNames(), joinPoint.getArgs(), annotation.key()
    );

    String pattern = String.format("%s::%s:*", annotation.cacheNames(), prefix);
    log.info("key pattern ({}) evict try in redis", pattern);
    Set<String> keys = redisTemplate.keys(pattern);
    if (keys != null) {
      log.info("{} clear complete", keys);
      redisTemplate.delete(keys);
    }

    return joinPoint.proceed();
  }
}

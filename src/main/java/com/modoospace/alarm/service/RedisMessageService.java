package com.modoospace.alarm.service;

import com.modoospace.alarm.publisher.RedisPublisher;
import com.modoospace.alarm.subscriber.RedisSubscribeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMessageService {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final RedisPublisher redisPublisher;
    private final RedisSubscribeListener redisSubscribeListener;

    // 채널 구독
    public void subscribe(String channel) {
        redisMessageListenerContainer.addMessageListener(redisSubscribeListener,
                ChannelTopic.of(channel));
    }

    // 이벤트 발행
    public void publish(String channel, Object message) {
        redisPublisher.publish(channel, message);
    }
}

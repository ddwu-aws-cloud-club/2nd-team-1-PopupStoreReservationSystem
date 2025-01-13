package com.westsomsom.finalproject.global.config;

import com.westsomsom.finalproject.chat.application.ChatMessagePublisher;
import com.westsomsom.finalproject.chat.application.ChatMessageSubscriber;
import com.westsomsom.finalproject.reservation.application.ReservationPublisher;
import com.westsomsom.finalproject.reservation.application.ReservationSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer container(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            MessageListenerAdapter chatListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 예약 관련 리스너 등록
        container.addMessageListener(listenerAdapter, new ChannelTopic("reservationChannel"));

        /// 채팅 관련 리스너 등록
        container.addMessageListener(chatListenerAdapter, chatTopic());

        return container;
    }

    // 예약 메시지 리스너 어댑터
    @Bean
    public MessageListenerAdapter listenerAdapter(ReservationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    // 채팅 메시지 리스너 어댑터
    @Bean
    public MessageListenerAdapter chatListenerAdapter(ChatMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    // 예약 채널 설정
    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("reservationChannel");
    }

    // 채팅 채널 설정
    @Bean
    public ChannelTopic chatTopic() {
        return new ChannelTopic("chatChannel");
    }

    // 예약 Publisher
    @Bean
    public ReservationPublisher reservationPublisher(RedisTemplate<String, Object> redisTemplate) {
        return new ReservationPublisher(redisTemplate);
    }

    // 채팅 Publisher
    @Bean
    public ChatMessagePublisher chatMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        return new ChatMessagePublisher(redisTemplate, chatTopic());
    }
}

package com.westsomsom.finalproject.global.config;

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
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic("reservationChannel"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(ReservationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("reservationChannel");
    }

    @Bean
    public ReservationPublisher reservationPublisher(RedisTemplate<String, Object> redisTemplate) {
        return new ReservationPublisher(redisTemplate);
    }
}

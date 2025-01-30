package com.westsomsom.finalproject.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketChatHandler webSocketHandler;
    private final WebSocketReservationHandler webSocketReservationHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(webSocketHandler, "/ws/conn")
                .addHandler(webSocketReservationHandler, "/ws-reservation")
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*");
    }
}

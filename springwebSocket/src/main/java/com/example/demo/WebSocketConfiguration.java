package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
	    registry.enableSimpleBroker("/topic"); // broker for all subscibers of /topic/* 
        registry.setApplicationDestinationPrefixes("/app"); // controller endpoint connect prefix 
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// making /ImgReg channel for clients to connect to
		//setAllowedOrigins presents a vunrability for testing only
		registry.addEndpoint("/ImgReg").setAllowedOriginPatterns("*").withSockJS();
	}
	
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		registry.setMessageSizeLimit(200000); // default : 64 * 1024
		registry.setSendTimeLimit(20 * 10000); // default : 10 * 10000
		registry.setSendBufferSizeLimit(3* 512 * 1024); // default : 512 * 1024
	}
	
}

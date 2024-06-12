package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableHystrix
public class GatwayConfig {

    @Autowired
    AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("ms-auth", r -> r.path("/auth/**")
                        .filters(f -> f.rewritePath("/auth/(?<s>.*)", "/${s}").filter(filter))
                        .uri("lb://ms-auth"))

                .route("ms-payment", r -> r.path("/payment/**")
                        .filters(f -> f.rewritePath("/payment/(?<s>.*)", "/${s}").filter(filter))
                        .uri("lb://ms-payment"))

                .route("ms-cours", r -> r.path("/cours/**")
                        .filters(f -> f.rewritePath("/cours/(?<s>.*)", "/${s}").filter(filter))
                        .uri("lb://ms-cours"))

                .route("ms-forum", r -> r.path("/forum/**")
                        .filters(f -> f.rewritePath("/forum/(?<s>.*)", "/${s}").filter(filter))
                        .uri("lb://ms-forum"))

                .route("video_call_app", r -> r.path("/conf/**")
                        .filters(f -> f.rewritePath("/conf/(?<s>.*)", "/${s}"))
                        .uri("http://localhost:8080"))

                .build();
    }

}

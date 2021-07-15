package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration config = new CorsConfiguration();
        //允许跨域的域名  如果是 * 代表所有域名 无法携带cookie
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://localhost:1000");
        config.addAllowedOrigin("http://www.gmall.com");
        config.addAllowedOrigin("http://gmall.com");
        //允许所有请求方式跨域访问  get put delete post
        config.addAllowedMethod("*");
        //允许所有携带任意的头信息来进行访问
        config.addAllowedHeader("*");
        //允许携带cookie  如果允许携带cookie  那么origin不能写成*
        config.setAllowCredentials(true);
        //cors注册类 拦截所有请求进行cors验证
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(configurationSource);
    }
}

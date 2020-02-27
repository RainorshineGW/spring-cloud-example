package com.example.gateway.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.client.loadbalancer.RetryLoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    private RetryLoadBalancerInterceptor loadBalancerInterceptor;

    @Bean
    public LoadBalancedJwtAccessTokenConverterRestTemplateCustomizer loadBalancedJwtAccessTokenConverterRestTemplateCustomizer() {
        // 设置启动时,restTemplate访问token_key采用LoadBalanced策略
        return new LoadBalancedJwtAccessTokenConverterRestTemplateCustomizer(loadBalancerInterceptor);
    }

    @Autowired
    private ResourceServerProperties resourceServerProperties;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(resourceServerProperties.getResourceId());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 允许所有的Endpoint端点链接
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .antMatchers("/oauth/*").permitAll();
    }

}

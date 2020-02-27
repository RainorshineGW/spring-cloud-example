package com.example.gateway.config.security.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

import com.example.gateway.service.OAuth2AuthenticationService;

@Component
public class RefreshTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private OAuth2AuthenticationService oAuth2AuthenticationService;

    @Override
    public void configure(HttpSecurity http) {
        RefreshTokenFilter customFilter = new RefreshTokenFilter(tokenStore, oAuth2AuthenticationService);
        http.addFilterBefore(customFilter, OAuth2AuthenticationProcessingFilter.class);
    }

}
package com.example.gateway.config.security;

import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.google.common.collect.Iterators;

public class HeaderHttpServletRequest extends HttpServletRequestWrapper {
    private final String AUTHORIZATION = "Authorization";
    private Enumeration<String> authorizationHeader;

    public HeaderHttpServletRequest(HttpServletRequest request, String token) {
        super(request);
        token = OAuth2AccessToken.BEARER_TYPE + " " + token;
        authorizationHeader = Iterators.asEnumeration(Arrays.asList(token).iterator());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        // BearerTokenExtractor:73 extractHeaderToken(HttpServletRequest request)
        // 重写getHeaders，针对OAuth2AuthenticationProcessingFilter中获取Header取出token
        if (AUTHORIZATION.equals(name)) {
            return authorizationHeader;
        }
        return super.getHeaders(name);
    }
}

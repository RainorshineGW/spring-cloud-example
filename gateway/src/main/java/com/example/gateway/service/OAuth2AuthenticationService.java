package com.example.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class OAuth2AuthenticationService {

    private final String OAUTH_TOKEN = "http://uaa/oauth/token";

    @Autowired
    private OAuth2ClientProperties oAuth2ClientProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 刷新token
     */
    public OAuth2AccessToken refreshToken(String refreshTokenValue) {
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.set(OAuth2Utils.CLIENT_ID, oAuth2ClientProperties.getClientId());
        formParams.set("client_secret", oAuth2ClientProperties.getClientSecret());
        formParams.set(OAuth2Utils.GRANT_TYPE, OAuth2AccessToken.REFRESH_TOKEN);
        formParams.set(OAuth2AccessToken.REFRESH_TOKEN, refreshTokenValue);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, reqHeaders);
        ResponseEntity<OAuth2AccessToken>
                responseEntity = restTemplate.postForEntity(OAUTH_TOKEN, entity, OAuth2AccessToken.class);
        return responseEntity.getBody();
    }
}

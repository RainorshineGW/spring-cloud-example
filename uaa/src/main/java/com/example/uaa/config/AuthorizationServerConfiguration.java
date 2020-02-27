package com.example.uaa.config;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
@EnableConfigurationProperties(AuthorizationServerProperties.class)
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AuthorizationServerProperties authorizationServerProperties;

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserNameDetailsService userNameDetailsService;

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        AuthorizationServerProperties.Jwt jwtProperties = authorizationServerProperties.getJwt();
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource(jwtProperties.getKeyStore()),
                jwtProperties.getKeyPassword().toCharArray()).getKeyPair(jwtProperties.getKeyAlias());
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Bean
    public JwtTokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * 配置OAuth2的客户端相关信息
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    }

    /**
     * 配置AuthorizationServer安全认证的相关信息，创建ClientCredentialsTokenEndpointFilter核心过滤器
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer
                // 开启/oauth/token_key验证端口权限访问
                .tokenKeyAccess(authorizationServerProperties.getTokenKeyAccess())
                // 开启/oauth/check_token验证端口认证权限访问
                .checkTokenAccess(authorizationServerProperties.getCheckTokenAccess())
                .allowFormAuthenticationForClients();
    }

    /**
     * 配置AuthorizationServerEndpointsConfigurer众多相关类
     * 包括配置身份认证器，配置认证方式，TokenStore，TokenGranter，OAuth2RequestFactory
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

        //pick up all  TokenEnhancers incl. those defined in the application
        //this avoids changes to this class if an application wants to add its own to the chain
        Collection<TokenEnhancer> tokenEnhancers = applicationContext.getBeansOfType(TokenEnhancer.class).values();
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(new ArrayList<>(tokenEnhancers));

        endpoints
                .userDetailsService(userNameDetailsService)
                .authenticationManager(authenticationManager)
                .tokenStore(jwtTokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .reuseRefreshTokens(false);             //don't reuse or we will run into session inactivity timeouts
    }

}

package com.example.gateway.config.security.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.filter.GenericFilterBean;

import com.example.gateway.config.security.HeaderHttpServletRequest;
import com.example.gateway.service.OAuth2AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zgw
 * 刷新token过滤器
 */
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenFilter extends GenericFilterBean {
    // 距离过期时间剩余的秒数
    private static final int REFRESH_WINDOW_SECS = 30;
    public static final String REFRESH_TOKEN = "Refresh-Token";
    public static final String ACCESS_TOKEN = "Access-Token";

    private TokenExtractor tokenExtractor = new BearerTokenExtractor();

    private final TokenStore tokenStore;
    private final OAuth2AuthenticationService oAuth2AuthenticationService;


    /**
     * Check access token and refresh it, if it is either not present, expired or about to expire.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        try {
            httpServletRequest = refreshTokensIfExpiring(httpServletRequest, httpServletResponse);
        } catch (ClientAuthenticationException ex) {
            log.warn("Security exception: could not refresh tokens", ex);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private HttpServletRequest refreshTokensIfExpiring(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // 取出请求头中的token，如果携带token则查看是否过期，过期了就刷新token,否则不做处理
        Authentication authentication = tokenExtractor.extract(httpServletRequest);
        if (Objects.isNull(authentication)) {
            return httpServletRequest;
        }
        String token = (String) authentication.getPrincipal();
        // 读取token中的信息
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
        if (mustRefreshToken(accessToken)) {
            // token过期或者即将过期
            // 取出refreshToken
            String refreshToken = this.extractHeaderRefreshToken(httpServletRequest);
            if (Objects.isNull(refreshToken)) {
                log.warn("refreshToken not found");
            } else {
                OAuth2AccessToken authenticate = oAuth2AuthenticationService.refreshToken(refreshToken);
                String oAuth2AccessToken = authenticate.getValue();
                httpServletRequest = new HeaderHttpServletRequest(httpServletRequest, oAuth2AccessToken);
                httpServletResponse.setHeader(ACCESS_TOKEN, oAuth2AccessToken);
                httpServletResponse.setHeader(REFRESH_TOKEN, authenticate.getRefreshToken().getValue());
            }
        }
        return httpServletRequest;
    }


    private boolean mustRefreshToken(OAuth2AccessToken accessToken) {
        // check if token is expired or about to expire
        return (accessToken.isExpired() || accessToken.getExpiresIn() < REFRESH_WINDOW_SECS);
    }

    private String extractHeaderRefreshToken(HttpServletRequest request) {
        return request.getHeader(REFRESH_TOKEN);
    }
}

package com.nlizzard.config;

import com.nlizzard.base.BaseInfoProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器：把网关注入到当前请求头中的用户身份信息透传给下游微服务。
 *
 * 背景：微服务间通过 OpenFeign 按服务名直连（如 file-service -> main-service），
 * 不经过网关，下游 UserInfoInterceptor 拿不到 headerUserId / headerUserTokenKey，
 * 会导致 UserContext 为 null 进而 NPE。这里在发起 Feign 调用时把上游请求头透传下去。
 */
@Configuration
public class FeignHeaderInterceptor extends BaseInfoProperties implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 当前线程没有 HTTP 请求上下文（如异步/定时任务里发起的 Feign 调用），无法透传
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader(HEADER_USER_ID);
        String redisTokenKey = request.getHeader(HEADER_USER_TOKEN_KEY);
        if (userId != null) {
            template.header(HEADER_USER_ID, userId);
        }
        if (redisTokenKey != null) {
            template.header(HEADER_USER_TOKEN_KEY, redisTokenKey);
        }
    }
}

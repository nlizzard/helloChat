package com.nlizzard.filter;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilterToken extends BaseInfoProperties implements GlobalFilter, Ordered {


    private final ExcludeUrlProperties excludeUrlProperties;

    // 路径匹配规则器
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1. 获得当前用户请求的路径url
        String url = exchange.getRequest().getURI().getPath();

        // 2. 获得所有的需要排除校验的url list
        List<String> excludeList = excludeUrlProperties.getUrls();

        // 3. 校验并且排除excludeList
        if (excludeList != null && !excludeList.isEmpty()) {
            for (String excludeUrl : excludeList) {
                if (antPathMatcher.matchStart(excludeUrl, url)) {
                    // 如果匹配到，则直接放行，表示当前的url是不需要被拦截校验的
                    return chain.filter(exchange);
                }
            }
        }

        // 3.2 放行静态资源路径
        String fileStart = excludeUrlProperties.getFileStart();
        if(StringUtils.isNotBlank(fileStart)){
            if (antPathMatcher.matchStart(fileStart, url)) {
                // 如果匹配到，则直接放行，表示当前的url是不需要被拦截校验的
                return chain.filter(exchange);
            }
        }

        // 4. 代码到达此处，表示请求被拦截，需要进行校验
        //log.info("当前请求的路径[{}]被拦截...", url);

        // 5. 从header中获得用户的id以及token
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String userTokenKey = headers.getFirst(HEADER_USER_TOKEN_KEY);
        String userToken = headers.getFirst(HEADER_USER_TOKEN);

        // 6. 判断header中是否有token，对用户请求进行判断拦截
        if (StringUtils.isNotBlank(userTokenKey) && StringUtils.isNotBlank(userToken)) {

            String redisUserToken = redis.get(userTokenKey);
            if(StringUtils.isBlank(redisUserToken) || !redisUserToken.equals(userToken)){
                // 如果redis中没有token，或者token不匹配，则表示用户没有登录
                return RenderErrorUtils.display(exchange, ResponseStatusEnum.UN_LOGIN);
            }

            String userId = JwtUtil.getUserId(userToken);

            // 将用户id放入header中，继续向下游服务传递
            chain.filter(exchange.mutate()
                            .request(r -> r.header(HEADER_USER_ID, Objects.requireNonNull(userId)))
                    .build());
        }

        // 默认不放行
        return RenderErrorUtils.display(exchange, ResponseStatusEnum.UN_LOGIN);
    }

    // 过滤器的顺序，数字越小则优先级越大
    @Override
    public int getOrder() {
        return 0;
    }
}

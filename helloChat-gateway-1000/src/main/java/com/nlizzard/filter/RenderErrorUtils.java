package com.nlizzard.filter;

import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class RenderErrorUtils {

    /**
     * 重新包装并且返回错误信息
     */
    public static Mono<Void> display(ServerWebExchange exchange,
                                     ResponseStatusEnum statusEnum) {
        // 1. 获得相应response
        ServerHttpResponse response = exchange.getResponse();

        // 2. 构建jsonResult
        GraceJSONResult jsonResult = GraceJSONResult.exception(statusEnum);

        // 3. 设置header类型
        if (!response.getHeaders().containsHeader("Content-Type")) {
            response.getHeaders().add("Content-Type",
                    MimeTypeUtils.APPLICATION_JSON_VALUE);
        }

        // 4. 修改response的状态码code为500
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        // 5. 转换json并且像response中写入数据
        String resultJson = new Gson().toJson(jsonResult);
        DataBuffer buffer = response
                .bufferFactory()
                .wrap(resultJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

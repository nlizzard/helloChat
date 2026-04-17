package com.nlizzard.exceptions;

import com.nlizzard.grace.result.GraceJSONResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.nlizzard.grace.result.ResponseStatusEnum.SYSTEM_OPERATION_ERROR;

/**
 * 处理Servlet请求过程中发生的异常,系统错误兜底
 */
@ControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Slf4j
@ResponseBody
public class GraceServletExceptionHandler {

    @ExceptionHandler(Exception.class)
    public GraceJSONResult handle(Exception e, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.error("请求路径：{}，发生异常：{}", path, e.getMessage());
        return GraceJSONResult.exception(SYSTEM_OPERATION_ERROR);
    }
}

package com.nlizzard.exceptions;

import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ControllerAdvice
@Slf4j
@ResponseBody
public class GraceExceptionHandler {


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public GraceJSONResult returnMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        e.printStackTrace();
        return GraceJSONResult.exception(ResponseStatusEnum.FILE_MAX_SIZE_500KB_ERROR);
    }

    // 处理自定义异常
    @ExceptionHandler(MyCustomException.class)
    public GraceJSONResult returnMyCustomException(MyCustomException e) {
        e.printStackTrace();
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }

    // 处理对象类型参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public GraceJSONResult returnObjectParamNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> errors = getErrors(result);
        return GraceJSONResult.errorMap(errors);
    }

    // 处理单参数类型参数校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    public GraceJSONResult handlerConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> map = new HashMap<>();
        // 获取所有的校验失败信息
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : constraintViolations) {
            // 获取参数路径（例如：getSMSCode.mobile）
            String path = violation.getPropertyPath().toString();
            // 如果只想取参数名 mobile，可以进行字符串截取
            String paramName = path.substring(path.lastIndexOf(".") + 1);
            // 获取注解中定义的 message
            String message = violation.getMessage();

            map.put(paramName, message);
        }
        return GraceJSONResult.errorMap(map);
    }

    // 从BindingResult中获得错误的属性字段名和错误信息
    public Map<String, String> getErrors(BindingResult result) {

        Map<String, String> map = new HashMap<>();

        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError fe : errorList) {
            // 错误所对应的属性字段名
            String field = fe.getField();
            // 错误信息
            String message = fe.getDefaultMessage();
            map.put(field, message);
        }
        return map;
    }
}

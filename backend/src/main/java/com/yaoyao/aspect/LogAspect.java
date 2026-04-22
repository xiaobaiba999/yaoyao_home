package com.yaoyao.aspect;

import cn.hutool.json.JSONUtil;
import com.yaoyao.annotation.Log;
import com.yaoyao.common.Result;
import com.yaoyao.entity.OperationLog;
import com.yaoyao.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogService operationLogService;
    private final HttpServletRequest request;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        OperationLog operationLog = new OperationLog();
        operationLog.setModule(logAnnotation.module());
        operationLog.setOperation(logAnnotation.operation());
        operationLog.setDescription(logAnnotation.description());
        operationLog.setMethod(((MethodSignature) joinPoint.getSignature()).getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        operationLog.setRequestIp(getClientIp());
        operationLog.setUserAgent(request.getHeader("User-Agent"));
        operationLog.setCreatedAt(LocalDateTime.now());

        try {
            Object[] args = joinPoint.getArgs();
            String params = truncate(JSONUtil.toJsonStr(args), 2000);
            operationLog.setRequestParams(params);
        } catch (Exception e) {
            operationLog.setRequestParams("参数解析失败");
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            operationLog.setDuration(duration);

            if (result instanceof Result<?> res) {
                operationLog.setResponseStatus(res.getCode());
            } else {
                operationLog.setResponseStatus(200);
            }

            operationLogService.saveLog(operationLog);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            operationLog.setDuration(duration);
            operationLog.setResponseStatus(500);
            operationLog.setErrorMessage(truncate(e.getMessage(), 1000));
            operationLogService.saveLog(operationLog);
            throw e;
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}

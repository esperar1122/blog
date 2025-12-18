package com.example.blog.aspect;

import com.example.blog.entity.OperationLog;
import com.example.blog.entity.User;
import com.example.blog.mapper.OperationLogMapper;
import com.example.blog.utils.IpUtil;
import com.example.blog.utils.SensitiveDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.example.blog.annotation.OperationLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        OperationLog operationLog = new OperationLog();

        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = null;

            if (attributes != null) {
                request = attributes.getRequest();
                operationLog.setIp(IpUtil.getIpAddress(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // 获取用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                operationLog.setUserId(user.getId());
                operationLog.setUsername(user.getUsername());
            } else if (authentication != null) {
                operationLog.setUsername(authentication.getName());
            }

            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            com.example.blog.annotation.OperationLog annotation = method.getAnnotation(com.example.blog.annotation.OperationLog.class);
            if (annotation != null) {
                operationLog.setOperation(annotation.value());
            }

            operationLog.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

            // 获取请求参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String params = objectMapper.writeValueAsString(args);
                    // 脱敏处理
                    String sanitizedParams = SensitiveDataUtil.sanitizeParams(params);
                    operationLog.setParams(sanitizedParams.length() > 2000 ? sanitizedParams.substring(0, 2000) + "..." : sanitizedParams);
                } catch (Exception e) {
                    log.warn("序列化请求参数失败", e);
                    operationLog.setParams("参数序列化失败");
                }
            }

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录执行结果
            long endTime = System.currentTimeMillis();
            operationLog.setTime(endTime - startTime);
            operationLog.setCreateTime(LocalDateTime.now());

            try {
                String resultJson = objectMapper.writeValueAsString(result);
                // 脱敏处理
                String sanitizedResult = SensitiveDataUtil.sanitizeResult(resultJson);
                operationLog.setResult(sanitizedResult.length() > 2000 ? sanitizedResult.substring(0, 2000) + "..." : sanitizedResult);
            } catch (Exception e) {
                log.warn("序列化执行结果失败", e);
                operationLog.setResult("结果序列化失败");
            }

            operationLog.setStatus(1); // 成功

            // 异步保存日志
            saveLogAsync(operationLog);

            return result;

        } catch (Exception e) {
            // 记录异常信息
            long endTime = System.currentTimeMillis();
            operationLog.setTime(endTime - startTime);
            operationLog.setCreateTime(LocalDateTime.now());
            operationLog.setResult("执行失败: " + e.getMessage());
            operationLog.setStatus(0); // 失败

            // 异步保存日志
            saveLogAsync(operationLog);

            throw e;
        }
    }

    @Async("logExecutor")
    private void saveLogAsync(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
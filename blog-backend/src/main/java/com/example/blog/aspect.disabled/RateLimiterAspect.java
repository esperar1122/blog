package com.example.blog.aspect;

import com.example.blog.annotation.RateLimiter;
import com.example.blog.common.Result;
import com.example.blog.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    // Lua脚本实现滑动窗口限流
    private static final String RATE_LIMIT_SCRIPT = """
        local key = KEYS[1]
        local limit = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local current_time = tonumber(ARGV[3])

        -- 移除过期的记录
        redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window * 1000)

        -- 获取当前窗口内的请求次数
        local current_requests = redis.call('ZCARD', key)

        if current_requests < limit then
            -- 添加当前请求记录
            redis.call('ZADD', key, current_time, current_time)
            -- 设置过期时间
            redis.call('EXPIRE', key, window)
            return 1
        else
            return 0
        end
        """;

    @Around("@annotation(rateLimiter)")
    public Object rateLimiter(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        // 获取请求标识（IP地址或用户ID）
        String key = getRateLimitKey(joinPoint, rateLimiter);

        // 执行限流检查
        boolean allowed = checkRateLimit(key, rateLimiter);

        if (!allowed) {
            log.warn("API访问频率超限: key={}, limit={}/{}s", key, rateLimiter.limit(), rateLimiter.timeWindow());
            throw new BusinessException(429, rateLimiter.message());
        }

        return joinPoint.proceed();
    }

    private String getRateLimitKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        StringBuilder keyBuilder = new StringBuilder(rateLimiter.prefix());

        try {
            // 尝试获取HttpServletRequest
            Object request = null;
            try {
                request = org.springframework.web.context.request.RequestContextHolder
                    .getRequestAttributes().getRequest();
            } catch (Exception e) {
                // 忽略异常，使用默认值
            }

            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;

                // 优先使用用户ID（如果已认证）
                String userId = httpRequest.getHeader("X-User-ID");
                if (userId != null && !userId.isEmpty()) {
                    keyBuilder.append("user:").append(userId);
                } else {
                    // 使用IP地址
                    String clientIp = getClientIp(httpRequest);
                    keyBuilder.append("ip:").append(clientIp);
                }
            } else {
                // 如果无法获取请求信息，使用方法名
                String methodName = joinPoint.getSignature().getName();
                keyBuilder.append("method:").append(methodName);
            }

            // 添加方法名作为后缀，实现细粒度限流
            keyBuilder.append(":").append(joinPoint.getSignature().getName());

        } catch (Exception e) {
            log.warn("获取限流键失败，使用默认键", e);
            keyBuilder.append("default");
        }

        return keyBuilder.toString();
    }

    private boolean checkRateLimit(String key, RateLimiter rateLimiter) {
        try {
            DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
            script.setScriptText(RATE_LIMIT_SCRIPT);
            script.setResultType(Boolean.class);

            Long result = redisTemplate.execute(
                script,
                Arrays.asList(key),
                String.valueOf(rateLimiter.limit()),
                String.valueOf(rateLimiter.timeWindow()),
                String.valueOf(System.currentTimeMillis())
            );

            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("Redis限流检查失败，允许请求通过", e);
            // 限流失败时，允许请求通过（避免影响正常业务）
            return true;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
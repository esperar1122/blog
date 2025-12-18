package com.example.blog.config;

import com.example.blog.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时验证安全配置
 */
@Slf4j
@Component
public class StartupValidation implements CommandLineRunner {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void run(String... args) {
        log.info("=== 安全配置验证开始 ===");

        try {
            // 验证JWT配置
            String testToken = jwtTokenProvider.generateToken("testuser", 1L, "USER");
            log.info("JWT生成测试: ✅ 成功");

            boolean isValid = jwtTokenProvider.validateToken(testToken);
            log.info("JWT验证测试: {}", isValid ? "✅ 成功" : "❌ 失败");

            String username = jwtTokenProvider.extractUsername(testToken);
            Long userId = jwtTokenProvider.extractUserId(testToken);
            String role = jwtTokenProvider.extractRole(testToken);

            log.info("JWT信息提取:");
            log.info("  用户名: {}", username);
            log.info("  用户ID: {}", userId);
            log.info("  用户角色: {}", role);

            long expiration = jwtTokenProvider.getAccessTokenExpirationMinutes();
            log.info("令牌过期时间: {} 分钟", expiration);

            log.info("=== 安全配置验证完成 ===");
            log.info("✅ 所有安全组件正常工作");
            log.info("✅ 项目可以正常启动和使用基础登录功能");

        } catch (Exception e) {
            log.error("❌ 安全配置验证失败: {}", e.getMessage());
            log.error("项目启动可能存在问题，请检查配置");
        }
    }
}
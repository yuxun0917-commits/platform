package com.platform.starter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器自动配置
 *
 * <p>基于 Spring Security 的 {@link BCryptPasswordEncoder} 实现，
 * 仅引入 {@code spring-security-crypto} 模块，不触发 Spring Security 的过滤器链自动装配。</p>
 *
 * <p>使用方式：业务模块引入 {@code project-starter} 的 Maven 坐标后，
 * 直接通过 {@code @Autowired PasswordEncoder passwordEncoder} 注入即可使用。</p>
 *
 * <ul>
 *   <li>加密：{@code passwordEncoder.encode(rawPassword)} → 每次生成不同的盐，密文不同</li>
 *   <li>校验：{@code passwordEncoder.matches(rawPassword, encodedPassword)} → 自动提取盐值比对</li>
 * </ul>
 *
 * @author platform
 */
@AutoConfiguration
@ConditionalOnClass(PasswordEncoder.class)
public class PasswordEncoderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PasswordEncoderAutoConfiguration.class);

    /**
     * 注入 BCrypt 密码编码器
     *
     * <p>强度参数默认 10，即 2^10 = 1024 轮迭代，兼顾安全性与性能。</p>
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        log.info("[Security] BCryptPasswordEncoder 初始化成功 | 使用方式: @Autowired PasswordEncoder");
        return encoder;
    }
}

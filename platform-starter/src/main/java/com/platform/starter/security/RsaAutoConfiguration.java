package com.platform.starter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * RSA 加密组件自动配置
 *
 * <p>业务模块引入 {@code project-starter} 后，直接通过
 * {@code @Autowired RsaComponent rsaComponent} 注入即可使用。</p>
 *
 * @author platform
 */
@AutoConfiguration
public class RsaAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RsaAutoConfiguration.class);

    /**
     * 注入 RSA 加密组件（启动时生成密钥对，私钥驻留内存）
     *
     * @return RsaComponent 实例
     */
    @Bean
    @ConditionalOnMissingBean(RsaComponent.class)
    public RsaComponent rsaComponent() {
        RsaComponent component = new RsaComponent();
        log.info("[RSA] RsaComponent 初始化成功 | 使用方式: @Autowired RsaComponent");
        return component;
    }
}

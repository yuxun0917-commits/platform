package com.platform.starter.mapperfacade;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * MapperFacade
 *
 * @author platform
 */
@AutoConfiguration
//@ConditionalOnClass(MapperFacade.class)
public class MapperFacadeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MapperFacadeAutoConfiguration.class);

    /**
     * 注入 BCrypt 密码编码器
     *
     * <p>强度参数默认 10，即 2^10 = 1024 轮迭代，兼顾安全性与性能。</p>
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    //@ConditionalOnMissingBean(MapperFacade.class)
    public MapperFacade mapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        log.info("[Security] MapperFacade 初始化成功 | 使用方式: @Autowired MapperFacade");
        return mapperFacade;
    }
}

package io.github.whitedg.mybatis.crypto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author White
 */
@Configuration
@EnableConfigurationProperties(MybatisCryptoProperties.class)
public class MyBatisCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MybatisCryptoEncryptionInterceptor.class)
    public MybatisCryptoEncryptionInterceptor encryptionInterceptor(MybatisCryptoProperties properties) {
        return new MybatisCryptoEncryptionInterceptor(properties.toMybatisCryptoConfig());
    }

    @Bean
    @ConditionalOnMissingBean(MybatisCryptoDecryptionInterceptor.class)
    public MybatisCryptoDecryptionInterceptor decryptionInterceptor(MybatisCryptoProperties properties) {
        return new MybatisCryptoDecryptionInterceptor(properties.toMybatisCryptoConfig());
    }

}

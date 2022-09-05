package io.github.whitedg.mybatis.crypto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author White
 */
@Configuration
@ConditionalOnProperty(prefix = "mybatis-crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MybatisCryptoProperties.class)
public class MyBatisCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MybatisCryptoInitializer.class)
    @ConditionalOnProperty(prefix = "mybatis-crypto", name = "type-packages")
    public MybatisCryptoInitializer mybatisCryptoInitializer(MybatisCryptoProperties properties) {
        MybatisCryptoInitializer initializer = new MybatisCryptoInitializer();
        try {
            initializer.asyncPreLoad(properties.getTypePackages(), properties.getDefaultEncryptor());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return initializer;
    }

    @Bean
    @ConditionalOnMissingBean(MybatisEncryptionPlugin.class)
    public MybatisEncryptionPlugin encryptionInterceptor(MybatisCryptoProperties properties) {
        return new MybatisEncryptionPlugin(properties.toMybatisCryptoConfig());
    }

    @Bean
    @ConditionalOnMissingBean(MybatisDecryptionPlugin.class)
    public MybatisDecryptionPlugin decryptionInterceptor(MybatisCryptoProperties properties) {
        return new MybatisDecryptionPlugin(properties.toMybatisCryptoConfig());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mybatis-crypto", name = "encrypted-query", havingValue = "true")
    @ConditionalOnMissingBean(MybatisQueryEncryptionPlugin.class)
    public MybatisQueryEncryptionPlugin queryEncryptionInterceptor(MybatisCryptoProperties properties) {
        return new MybatisQueryEncryptionPlugin(properties.toMybatisCryptoConfig());
    }
}

package io.github.whitedg.mybatis.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author White
 */
@ConfigurationProperties(prefix = "mybatis-crypto")
public class MybatisCryptoProperties {

    private boolean enabled = true;
    private List<String> mappedKeyPrefixes;
    private boolean failFast = true;
    private String defaultKey;
    private Class<? extends IEncryptor> defaultEncryptor;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Class<? extends IEncryptor> getDefaultEncryptor() {
        return defaultEncryptor;
    }

    public void setDefaultEncryptor(Class<? extends IEncryptor> defaultEncryptor) {
        this.defaultEncryptor = defaultEncryptor;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public List<String> getMappedKeyPrefixes() {
        return mappedKeyPrefixes;
    }

    public void setMappedKeyPrefixes(List<String> mappedKeyPrefixes) {
        this.mappedKeyPrefixes = mappedKeyPrefixes;
    }

    private void validate() {
        if (defaultKey == null || defaultKey.trim().equals("")) {
            throw new IllegalStateException("mybatis.crypto.default-key missing");
        }
        if (defaultEncryptor == null) {
            throw new IllegalStateException("mybatis.crypto.default-encryptor missing");
        }
    }

    private MybatisCryptoConfig mybatisCryptoConfig;

    MybatisCryptoConfig toMybatisCryptoConfig() {
        if (mybatisCryptoConfig == null) {
            validate();
            mybatisCryptoConfig = new MybatisCryptoConfig(this.mappedKeyPrefixes, this.failFast,
                    this.defaultKey, this.defaultEncryptor);
        }
        return mybatisCryptoConfig;
    }
}

package io.github.whitedg.mybatis.crypto;

import java.util.List;

/**
 * @author White
 */
public class MybatisCryptoConfig {
    private List<String> mappedKeyPrefixes;
    private boolean failFast;
    private String defaultKey;
    private Class<? extends IEncryptor> defaultEncryptor;

    public MybatisCryptoConfig(List<String> mappedKeyPrefixes, boolean failFast, String defaultKey, Class<? extends IEncryptor> defaultEncryptor) {
        this.mappedKeyPrefixes = mappedKeyPrefixes;
        this.failFast = failFast;
        this.defaultKey = defaultKey;
        this.defaultEncryptor = defaultEncryptor;
        afterPropertiesSet();
    }

    private void afterPropertiesSet() {
        if (defaultEncryptor == null || defaultEncryptor == IEncryptor.class) {
            throw new IllegalArgumentException("defaultEncryptor must be not null or IEncryptor");
        }
    }

    public void setMappedKeyPrefixes(List<String> mappedKeyPrefixes) {
        this.mappedKeyPrefixes = mappedKeyPrefixes;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
    }

    public void setDefaultEncryptor(Class<? extends IEncryptor> defaultEncryptor) {
        this.defaultEncryptor = defaultEncryptor;
    }

    public List<String> getMappedKeyPrefixes() {
        return mappedKeyPrefixes;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public Class<? extends IEncryptor> getDefaultEncryptor() {
        return defaultEncryptor;
    }
}

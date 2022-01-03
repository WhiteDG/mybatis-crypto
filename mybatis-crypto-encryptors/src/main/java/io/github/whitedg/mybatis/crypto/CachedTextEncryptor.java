package io.github.whitedg.mybatis.crypto;

import org.jasypt.util.text.TextEncryptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author White
 */
public abstract class CachedTextEncryptor implements IEncryptor {

    private final Map<String, TextEncryptor> cache = new ConcurrentHashMap<>();

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        if (val2bEncrypted == null) {
            return null;
        }
        return get(key).encrypt(val2bEncrypted.toString());
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        if (val2bDecrypted == null) {
            return null;
        }
        return get(key).decrypt(val2bDecrypted.toString());
    }

    protected TextEncryptor get(String key) {
        return cache.computeIfAbsent(key, this::createTextEncryptor);
    }

    protected abstract TextEncryptor createTextEncryptor(String key);
}

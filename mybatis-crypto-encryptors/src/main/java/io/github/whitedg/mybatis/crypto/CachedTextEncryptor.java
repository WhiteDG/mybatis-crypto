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
    public String encrypt(Object plain, String key) throws Exception {
        if (plain == null) {
            return null;
        }
        return get(key).encrypt(plain.toString());
    }

    @Override
    public String decrypt(Object cipher, String key) throws Exception {
        if (cipher == null) {
            return null;
        }
        return get(key).decrypt(cipher.toString());
    }

    protected TextEncryptor get(String key) {
        return cache.computeIfAbsent(key, this::createTextEncryptor);
    }

    protected abstract TextEncryptor createTextEncryptor(String key);
}

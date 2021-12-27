package io.github.whitedg.demo.encryptor;

import io.github.whitedg.mybatis.crypto.IEncryptor;
import org.jasypt.util.text.AES256TextEncryptor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author White
 */
public class AES256Encryptor implements IEncryptor {

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        return get(key).encrypt(val2bEncrypted.toString());
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        return get(key).decrypt(val2bDecrypted.toString());
    }

    private final Map<String, AES256TextEncryptor> cache = new HashMap<>();

    private AES256TextEncryptor get(String key) {
        return cache.computeIfAbsent(key, k -> {
            AES256TextEncryptor encryptor = new AES256TextEncryptor();
            encryptor.setPassword(k);
            return encryptor;
        });
    }

}

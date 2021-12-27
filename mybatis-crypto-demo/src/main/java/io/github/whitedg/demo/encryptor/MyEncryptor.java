package io.github.whitedg.demo.encryptor;

import io.github.whitedg.mybatis.crypto.IEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author White
 */
public class MyEncryptor implements IEncryptor {

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        return get(key).encrypt(val2bEncrypted.toString());
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        return get(key).decrypt(val2bDecrypted.toString());
    }

    private final Map<String, BasicTextEncryptor> cache = new HashMap<>();

    private BasicTextEncryptor get(String key) {
        return cache.computeIfAbsent(key, s -> {
            BasicTextEncryptor encryptor = new BasicTextEncryptor();
            encryptor.setPassword(s);
            return encryptor;
        });
    }

}

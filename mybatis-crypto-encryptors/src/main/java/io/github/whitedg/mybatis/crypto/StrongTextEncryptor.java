package io.github.whitedg.mybatis.crypto;

import org.jasypt.util.text.TextEncryptor;

/**
 * @author White
 */
public class StrongTextEncryptor extends CachedTextEncryptor {

    @Override
    protected TextEncryptor createTextEncryptor(String key) {
        org.jasypt.util.text.StrongTextEncryptor encryptor = new org.jasypt.util.text.StrongTextEncryptor();
        encryptor.setPassword(key);
        return encryptor;
    }
}

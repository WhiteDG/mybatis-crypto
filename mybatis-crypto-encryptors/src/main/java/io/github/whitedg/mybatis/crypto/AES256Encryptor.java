package io.github.whitedg.mybatis.crypto;

import org.jasypt.util.text.TextEncryptor;

/**
 * @author White
 */
public class AES256Encryptor extends CachedTextEncryptor {

    @Override
    protected TextEncryptor createTextEncryptor(String key) {
        org.jasypt.util.text.AES256TextEncryptor encryptor = new org.jasypt.util.text.AES256TextEncryptor();
        encryptor.setPassword(key);
        return encryptor;
    }
}

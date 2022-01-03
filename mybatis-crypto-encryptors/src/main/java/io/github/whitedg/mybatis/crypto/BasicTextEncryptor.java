package io.github.whitedg.mybatis.crypto;

import org.jasypt.util.text.TextEncryptor;

/**
 * @author White
 */
public class BasicTextEncryptor extends CachedTextEncryptor {

    @Override
    protected TextEncryptor createTextEncryptor(String key) {
        org.jasypt.util.text.BasicTextEncryptor encryptor = new org.jasypt.util.text.BasicTextEncryptor();
        encryptor.setPassword(key);
        return encryptor;
    }
}

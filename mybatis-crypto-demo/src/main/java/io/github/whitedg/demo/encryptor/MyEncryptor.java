package io.github.whitedg.demo.encryptor;

import io.github.whitedg.mybatis.crypto.IEncryptor;

/**
 * @author White
 */
public class MyEncryptor implements IEncryptor {

    public static String TAG = "-SUFFIX-BY-ENCRYPTOR";

    @Override
    public String encrypt(Object plain, String key) throws Exception {
        return plain.toString() + TAG;
    }

    @Override
    public String decrypt(Object cipher, String key) throws Exception {
        String str = cipher.toString();
        return str.substring(0, str.length() - TAG.length());
    }
}

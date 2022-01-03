package io.github.whitedg.demo.encryptor;

import io.github.whitedg.mybatis.crypto.IEncryptor;

/**
 * @author White
 */
public class MyEncryptor implements IEncryptor {

    public static String TAG = "-SUFFIX-BY-ENCRYPTOR";

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        return val2bEncrypted.toString() + TAG;
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        String str = val2bDecrypted.toString();
        return str.substring(0, str.length() - TAG.length());
    }
}

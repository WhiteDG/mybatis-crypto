package io.github.whitedg.mybatis.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author White
 */
public class Base64Encryptor implements IEncryptor {

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        return Base64.getEncoder().encodeToString(val2bEncrypted.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(val2bDecrypted.toString());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

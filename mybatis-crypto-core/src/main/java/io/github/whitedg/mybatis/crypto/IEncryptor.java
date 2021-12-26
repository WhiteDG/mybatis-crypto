package io.github.whitedg.mybatis.crypto;

/**
 * @author White
 */
public interface IEncryptor {

    String encrypt(Object val2bEncrypted, String key) throws Exception;

    String decrypt(Object val2bDecrypted, String key) throws Exception;

}

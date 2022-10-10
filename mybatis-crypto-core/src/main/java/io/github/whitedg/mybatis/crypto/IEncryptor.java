package io.github.whitedg.mybatis.crypto;

/**
 * @author White
 */
public interface IEncryptor {

    String encrypt(Object plain, String key) throws Exception;

    String decrypt(Object cipher, String key) throws Exception;

}

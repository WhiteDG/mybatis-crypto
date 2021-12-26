package io.github.whitedg.mybatis.crypto;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author White
 */
class EncryptorProvider {

    private static final ConcurrentHashMap<EncryptedField, IEncryptor> CACHE = new ConcurrentHashMap<>();

    public static IEncryptor get(EncryptedField encryptedField, Class<? extends IEncryptor> globalDefaultEncryptor) {
        // globalDefaultEncryptor is always the same object
        return CACHE.computeIfAbsent(encryptedField, ignored -> {
            try {
                Class<? extends IEncryptor> specifiedEncryptor = encryptedField.encryptor();
                Class<? extends IEncryptor> encryptor = specifiedEncryptor == IEncryptor.class ? globalDefaultEncryptor : specifiedEncryptor;
                return encryptor.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

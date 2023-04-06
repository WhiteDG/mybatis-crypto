package io.github.whitedg.mybatis.crypto;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author White
 */
class EncryptorProvider {

    private static final ConcurrentHashMap<EncryptedField, IEncryptor> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends IEncryptor>, IEncryptor> ENCRYPTOR_CACHE = new ConcurrentHashMap<>();

    public static IEncryptor getOrDefault(EncryptedField encryptedField, Class<? extends IEncryptor> globalDefaultEncryptor) {
        // globalDefaultEncryptor is always the same object
        return CACHE.computeIfAbsent(encryptedField, ignored -> {
            return getOrDefault(encryptedField.encryptor(), globalDefaultEncryptor);
        });
    }

    public static IEncryptor getOrDefault(Class<? extends IEncryptor> encryptor, Class<? extends IEncryptor> globalDefaultEncryptor) {
        return ENCRYPTOR_CACHE.computeIfAbsent(encryptor, ignored -> {
            try {
                Class<? extends IEncryptor> finalEncryptor = encryptor == IEncryptor.class ? globalDefaultEncryptor : encryptor;
                return finalEncryptor.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new MybatisCryptoException(e);
            }
        });
    }
}

package io.github.whitedg.mybatis.crypto;

/**
 * @author White
 */
class EncryptedParamConfig {

    private String paramName;
    private String key;
    private Class<? extends IEncryptor> encryptor;
    private EncryptedField encryptedField;

    public EncryptedParamConfig(String paramName, String key, Class<? extends IEncryptor> encryptor) {
        this.paramName = paramName;
        this.key = key;
        this.encryptor = encryptor;
    }

    public EncryptedParamConfig(String paramName, EncryptedField encryptedField) {
        this(paramName, encryptedField.key(), encryptedField.encryptor());
        this.encryptedField = encryptedField;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Class<? extends IEncryptor> getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(Class<? extends IEncryptor> encryptor) {
        this.encryptor = encryptor;
    }

    public EncryptedField getEncryptedField() {
        return encryptedField;
    }

    public void setEncryptedField(EncryptedField encryptedField) {
        this.encryptedField = encryptedField;
    }
}

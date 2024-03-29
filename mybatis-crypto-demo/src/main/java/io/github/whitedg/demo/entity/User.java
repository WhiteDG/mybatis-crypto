package io.github.whitedg.demo.entity;

import io.github.whitedg.mybatis.crypto.Base64Encryptor;
import io.github.whitedg.mybatis.crypto.EncryptedField;
import io.github.whitedg.mybatis.crypto.StrongTextEncryptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author White
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class User extends SimpleUser {

    @EncryptedField(encryptor = Base64Encryptor.class)
    private String email;

    @EncryptedField(encryptor = StrongTextEncryptor.class, key = "SPECIFIED-KEY")
    private String password;

    @EncryptedField
    private String idCardNo;
}

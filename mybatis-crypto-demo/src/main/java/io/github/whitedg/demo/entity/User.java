package io.github.whitedg.demo.entity;

import io.github.whitedg.demo.encryptor.AES256Encryptor;
import io.github.whitedg.mybatis.crypto.EncryptedField;
import lombok.Data;

/**
 * @author White
 */
@Data
public class User {

    private Long id;

    private String name;

    @EncryptedField
    private String email;

    @EncryptedField(encryptor = AES256Encryptor.class, key = "SPECIFIED-KEY")
    private String password;

}

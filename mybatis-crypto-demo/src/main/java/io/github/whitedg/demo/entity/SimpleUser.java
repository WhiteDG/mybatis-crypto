package io.github.whitedg.demo.entity;

import io.github.whitedg.demo.encryptor.MyEncryptor;
import io.github.whitedg.mybatis.crypto.EncryptedField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author White
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SimpleUser extends BaseEntity {

    @EncryptedField(encryptor = MyEncryptor.class)
    private String name;
}

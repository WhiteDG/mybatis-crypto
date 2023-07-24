package io.github.whitedg.demo.entity;

import io.github.whitedg.mybatis.crypto.Base64Encryptor;
import io.github.whitedg.mybatis.crypto.EncryptedField;
import io.github.whitedg.mybatis.crypto.StrongTextEncryptor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author White
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserWithAssociation extends SimpleUser {

    @EncryptedField(encryptor = Base64Encryptor.class)
    private String email;

    @EncryptedField(encryptor = StrongTextEncryptor.class, key = "SPECIFIED-KEY")
    private String password;

    // association 中使用 select 语句的不需要加 @EncryptedField
    private IdCard idCardSelect;

    // association 中使用 resultMap 可以使用 @EncryptedField 标记需要加解密
    @EncryptedField
    private IdCard idCardResultMap;

    @EncryptedField
    private List<Address> addresses;

    // 无效
    @EncryptedField
    private LocalDateTime createdTime;
}

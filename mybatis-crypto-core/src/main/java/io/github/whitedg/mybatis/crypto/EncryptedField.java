package io.github.whitedg.mybatis.crypto;

import java.lang.annotation.*;

/**
 * @author White
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptedField {

    String key() default "";

    Class<? extends IEncryptor> encryptor() default IEncryptor.class;
}

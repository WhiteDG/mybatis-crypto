package io.github.whitedg.demo.entity;

import io.github.whitedg.mybatis.crypto.EncryptedField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author White
 */
@Data
@Accessors(chain = true)
public class IdCard {

    @EncryptedField
    private String cardNo;

}

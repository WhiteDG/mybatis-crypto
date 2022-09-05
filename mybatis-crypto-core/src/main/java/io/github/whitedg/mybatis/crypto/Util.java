package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.mapping.SqlCommandType;

/**
 * @author White
 */
class Util {

    public static String getKeyOrDefault(EncryptedField encryptedField, String defaultKey) {
        String key = encryptedField.key();
        if (key == null || key.equals("")) {
            return defaultKey == null ? "" : defaultKey;
        }
        return key;
    }

    public static boolean encryptionRequired(Object parameter, SqlCommandType sqlCommandType) {
        return (sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.UPDATE || sqlCommandType == SqlCommandType.SELECT)
                && decryptionRequired(parameter);
    }

    public static boolean decryptionRequired(Object parameter) {
        return !(parameter == null || parameter instanceof Double || parameter instanceof Integer
                || parameter instanceof Long || parameter instanceof Short || parameter instanceof Float
                || parameter instanceof Boolean || parameter instanceof Character
                || parameter instanceof Byte);
    }
}

package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

/**
 * @author White
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class MybatisDecryptionPlugin implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisDecryptionPlugin.class);

    private final boolean failFast;
    private final String defaultKey;
    private final Class<? extends IEncryptor> defaultEncryptor;

    public MybatisDecryptionPlugin(MybatisCryptoConfig myBatisCryptoConfig) {
        this.failFast = myBatisCryptoConfig.isFailFast();
        this.defaultKey = myBatisCryptoConfig.getDefaultKey();
        this.defaultEncryptor = myBatisCryptoConfig.getDefaultEncryptor();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result == null) {
            return null;
        }
        if (result instanceof ArrayList) {
            //noinspection rawtypes
            ArrayList resultList = (ArrayList) result;
            if (!resultList.isEmpty()) {
                Object firstItem = resultList.get(0);
                boolean needToDecrypt = Util.decryptionRequired(firstItem);
                if (needToDecrypt) {
                    Set<Field> encryptedFields = EncryptedFieldsProvider.get(firstItem.getClass());
                    for (Object item : resultList) {
                        decryptEntity(encryptedFields, item);
                    }
                }
            }
        } else {
            if (Util.decryptionRequired(result)) {
                decryptEntity(EncryptedFieldsProvider.get(result.getClass()), result);
            }
        }
        return result;
    }

    private void decryptEntity(Set<Field> encryptedFields, Object item) throws MybatisCryptoException {
        if (encryptedFields == null || encryptedFields.isEmpty()) {
            return;
        }
        for (Field field : encryptedFields) {
            EncryptedField encryptedField = field.getAnnotation(EncryptedField.class);
            if (encryptedField != null) {
                try {
                    String key = Util.getKey(encryptedField, defaultKey);
                    IEncryptor iEncryptor = EncryptorProvider.get(encryptedField, defaultEncryptor);
                    field.setAccessible(true);
                    Object originalVal = field.get(item);
                    if (originalVal != null) {
                        String decryptedVal = iEncryptor.decrypt(originalVal.toString(), key);
                        field.set(item, decryptedVal);
                    }
                } catch (Exception e) {
                    if (failFast) {
                        throw new MybatisCryptoException(e);
                    } else {
                        log.warn("decrypt filed error.", e);
                    }
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}

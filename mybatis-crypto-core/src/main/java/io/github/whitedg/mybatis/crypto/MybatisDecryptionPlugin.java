package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
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
        try {
            decryptObj(result);
        } catch (Exception e) {
            if (failFast) {
                throw new MybatisCryptoException(e);
            } else {
                log.warn("decrypt filed error.", e);
            }
        }
        return result;
    }

    private void decryptObj(Object obj) throws Exception {
        if (obj == null) {
            return;
        }
        if (obj instanceof Collection) {
            Collection<?> list = (Collection<?>) obj;
            if (list.isEmpty()) {
                return;
            }
            Object firstNonNullItem = list.stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (!Util.decryptionRequired(firstNonNullItem)) {
                return;
            }
            Set<Field> encryptedFields = EncryptedFieldsProvider.get(firstNonNullItem.getClass());
            if (encryptedFields == null || encryptedFields.isEmpty()) {
                return;
            }
            for (Object item : list) {
                decryptObj(item);
            }
        } else {
            if (Util.decryptionRequired(obj)) {
                Set<Field> encryptedFields = EncryptedFieldsProvider.get(obj.getClass());
                if (encryptedFields == null || encryptedFields.isEmpty()) {
                    return;
                }
                for (Field field : encryptedFields) {
                    decryptField(field, obj);
                }
            }
        }
    }

    private void decryptField(Field field, Object obj) throws Exception {
        EncryptedField encryptedField = field.getAnnotation(EncryptedField.class);
        if (encryptedField == null) {
            return;
        }
        Object cipher = field.get(obj);
        if (cipher == null) {
            return;
        }
        if (cipher instanceof String) {
            String key = Util.getKeyOrDefault(encryptedField, defaultKey);
            IEncryptor iEncryptor = EncryptorProvider.getOrDefault(encryptedField, defaultEncryptor);
            String plain = iEncryptor.decrypt(cipher.toString(), key);
            field.set(obj, plain);
        } else {
            decryptObj(cipher);
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

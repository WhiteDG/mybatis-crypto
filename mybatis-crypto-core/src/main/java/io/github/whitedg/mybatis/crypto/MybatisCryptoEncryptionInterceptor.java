package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author White
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MybatisCryptoEncryptionInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisCryptoEncryptionInterceptor.class);

    private final List<String> mappedKeyPrefixes;
    private final boolean failFast;
    private final String defaultKey;
    private final Class<? extends IEncryptor> defaultEncryptor;

    public MybatisCryptoEncryptionInterceptor(MybatisCryptoConfig myBatisCryptoConfig) {
        this.mappedKeyPrefixes = myBatisCryptoConfig.getMappedKeyPrefixes();
        this.failFast = myBatisCryptoConfig.isFailFast();
        this.defaultKey = myBatisCryptoConfig.getDefaultKey();
        this.defaultEncryptor = myBatisCryptoConfig.getDefaultEncryptor();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        SqlCommandType sqlCommandType = ms.getSqlCommandType();
        Object parameter = args[1];
        if (Util.encryptionRequired(parameter, sqlCommandType)) {
            if (parameter instanceof MapperMethod.ParamMap) {
                MapperMethod.ParamMap<Object> paramMap = (MapperMethod.ParamMap<Object>) parameter;
                encryptParamMap(paramMap);
            } else {
                encryptEntity(parameter);
            }
        }
        return invocation.proceed();
    }

    private void encryptEntity(Object parameter) throws MybatisCryptoException {
        processFields(EncryptedFieldsProvider.get(parameter.getClass()), parameter);
    }

    private void encryptParamMap(MapperMethod.ParamMap<Object> paramMap) throws MybatisCryptoException {
        Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || key == null) {
                continue;
            }
            for (String mappedKeyPrefix : mappedKeyPrefixes) {
                if (key.startsWith(mappedKeyPrefix)) {
                    if (value instanceof ArrayList) {
                        //noinspection rawtypes
                        ArrayList list = (ArrayList) value;
                        if (!list.isEmpty()) {
                            Object firstItem = list.get(0);
                            Class<?> itemClass = firstItem.getClass();
                            Set<Field> encryptedFields = EncryptedFieldsProvider.get(itemClass);
                            for (Object item : list) {
                                processFields(encryptedFields, item);
                            }
                        }
                    } else {
                        processFields(EncryptedFieldsProvider.get(value.getClass()), value);
                    }
                }
            }
        }
    }

    private void processFields(Set<Field> encryptedFields, Object entry) throws MybatisCryptoException {
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
                    Object originalVal = field.get(entry);
                    String encryptedVal = iEncryptor.encrypt(originalVal, key);
                    field.set(entry, encryptedVal);
                } catch (Exception e) {
                    if (failFast) {
                        throw new MybatisCryptoException(e);
                    } else {
                        log.warn("process encrypted filed error.", e);
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

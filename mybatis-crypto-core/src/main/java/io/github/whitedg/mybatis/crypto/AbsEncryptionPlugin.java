package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author White
 */
public class AbsEncryptionPlugin implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisEncryptionPlugin.class);

    private final List<String> mappedKeyPrefixes;
    private final boolean failFast;
    private final String defaultKey;
    private final Class<? extends IEncryptor> defaultEncryptor;
    private final boolean keepParameter;

    public AbsEncryptionPlugin(MybatisCryptoConfig myBatisCryptoConfig) {
        this.mappedKeyPrefixes = myBatisCryptoConfig.getMappedKeyPrefixes();
        this.failFast = myBatisCryptoConfig.isFailFast();
        this.defaultKey = myBatisCryptoConfig.getDefaultKey();
        this.defaultEncryptor = myBatisCryptoConfig.getDefaultEncryptor();
        this.keepParameter = myBatisCryptoConfig.isKeepParameter();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        if (Util.encryptionRequired(parameter, ms.getSqlCommandType())) {
            doEncrypt(parameter);
            Object result = invocation.proceed();
            if (keepParameter) {
                doDecrypt(parameter);
            }
            return result;
        } else {
            return invocation.proceed();
        }
    }

    private void doEncrypt(Object parameter) {
        handleParameter(Mode.ENCRYPT, parameter);
    }

    private void doDecrypt(Object parameter) {
        handleParameter(Mode.DECRYPT, parameter);
    }

    private void handleParameter(Mode mode, Object parameter) {
        boolean isParamMap = parameter instanceof MapperMethod.ParamMap;
        if (isParamMap) {
            //noinspection unchecked
            MapperMethod.ParamMap<Object> paramMap = (MapperMethod.ParamMap<Object>) parameter;
            encryptParamMap(mode, paramMap);
        } else {
            encryptEntity(mode, parameter);
        }
    }

    private <T> void encryptEntity(Mode mode, T parameter) throws MybatisCryptoException {
        Set<Field> encryptedFields = EncryptedFieldsProvider.get(parameter.getClass());
        if (encryptedFields == null || encryptedFields.isEmpty()) {
            return;
        }
        processFields(mode, encryptedFields, parameter);
    }

    private void encryptParamMap(Mode mode, MapperMethod.ParamMap<Object> paramMap) throws MybatisCryptoException {
        Set<Map.Entry<String, Object>> paramMapEntrySet = paramMap.entrySet();
        for (Map.Entry<String, Object> paramEntry : paramMapEntrySet) {
            String key = paramEntry.getKey();
            Object value = paramEntry.getValue();
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
                                processFields(mode, encryptedFields, item);
                            }
                        }
                    } else {
                        processFields(mode, EncryptedFieldsProvider.get(value.getClass()), value);
                    }
                }
            }
        }
    }

    private void processFields(Mode mode, Set<Field> encryptedFields, Object entry) throws MybatisCryptoException {
        if (encryptedFields == null || encryptedFields.isEmpty()) {
            return;
        }
        for (Field field : encryptedFields) {
            try {
                EncryptedField encryptedField = field.getAnnotation(EncryptedField.class);
                if (encryptedField == null) {
                    continue;
                }
                Object originalVal = field.get(entry);
                if (originalVal == null) {
                    continue;
                }
                if (originalVal instanceof String && ((String) originalVal).isEmpty()) {
                    continue;
                }
                String key = Util.getKeyOrDefault(encryptedField, defaultKey);
                IEncryptor iEncryptor = EncryptorProvider.getOrDefault(encryptedField, defaultEncryptor);
                String updatedVal = Mode.ENCRYPT.equals(mode) ? iEncryptor.encrypt(originalVal, key) : iEncryptor.decrypt(originalVal, key);
                field.set(entry, updatedVal);
            } catch (Exception e) {
                if (failFast) {
                    throw new MybatisCryptoException(e);
                } else {
                    log.warn("process encrypted filed error.", e);
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

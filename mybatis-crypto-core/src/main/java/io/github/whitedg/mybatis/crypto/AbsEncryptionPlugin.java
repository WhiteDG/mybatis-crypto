package io.github.whitedg.mybatis.crypto;

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
class AbsEncryptionPlugin implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisEncryptionPlugin.class);

    private final List<String> mappedKeyPrefixes;
    private final boolean failFast;
    private final String defaultKey;
    private final Class<? extends IEncryptor> defaultEncryptor;
    private final boolean keepParameter;

    public AbsEncryptionPlugin(MybatisCryptoConfig myBatisCryptoConfig) {
        this.mappedKeyPrefixes = Collections.unmodifiableList(myBatisCryptoConfig.getMappedKeyPrefixes());
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
            doEncrypt(parameter, ms);
            try {
                return invocation.proceed();
            } finally {
                if (keepParameter) {
                    doDecrypt(parameter, ms);
                }
            }
        } else {
            return invocation.proceed();
        }
    }

    private void doEncrypt(Object parameter, MappedStatement mappedStatement) {
        processParameter(Mode.ENCRYPT, parameter, mappedStatement);
    }

    private void doDecrypt(Object parameter, MappedStatement mappedStatement) {
        processParameter(Mode.DECRYPT, parameter, mappedStatement);
    }

    private void processParameter(Mode mode, Object parameter, MappedStatement mappedStatement) {
        boolean isParamMap = parameter instanceof HashMap;
        if (isParamMap) {
            //noinspection unchecked
            HashMap<String, Object> paramMap = (HashMap<String, Object>) parameter;
            processParamMap(mode, paramMap, mappedStatement);
        } else {
            processEntity(mode, parameter);
        }
    }

    private <T> void processEntity(Mode mode, T parameter) throws MybatisCryptoException {
        Set<Field> encryptedFields = EncryptedFieldsProvider.get(parameter.getClass());
        if (encryptedFields == null || encryptedFields.isEmpty()) {
            return;
        }
        processFields(mode, encryptedFields, parameter);
    }

    private void processParamMap(Mode mode, HashMap<String, Object> paramMap, MappedStatement mappedStatement) throws MybatisCryptoException {
        Map<String, EncryptedParamConfig> encryptedParamConfigs = EncryptedParamsProvider.get(mappedStatement.getId(), mappedKeyPrefixes);
        if (encryptedParamConfigs == null || encryptedParamConfigs.isEmpty()) {
            return;
        }
        for (Map.Entry<String, EncryptedParamConfig> entry : encryptedParamConfigs.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = paramMap.get(paramName);
            if (paramValue == null) {
                continue;
            }
            EncryptedParamConfig encryptedParamConfig = entry.getValue();
            if (paramValue instanceof Collection) {
                Collection<?> list = (Collection<?>) paramValue;
                if (list.isEmpty()) {
                    continue;
                }
                Object nonNullItem = list.stream().filter(Objects::nonNull).findFirst().orElse(null);
                if (nonNullItem == null) {
                    continue;
                }
                if (nonNullItem instanceof String) {
                    //noinspection rawtypes
                    Collection newList = new ArrayList();
                    for (Object item : list) {
                        newList.add(processString(mode, item, encryptedParamConfig));
                    }
                    // Replace plain text with ciphertext
                    list.clear();
                    list.addAll(newList);
                } else {
                    Class<?> itemClass = nonNullItem.getClass();
                    Set<Field> encryptedFields = EncryptedFieldsProvider.get(itemClass);
                    if (encryptedFields != null && !encryptedFields.isEmpty()) {
                        for (Object item : list) {
                            processFields(mode, encryptedFields, item);
                        }
                    }
                }
            } else if (paramValue instanceof String) {
                paramMap.put(paramName, processString(mode, paramValue, encryptedParamConfig));
            } else {
                processFields(mode, EncryptedFieldsProvider.get(paramValue.getClass()), paramValue);
            }
        }
    }

    private Object processString(Mode mode, Object originalValue, EncryptedParamConfig encryptedParamConfig) {
        IEncryptor iEncryptor = EncryptorProvider.getOrDefault(encryptedParamConfig.getEncryptor(), defaultEncryptor);
        String key = encryptedParamConfig.getKey() == null || encryptedParamConfig.getKey().equals("") ? defaultKey : encryptedParamConfig.getKey();
        try {
            return Util.doFinal(iEncryptor, mode, originalValue, key);
        } catch (Exception e) {
            if (failFast) {
                throw new MybatisCryptoException(e);
            } else {
                log.warn("process encrypted parameter error.", e);
                return originalValue;
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
                if (!String.class.equals(field.getType())) {
                    continue;
                }
                Object originalVal = field.get(entry);
                if (originalVal == null) {
                    continue;
                }
                if (((String) originalVal).isEmpty()) {
                    continue;
                }
                String key = Util.getKeyOrDefault(encryptedField, defaultKey);
                IEncryptor iEncryptor = EncryptorProvider.getOrDefault(encryptedField, defaultEncryptor);
                String updatedVal = Util.doFinal(iEncryptor, mode, originalVal, key);
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

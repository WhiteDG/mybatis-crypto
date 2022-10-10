package io.github.whitedg.mybatis.crypto;

import com.esotericsoftware.kryo.kryo5.Kryo;
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
            Kryo kryo = null;
            try {
                Object execParam = parameter;
                if (keepParameter) {
                    kryo = KryoPool.obtain();
                    execParam = kryo.copy(parameter);
                }
                boolean isParamMap = parameter instanceof MapperMethod.ParamMap;
                if (isParamMap) {
                    //noinspection unchecked
                    MapperMethod.ParamMap<Object> paramMap = (MapperMethod.ParamMap<Object>) execParam;
                    encryptParamMap(paramMap);
                } else {
                    encryptEntity(execParam);
                }
                args[1] = execParam;
                Object result = invocation.proceed();
                postExecution(ms, parameter, execParam, isParamMap);
                return result;
            } finally {
                KryoPool.free(kryo);
            }
        } else {
            return invocation.proceed();
        }
    }

    private void postExecution(MappedStatement ms, Object parameter, Object execParam, boolean isParamMap) throws IllegalAccessException {
        if (!keepParameter) {
            return;
        }
        if (!isParamMap) {
            handleKeyProperties(ms, parameter, execParam);
        } else {
            //noinspection unchecked
            MapperMethod.ParamMap<Object> plainParamMap = (MapperMethod.ParamMap<Object>) parameter;
            //noinspection unchecked
            MapperMethod.ParamMap<Object> cipherParamMap = (MapperMethod.ParamMap<Object>) execParam;
            for (Map.Entry<String, Object> plainEntry : plainParamMap.entrySet()) {
                String key = plainEntry.getKey();
                for (String keyPrefix : mappedKeyPrefixes) {
                    if (key != null && key.startsWith(keyPrefix)) {
                        Object plainVal = plainEntry.getValue();
                        Object cipherVal = cipherParamMap.get(key);
                        if (plainVal instanceof ArrayList) {
                            //noinspection rawtypes
                            ArrayList plainList = (ArrayList) plainVal;
                            //noinspection rawtypes
                            ArrayList cipherList = (ArrayList) cipherVal;
                            for (int i = 0; i < plainList.size(); i++) {
                                handleKeyProperties(ms, plainList.get(i), cipherList.get(i));
                            }
                        } else {
                            handleKeyProperties(ms, plainVal, cipherVal);
                        }
                    }
                }
            }
        }
    }

    private void handleKeyProperties(MappedStatement ms, Object parameter, Object copyOfParameter) throws IllegalAccessException {
        List<Field> keyFields = KeyFieldsProvider.get(ms, copyOfParameter);
        for (Field keyField : keyFields) {
            keyField.set(parameter, keyField.get(copyOfParameter));
        }
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

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}

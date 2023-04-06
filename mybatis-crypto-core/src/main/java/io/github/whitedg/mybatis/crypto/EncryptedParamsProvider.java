package io.github.whitedg.mybatis.crypto;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author White
 */
public class EncryptedParamsProvider {

    private static final Map<String, Map<String, EncryptedParamConfig>> encryptedParamCache = new ConcurrentHashMap<>();

    public static Map<String, EncryptedParamConfig> get(String mappedStatementId, List<String> mappedKeyPrefixes) {
        return encryptedParamCache.computeIfAbsent(mappedStatementId + "_" + mappedKeyPrefixes.hashCode(),
                ignored -> findParamsToEncrypt(mappedStatementId, mappedKeyPrefixes));
    }

    private static Map<String, EncryptedParamConfig> findParamsToEncrypt(String id, List<String> mappedKeyPrefixes) {
        try {
            // 注解解析
            Parameter[] parameters = Util.getParametersByMappedStatementId(id);
            if (parameters == null || parameters.length == 0) {
                return Collections.emptyMap();
            }
            Map<String, EncryptedParamConfig> shouldEncryptParams = new HashMap<>();
            for (int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
                Parameter parameter = parameters[paramIndex];
                String paramName = Util.getParamName(parameter);
                EncryptedField encryptedField = parameter.getAnnotation(EncryptedField.class);
                if (encryptedField != null) {
                    addParamToEncrypt(shouldEncryptParams, paramIndex, parameter.getType(), paramName, encryptedField);
                } else {
                    addParamIfMatchedMappedKeyPrefix(mappedKeyPrefixes, shouldEncryptParams, paramIndex, parameter.getType(), paramName);
                }
            }
            return shouldEncryptParams;
        } catch (Exception e) {
            throw new MybatisCryptoException(e);
        }
    }

    private static void addParamIfMatchedMappedKeyPrefix(List<String> mappedKeyPrefixes, Map<String, EncryptedParamConfig> shouldEncryptParams, int paramIndex, Class<?> parameterType, String paramName) {
        for (String mappedKeyPrefix : mappedKeyPrefixes) {
            if (paramName.startsWith(mappedKeyPrefix)) {
                addParamToEncrypt(shouldEncryptParams, paramIndex, parameterType, paramName, null);
            }
        }
    }

    private static void addParamToEncrypt(Map<String, EncryptedParamConfig> shouldEncryptParams, int paramIndex, Class<?> parameterType, String paramName, EncryptedField encryptedField) {
        EncryptedParamConfig encryptedParamConfig;
        if (encryptedField != null) {
            encryptedParamConfig = new EncryptedParamConfig(paramName, encryptedField);
        } else {
            encryptedParamConfig = new EncryptedParamConfig(paramName, null, IEncryptor.class);
        }
        shouldEncryptParams.put(paramName, encryptedParamConfig);
        if (parameterType.equals(String.class)) {
            EncryptedParamConfig encryptedParamConfig0;
            String paramIndexKey = "param" + (paramIndex + 1);
            if (encryptedField != null) {
                encryptedParamConfig0 = new EncryptedParamConfig(paramIndexKey, encryptedField);
            } else {
                encryptedParamConfig0 = new EncryptedParamConfig(paramIndexKey, null, IEncryptor.class);
            }
            shouldEncryptParams.put(paramIndexKey, encryptedParamConfig0);
        }
    }
}

package io.github.whitedg.mybatis.crypto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author White
 */
class EncryptedFieldsProvider {

    private static final Map<Class<?>, Set<Field>> encryptedFieldCache = new ConcurrentHashMap<>();

    public static Set<Field> get(Class<?> parameterClass) {
        return encryptedFieldCache.computeIfAbsent(parameterClass, aClass -> {
            Field[] declaredFields = aClass.getDeclaredFields();
            return Arrays.stream(declaredFields).filter(field ->
                            field.isAnnotationPresent(EncryptedField.class) && field.getType() == String.class)
                    .collect(Collectors.toSet());
        });
    }
}

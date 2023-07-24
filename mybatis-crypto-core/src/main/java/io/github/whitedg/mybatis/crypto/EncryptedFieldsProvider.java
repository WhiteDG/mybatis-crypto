package io.github.whitedg.mybatis.crypto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
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
            Field[] declaredFields = ReflectUtil.getFields(aClass);
            if (declaredFields == null || declaredFields.length == 0) {
                return Collections.emptySet();
            }
            Set<Field> fieldSet = Arrays.stream(declaredFields)
                    .filter(field -> field.isAnnotationPresent(EncryptedField.class))
                    .collect(Collectors.toSet());
            if (fieldSet.isEmpty()) {
                return Collections.emptySet();
            }
            for (Field field : fieldSet) {
                field.setAccessible(true);
            }
            return fieldSet;
        });
    }
}

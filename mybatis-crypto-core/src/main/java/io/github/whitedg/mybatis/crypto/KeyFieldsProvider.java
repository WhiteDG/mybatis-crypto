package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author White
 */
class KeyFieldsProvider {
    private static final ConcurrentHashMap<String, List<Field>> CACHE = new ConcurrentHashMap<>();

    public static List<Field> get(MappedStatement mappedStatement, Object obj) {
        String id = mappedStatement.getId();
        return CACHE.computeIfAbsent(id, msId -> {
            String[] keyProperties = mappedStatement.getKeyProperties();
            if (keyProperties == null || keyProperties.length == 0) {
                return Collections.emptyList();
            }
            List<Field> fields = new ArrayList<>(keyProperties.length);
            try {
                Map<String, Field> allFields = getAllFields(obj);
                for (String keyProperty : keyProperties) {
                    Field field = allFields.get(keyProperty);
                    if (field == null) {
                        throw new NoSuchFieldException(keyProperty);
                    }
                    field.setAccessible(true);
                    fields.add(field);
                }
            } catch (NoSuchFieldException e) {
                throw new MybatisCryptoException(e);
            }
            return fields;
        });
    }

    private static Map<String, Field> getAllFields(Object obj) {
        Class<?> searchType = obj.getClass();
        Map<String, Field> allFields = new HashMap<>();
        while (searchType != null) {
            Field[] declaredFields = searchType.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                allFields.put(declaredField.getName(), declaredField);
            }
            searchType = searchType.getSuperclass();
        }
        return allFields;
    }
}

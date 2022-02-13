package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                for (String keyProperty : keyProperties) {
                    Field field = obj.getClass().getDeclaredField(keyProperty);
                    field.setAccessible(true);
                    fields.add(field);
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            return fields;
        });
    }
}

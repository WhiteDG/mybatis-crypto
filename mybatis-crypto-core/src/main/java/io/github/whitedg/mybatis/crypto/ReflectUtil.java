package io.github.whitedg.mybatis.crypto;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author White
 */
class ReflectUtil {

    private static final Map<Class<?>, Field[]> FIELDS_CACHE = new ConcurrentHashMap<>();

    public static Field[] getFields(Class<?> beanClass) throws SecurityException {
        Field[] allFields = FIELDS_CACHE.get(beanClass);
        if (null != allFields) {
            return allFields;
        }

        allFields = getFieldsDirectly(beanClass, true);
        FIELDS_CACHE.put(beanClass, allFields);
        return allFields;
    }

    public static Field[] getFieldsDirectly(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        Field[] allFields = null;
        Class<?> searchType = beanClass;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (null == allFields) {
                allFields = declaredFields;
            } else {
                Field[] newAllFields = new Field[allFields.length + declaredFields.length];
                System.arraycopy(allFields, 0, newAllFields, 0, allFields.length);
                System.arraycopy(declaredFields, 0, newAllFields, allFields.length, declaredFields.length);
                allFields = newAllFields;
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }

        return allFields;
    }
}

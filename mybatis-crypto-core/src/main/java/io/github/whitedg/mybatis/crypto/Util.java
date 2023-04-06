package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author White
 */
class Util {

    private static final List<String> suffixList = new ArrayList<>(2);

    static {
        suffixList.add("_mpCount");
        suffixList.add("_COUNT");
    }

    public static String getKeyOrDefault(EncryptedField encryptedField, String defaultKey) {
        String key = encryptedField.key();
        if (key == null || key.equals("")) {
            return defaultKey == null ? "" : defaultKey;
        }
        return key;
    }

    public static boolean encryptionRequired(Object parameter, SqlCommandType sqlCommandType) {
        return (sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.UPDATE || sqlCommandType == SqlCommandType.SELECT)
                && decryptionRequired(parameter);
    }

    public static boolean decryptionRequired(Object parameter) {
        return !(parameter == null || parameter instanceof Double || parameter instanceof Integer
                || parameter instanceof Long || parameter instanceof Short || parameter instanceof Float
                || parameter instanceof Boolean || parameter instanceof Character
                || parameter instanceof Byte);
    }

    public static String getParamName(Parameter parameter) {
        Param paramAnnotation = parameter.getAnnotation(Param.class);
        return paramAnnotation != null ? paramAnnotation.value() : parameter.getName();
    }

    public static Parameter[] getParametersByMappedStatementId(String msId) throws ClassNotFoundException {
        String className = msId.substring(0, msId.lastIndexOf("."));
        String methodName = msId.substring(msId.lastIndexOf(".") + 1);
        Method method = findMethod(className, methodName);
        if (method == null) {
            return null;
        }
        return method.getParameters();
    }

    public static Method findMethod(String className, String methodName) throws ClassNotFoundException {
        String trueMethodName = suffixList.stream().filter(methodName::endsWith).findFirst()
                .map(suffix -> methodName.substring(0, methodName.length() - suffix.length()))
                .orElse(methodName);
        Method[] methods = Class.forName(className).getMethods();
        if (methods.length == 0) {
            return null;
        }
        return Arrays.stream(methods)
                .filter(method -> method.getName().equals(trueMethodName))
                .findFirst()
                .orElse(null);
    }

    public static String doFinal(IEncryptor encryptor, Mode mode, Object originalValue, String key) throws Exception {
        return Mode.DECRYPT.equals(mode) ? encryptor.decrypt(originalValue, key) : encryptor.encrypt(originalValue, key);
    }
}

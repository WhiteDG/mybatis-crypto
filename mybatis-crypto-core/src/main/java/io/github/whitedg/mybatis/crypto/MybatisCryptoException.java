package io.github.whitedg.mybatis.crypto;

/**
 * @author White
 */
public class MybatisCryptoException extends RuntimeException {

    public MybatisCryptoException() {
    }

    public MybatisCryptoException(String message) {
        super(message);
    }

    public MybatisCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public MybatisCryptoException(Throwable cause) {
        super(cause);
    }

    public MybatisCryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

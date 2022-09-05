package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;

/**
 * @author White
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MybatisEncryptionPlugin extends AbsEncryptionPlugin {

    public MybatisEncryptionPlugin(MybatisCryptoConfig myBatisCryptoConfig) {
        super(myBatisCryptoConfig);
    }
}

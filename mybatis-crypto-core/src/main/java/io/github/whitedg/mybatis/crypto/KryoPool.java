package io.github.whitedg.mybatis.crypto;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.Pool;

/**
 * @author White
 */
class KryoPool {
    private static final Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, Integer.MAX_VALUE) {
        protected Kryo create() {
            return newKryo();
        }
    };

    static Kryo newKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        DefaultInstantiatorStrategy initStrategy = new DefaultInstantiatorStrategy();
        initStrategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setInstantiatorStrategy(initStrategy);
        return kryo;
    }

    public static Kryo obtain() {
        return kryoPool.obtain();
    }

    public static void free(Kryo kryo) {
        if (kryo != null) {
            kryoPool.free(kryo);
        }
    }
}

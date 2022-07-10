package io.github.whitedg.mybatis.crypto;

import org.apache.ibatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * @author White
 */
class MybatisCryptoInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisCryptoInitializer.class);

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

    void asyncPreLoad(String packagePatterns, Class<? extends IEncryptor> defaultEncryptor) throws IOException {
        new PreLoadThread(packagePatterns, defaultEncryptor).start();
    }

    private static class PreLoadThread extends Thread {

        private final String packagePatterns;
        private final Class<? extends IEncryptor> defaultEncryptor;

        public PreLoadThread(String packagePatterns, Class<? extends IEncryptor> defaultEncryptor) {
            this.packagePatterns = packagePatterns;
            this.defaultEncryptor = defaultEncryptor;
            this.setName("MybatisCryptoPreLoadThread");
            this.setDaemon(true);
        }

        @Override
        public void run() {
            LOGGER.info("Pre-Loading EncryptedFields by Mybatis-Crypto...");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Set<Class<?>> classes;
            try {
                classes = scanClasses(packagePatterns);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            classes.stream().filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isInterface())
                    .filter(clazz -> !clazz.isMemberClass())
                    .forEach(clazz -> {
                        Set<Field> encryptedFields = EncryptedFieldsProvider.get(clazz);
                        if (!CollectionUtils.isEmpty(encryptedFields)) {
                            LOGGER.debug("{} encrypted field(s) found for {}", encryptedFields.size(), clazz);
                            for (Field field : encryptedFields) {
                                EncryptedField encryptedField = field.getAnnotation(EncryptedField.class);
                                if (encryptedField != null) {
                                    EncryptorProvider.getOrDefault(encryptedField, defaultEncryptor);
                                }
                            }
                        }
                    });
            stopWatch.stop();
            LOGGER.info("Pre-Loading EncryptedFields by Mybatis-Crypto completed in {}ms", stopWatch.getTotalTimeMillis());
        }
    }

    private static Set<Class<?>> scanClasses(String packagePatterns) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String[] packagePatternArray = tokenizeToStringArray(packagePatterns,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        for (String packagePattern : packagePatternArray) {
            Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class");
            for (Resource resource : resources) {
                try {
                    ClassMetadata classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    classes.add(clazz);
                } catch (Throwable e) {
                    LOGGER.warn("Cannot load the '" + resource + "'. Cause by " + e.toString());
                }
            }
        }
        return classes;
    }
}

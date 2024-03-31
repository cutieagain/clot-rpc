package cn.cutie.clotrpc.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class ScanPackagesUtils {


    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    @NotNull
    public static List<Class<?>> scanPackages(@NotNull String[] packages, Predicate<Class<?>> predicate){
        List<Class<?>> results = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        for (String basePackage : packages) {
            if (StringUtils.isBlank(basePackage)) {
                continue;
            }
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/" + DEFAULT_RESOURCE_PATTERN;
            log.info("packageSearchPath="+packageSearchPath);
            try {
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    //log.info(" resource: " + resource.getFilename());
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    String className = classMetadata.getClassName();
                    Class<?> clazz = Class.forName(className);
                    if(predicate.test(clazz)) {
                        //log.info(" ===> class: " + className);
                        results.add(clazz);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public static void main(String[] args) {
        String packages = "cn.cutie.clotrpc";

        log.info(" 1. *********** ");
        log.info(" => scan all classes for packages: " + packages);
        List<Class<?>> classes = scanPackages(packages.split(","), p -> true);
        classes.forEach(System.out::println);

        log.info("");
        log.info(" 2. *********** ");
        log.info(" => scan all classes with @Configuration for packages: " + packages);
        List<Class<?>> classesWithConfig = scanPackages(packages.split(","),
                p -> Arrays.stream(p.getAnnotations())
                        .anyMatch(a -> a.annotationType().equals(Configuration.class)));
        classesWithConfig.forEach(System.out::println);
    }

}
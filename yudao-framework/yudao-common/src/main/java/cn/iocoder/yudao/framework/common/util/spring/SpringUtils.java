package cn.iocoder.yudao.framework.common.util.spring;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.aop.support.AopUtils;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Spring 工具类
 *
 * @author 芋道源码
 */
public class SpringUtils extends SpringUtil {

    /**
     * 是否为生产环境
     *
     * @return 是否生产环境
     */
    public static boolean isProd() {
        String activeProfile = getActiveProfile();
        return Objects.equals("prod", activeProfile);
    }

    /**
     * Get a bean by exact type (ignoring subclasses).
     *
     * @param clazz the class of the bean to retrieve
     * @param <T>   the type of the bean
     * @return the bean instance of the exact type
     * @throws IllegalStateException if no bean or multiple beans of the exact type are found
     */
    public static <T> T getBeanByExactType(Class<T> clazz) {
        // Get all beans of the type (including subclasses)
        Map<String, T> beansOfType = getApplicationContext().getBeansOfType(clazz);
        // Filter beans to retain only exact matches
        Map<String, T> exactTypeBeans = beansOfType.entrySet().stream()
                .filter(entry -> AopUtils.getTargetClass(entry.getValue()).equals(clazz))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        if (exactTypeBeans.isEmpty()) {
            throw new IllegalStateException("No bean found of exact type: " + clazz.getName());
        }
        if (exactTypeBeans.size() > 1) {
            throw new IllegalStateException("Multiple beans found of exact type: " + clazz.getName());
        }

        // Return the single bean
        return exactTypeBeans.values().iterator().next();
    }

}

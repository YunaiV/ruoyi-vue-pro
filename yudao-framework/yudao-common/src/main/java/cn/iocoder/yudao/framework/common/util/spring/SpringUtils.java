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
     * 根据精确类型获取 Bean（忽略子类）。
     *
     * @param clazz 要检索的 Bean 的类
     * @param <T>   Bean 的类型
     * @return 精确类型的 Bean 实例
     * @throws IllegalStateException 如果未找到或找到多个精确类型的 Bean
     */
    public static <T> T getBeanByExactType(Class<T> clazz) {
        // 获取所有类型为 clazz 的 Bean（包括子类）
        Map<String, T> beansOfType = getApplicationContext().getBeansOfType(clazz);
        // 过滤 Bean 以保留仅精确匹配的 Bean
        Map<String, T> exactTypeBeans = beansOfType.entrySet().stream()
                .filter(entry -> AopUtils.getTargetClass(entry.getValue()).equals(clazz))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (exactTypeBeans.isEmpty()) {
            throw new IllegalStateException("未找到精确类型为 " + clazz.getName() + " 的 Bean");
        }
        if (exactTypeBeans.size() > 1) {
            throw new IllegalStateException("找到多个精确类型为 " + clazz.getName() + " 的 Bean");
        }
        // 返回单个 Bean
        return exactTypeBeans.values().iterator().next();
    }


}

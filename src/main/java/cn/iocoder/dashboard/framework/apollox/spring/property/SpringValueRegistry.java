package cn.iocoder.dashboard.framework.apollox.spring.property;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;

/**
 * {@link SpringValue} 注册表
 */
public class SpringValueRegistry {

    /**
     * SpringValue 集合
     *
     * KEY：属性 KEY ，即 Config 配置 KEY
     * VALUE：SpringValue 数组
     */
    private final Multimap<String, SpringValue> registry = LinkedListMultimap.create();

    // 注册
    public void register(String key, SpringValue springValue) {
        registry.put(key, springValue);
    }

    // 获得
    public Collection<SpringValue> get(String key) {
        return registry.get(key);
    }

}

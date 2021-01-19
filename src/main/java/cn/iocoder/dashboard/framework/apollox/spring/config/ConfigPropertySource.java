package cn.iocoder.dashboard.framework.apollox.spring.config;

import cn.iocoder.dashboard.framework.apollox.Config;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Set;

/**
 * Property source wrapper for Config
 *
 * 基于 {@link Config} 的 PropertySource 实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySource extends EnumerablePropertySource<Config> {

    private static final String[] EMPTY_ARRAY = new String[0];

    public ConfigPropertySource(String name, Config source) { // 此处的 Apollo Config 作为 `source`
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        // 从 Config 中，获得属性名集合
        Set<String> propertyNames = this.source.getPropertyNames();
        // 转换成 String 数组，返回
        if (propertyNames.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return propertyNames.toArray(new String[0]);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.getProperty(name, null);
    }

}

package cn.iocoder.dashboard.framework.apollox;

import java.util.Collections;
import java.util.Set;

public class DefaultConfig implements Config {

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.emptySet(); // TODO 等下实现
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {

    }

}

package cn.iocoder.dashboard.framework.apollox;

import cn.hutool.core.lang.Singleton;

public class ConfigService {

    public static Config getConfig(String namespace) {
        return Singleton.get(DefaultConfig.class);
    }

}

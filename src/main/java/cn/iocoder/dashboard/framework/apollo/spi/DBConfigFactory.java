package cn.iocoder.dashboard.framework.apollo.spi;

import cn.iocoder.dashboard.framework.apollo.internals.DBConfigRepository;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.spi.ConfigFactory;

public class DBConfigFactory implements ConfigFactory {

    @Override
    public Config create(String namespace) {
        return new DefaultConfig(namespace, this.createDBConfigRepository(namespace));
    }

    @Override
    public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        throw new UnsupportedOperationException("暂不支持 Apollo 配置文件");
    }

    private ConfigRepository createDBConfigRepository(String namespace) {
        return new DBConfigRepository(namespace); // TODO 芋艿：看看怎么优化
    }

}

package cn.iocoder.yudao.framework.apollo.spi;

import cn.iocoder.yudao.framework.apollo.internals.DBConfigRepository;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.spi.ConfigFactory;

/**
 * 基于 DB 的 ConfigFactory 实现类
 *
 * @author 芋道源码
 */
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
        return new DBConfigRepository(namespace);
    }

}

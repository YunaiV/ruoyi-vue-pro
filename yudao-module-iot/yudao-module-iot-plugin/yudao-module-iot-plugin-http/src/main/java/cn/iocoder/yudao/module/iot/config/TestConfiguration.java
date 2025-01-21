package cn.iocoder.yudao.module.iot.config;

import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.device.dto.DeviceDataCreateReqDTO;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO 芋艿：临时实现；
@Configuration
public class TestConfiguration {

    @Bean
    public DeviceDataApi deviceDataApi() {
        return new DeviceDataApi() {

            @Override
            public void saveDeviceData(DeviceDataCreateReqDTO createDTO) {
                System.out.println("saveDeviceData");
            }

        };
    }

    // TODO @haohao：可能要看下，有没更好的方式
    @Bean(initMethod = "start")
    public HttpVertxPlugin HttpVertxPlugin() {
        PluginWrapper pluginWrapper = new PluginWrapper(new DefaultPluginManager(), null, null, null);
        return new HttpVertxPlugin(pluginWrapper);
    }

}

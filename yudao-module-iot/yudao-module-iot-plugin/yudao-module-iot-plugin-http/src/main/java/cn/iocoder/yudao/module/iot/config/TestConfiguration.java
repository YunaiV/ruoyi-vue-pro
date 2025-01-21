package cn.iocoder.yudao.module.iot.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO 芋艿：临时实现；
@Configuration
public class TestConfiguration {

//    @Resource
//    private RestTemplate restTemplate;

    // TODO 芋艿：这里，后续看看怎么创建好点
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public DeviceDataApi deviceDataApi(RestTemplate restTemplate) {
        return new DeviceDataApi() {

            @Override
            public CommonResult<Boolean> updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
                // TODO haohao：待实现
                return null;
            }

            @Override
            public CommonResult<Boolean> reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
                // TODO haohao：待实现
                return null;
            }

            @Override
            public CommonResult<Boolean> reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
                // TODO haohao：待完整实现
                String url = "http://127.0.0.1:48080/rpc-api/iot/device-data/report-property";
                try {
                    restTemplate.postForObject(url, reportReqDTO, CommonResult.class);
                    return success(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    return CommonResult.error(400, "error");
                }
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

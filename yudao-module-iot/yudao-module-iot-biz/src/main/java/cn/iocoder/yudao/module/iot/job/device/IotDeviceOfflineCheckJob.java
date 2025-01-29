package cn.iocoder.yudao.module.iot.job.device;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import org.springframework.stereotype.Component;

// TODO @芋艿：待实现
/**
 * IoT 设备离线检查 Job
 *
 * 检测逻辑：设备最后一条 {@link cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage} 消息超过一定时间，则认为设备离线
 *
 * @author 芋道源码
 */
@Component
public class IotDeviceOfflineCheckJob implements JobHandler {

    @Override
    @TenantJob
    public String execute(String param) {
        return "";
    }

}

package cn.iocoder.yudao.module.iot.job.plugin;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.service.plugin.PluginInstanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 插件实例 Job
 *
 * @author 芋道源码
 */
@Component
public class PluginInstancesJob {

    @Resource
    private PluginInstanceService pluginInstanceService;

    @Scheduled(initialDelay = 60, fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void updatePluginInstances() {
        TenantUtils.executeIgnore(() -> {
            pluginInstanceService.updatePluginInstances();
        });
    }

}
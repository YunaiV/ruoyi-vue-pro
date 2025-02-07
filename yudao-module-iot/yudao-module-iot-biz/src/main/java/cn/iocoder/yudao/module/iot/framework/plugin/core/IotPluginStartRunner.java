package cn.iocoder.yudao.module.iot.framework.plugin.core;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginConfigDO;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginDeployTypeEnum;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import cn.iocoder.yudao.module.iot.service.plugin.IotPluginConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

/**
 * IoT 插件启动 Runner
 *
 * 用于 Spring Boot 启动时，启动 {@link IotPluginDeployTypeEnum#JAR} 部署类型的插件
 */
@RequiredArgsConstructor
@Slf4j
public class IotPluginStartRunner implements ApplicationRunner {

    private final SpringPluginManager springPluginManager;

    private final IotPluginConfigService pluginConfigService;

    @Override
    public void run(ApplicationArguments args) {
        List<IotPluginConfigDO> pluginConfigList = TenantUtils.executeIgnore(
                () -> pluginConfigService.getPluginConfigListByStatusAndDeployType(
                        IotPluginStatusEnum.RUNNING.getStatus(), IotPluginDeployTypeEnum.JAR.getDeployType()));
        if (CollUtil.isEmpty(pluginConfigList)) {
            log.info("[run][没有需要启动的插件]");
            return;
        }

        // 遍历插件列表，逐个启动
        pluginConfigList.forEach(pluginConfig -> {
            try {
                log.info("[run][插件({}) 启动开始]", pluginConfig.getPluginKey());
                springPluginManager.startPlugin(pluginConfig.getPluginKey());
                log.info("[run][插件({}) 启动完成]", pluginConfig.getPluginKey());
            } catch (Exception e) {
                log.error("[run][插件({}) 启动异常]", pluginConfig.getPluginKey(), e);
            }
        });
    }

}
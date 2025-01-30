package cn.iocoder.yudao.module.iot.framework.plugin.core;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.PluginInfoDO;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import cn.iocoder.yudao.module.iot.service.plugin.PluginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

// TODO @芋艿：需要 review 下
@Component
@Slf4j
public class PluginStart implements ApplicationRunner {

    @Resource
    private PluginInfoService pluginInfoService;

    @Resource
    private SpringPluginManager pluginManager;

    @Override
    public void run(ApplicationArguments args) {
        TenantUtils.executeIgnore(() -> { // 1. 忽略租户上下文执行
            List<PluginInfoDO> pluginInfoList = pluginInfoService
                    .getPluginInfoListByStatus(IotPluginStatusEnum.RUNNING.getStatus()); // 2. 获取运行中的插件列表
            if (CollUtil.isEmpty(pluginInfoList)) { // 3. 检查插件列表是否为空
                log.info("[run] 没有需要启动的插件"); // 4. 日志记录没有插件需要启动
                return;
            }
            pluginInfoList.forEach(pluginInfo -> { // 5. 使用lambda表达式遍历插件列表
                try {
                    log.info("[run][启动插件] pluginKey = {}", pluginInfo.getPluginKey()); // 6. 日志记录插件启动信息
                    pluginManager.startPlugin(pluginInfo.getPluginKey()); // 7. 启动插件
                } catch (Exception e) {
                    log.error("[run][启动插件失败] pluginKey = {}", pluginInfo.getPluginKey(), e); // 8. 记录启动失败的日志
                }
            });
        });

    }

}

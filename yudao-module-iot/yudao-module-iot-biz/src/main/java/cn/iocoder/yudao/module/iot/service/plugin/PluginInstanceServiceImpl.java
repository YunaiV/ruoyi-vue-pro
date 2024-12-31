package cn.iocoder.yudao.module.iot.service.plugin;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance.PluginInstanceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.PluginInstanceMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 插件实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PluginInstanceServiceImpl implements PluginInstanceService {

    /**
     * 主程序id
     */
    public static final String MAIN_ID = IdUtil.fastSimpleUUID();

    @Resource
    private PluginInfoService pluginInfoService;
    @Resource
    private PluginInstanceMapper pluginInstanceMapper;
    @Resource
    private SpringPluginManager pluginManager;

    @Value("${server.port:48080}")
    private int port;


    @Override
    public void updatePluginInstances() {
        // 1. 查询 pf4j 插件列表
        List<PluginWrapper> plugins = pluginManager.getPlugins();

        // 2. 查询插件信息列表
        List<PluginInfoDO> pluginInfos = pluginInfoService.getPluginInfoList();

        // 动态获取主程序的 IP 和端口
        String mainIp = getLocalIpAddress();

        // 3. 遍历插件列表，并保存为插件实例
        for (PluginWrapper plugin : plugins) {
            String pluginKey = plugin.getPluginId();
            PluginInfoDO pluginInfo = pluginInfos.stream()
                    .filter(pluginInfoDO -> pluginInfoDO.getPluginKey().equals(pluginKey))
                    .findFirst()
                    .orElse(null);

            // 4. 如果插件信息不存在，则跳过
            if (pluginInfo == null) {
                continue;
            }

            // 5. 查询插件实例
            PluginInstanceDO pluginInstance = pluginInstanceMapper.selectByMainIdAndPluginId(MAIN_ID, pluginInfo.getId());

            // 6. 如果插件实例不存在，则创建
            if (pluginInstance == null) {
                pluginInstance = new PluginInstanceDO();
                pluginInstance.setPluginId(pluginInfo.getId());
                pluginInstance.setMainId(MAIN_ID);
                pluginInstance.setIp(mainIp);
                pluginInstance.setPort(port);
                pluginInstance.setHeartbeatAt(System.currentTimeMillis());
                pluginInstanceMapper.insert(pluginInstance);
            } else {
                // 7. 如果插件实例存在，则更新
                pluginInstance.setHeartbeatAt(System.currentTimeMillis());
                pluginInstanceMapper.updateById(pluginInstance);
            }
        }
    }

    private String getLocalIpAddress() {
        try {
            List<String> ipList = NetUtil.localIpv4s().stream()
                    .filter(ip -> !ip.startsWith("0.0") && !ip.startsWith("127.") && !ip.startsWith("169.254") && !ip.startsWith("255.255.255.255"))
                    .toList();
            return ipList.isEmpty() ? "127.0.0.1" : ipList.get(0);
        } catch (Exception e) {
            log.error("获取本地IP地址失败", e);
            return "127.0.0.1"; // 默认值
        }
    }

}
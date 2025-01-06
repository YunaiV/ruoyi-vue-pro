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
     * 主程序 ID
     */
    // TODO @haohao：这个可以后续确认下，有没更合适的标识。例如说 mac 地址之类的
    public static final String MAIN_ID = IdUtil.fastSimpleUUID();

    @Resource
    private PluginInfoService pluginInfoService;
    @Resource
    private PluginInstanceMapper pluginInstanceMapper;
    @Resource
    private SpringPluginManager pluginManager;

    @Value("${server.port:48080}")
    private int port;

    // TODO @haohao：建议把 PluginInfoServiceImpl 里面，和 instance 相关的逻辑拿过来，可能会更好。info 处理信息，instance 处理实例

    // TODO @haohao：这个改成 reportPluginInstance 会不会更合适哈。
    @Override
    public void updatePluginInstances() {
        // 1.1 查询 pf4j 插件列表
        List<PluginWrapper> plugins = pluginManager.getPlugins();
        // 1.2 查询插件信息列表
        List<PluginInfoDO> pluginInfos = pluginInfoService.getPluginInfoList();
        // 1.3 动态获取主程序的 IP 和端口
        String mainIp = getLocalIpAddress();

        // 2. 遍历插件列表，并保存为插件实例
        for (PluginWrapper plugin : plugins) {
            // 2.1 查找插件信息
            String pluginKey = plugin.getPluginId();
            // TODO @haohao：CollUtil.findOne() 简化
            PluginInfoDO pluginInfo = pluginInfos.stream()
                    .filter(pluginInfoDO -> pluginInfoDO.getPluginKey().equals(pluginKey))
                    .findFirst()
                    .orElse(null);
            if (pluginInfo == null) {
                // TODO @haohao：建议打个 error log
                continue;
            }

            // 2.2 查询插件实例
            PluginInstanceDO pluginInstance = pluginInstanceMapper.selectByMainIdAndPluginId(MAIN_ID, pluginInfo.getId());
            // 2.3.1 如果插件实例不存在，则创建
            if (pluginInstance == null) {
                // TODO @haohao：可以链式调用；建议新建一个！
                pluginInstance = new PluginInstanceDO();
                pluginInstance.setPluginId(pluginInfo.getId());
                pluginInstance.setMainId(MAIN_ID);
                pluginInstance.setIp(mainIp);
                pluginInstance.setPort(port);
                pluginInstance.setHeartbeatAt(System.currentTimeMillis());
                pluginInstanceMapper.insert(pluginInstance);
            } else {
                // 2.3.2 如果插件实例存在，则更新
                pluginInstance.setHeartbeatAt(System.currentTimeMillis());
                pluginInstanceMapper.updateById(pluginInstance);
            }
        }
    }

    // TODO @haohao：这个目的是，获取到第一个有效 ip 是哇？
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
package cn.iocoder.yudao.module.iot.service.plugin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInstanceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.IotPluginInfoMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.IotPluginInstanceMapper;
import cn.iocoder.yudao.module.iot.dal.redis.plugin.DevicePluginProcessIdRedisDAO;
import cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * IoT 插件实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotPluginInstanceServiceImpl implements IotPluginInstanceService {

    // TODO @haohao：mac@uuid
    public static final String MAIN_ID = IdUtil.fastSimpleUUID();

    // TODO @haohao：不直接操作，通过 Service 哈
    @Resource
    private IotPluginInfoMapper pluginInfoMapper;

    @Resource
    private IotPluginInstanceMapper pluginInstanceMapper;

    @Resource
    private DevicePluginProcessIdRedisDAO devicePluginProcessIdRedisDAO;

    @Resource
    private SpringPluginManager pluginManager;

    @Value("${pf4j.pluginsDir}")
    private String pluginsDir;
    @Value("${server.port:48080}")
    private int port;

    @Override
    public void stopAndUnloadPlugin(String pluginKey) {
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);
        if (plugin == null) {
            log.warn("插件不存在或已卸载: {}", pluginKey);
            return;
        }
        if (plugin.getPluginState().equals(PluginState.STARTED)) {
            pluginManager.stopPlugin(pluginKey); // 停止插件
            log.info("已停止插件: {}", pluginKey);
        }
        pluginManager.unloadPlugin(pluginKey); // 卸载插件
        log.info("已卸载插件: {}", pluginKey);
    }

    @Override
    public void deletePluginFile(IotPluginInfoDO pluginInfoDO) {
        File file = new File(pluginsDir, pluginInfoDO.getFileName());
        if (!file.exists()) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(1); // 等待 1 秒，避免插件未卸载完毕
            if (!file.delete()) {
                log.error("[deletePluginInfo][删除插件文件({}) 失败]", pluginInfoDO.getFileName());
            }
        } catch (InterruptedException e) {
            log.error("[deletePluginInfo][删除插件文件({}) 失败]", pluginInfoDO.getFileName(), e);
        }
    }

    @Override
    public String uploadAndLoadNewPlugin(MultipartFile file) {
        String pluginKeyNew;
        // TODO @haohao：多节点，是不是要上传 s3 之类的存储器；然后定时去加载
        Path pluginsPath = Paths.get(pluginsDir);
        try {
            FileUtil.mkdir(pluginsPath.toFile()); // 创建插件目录
            String filename = file.getOriginalFilename();
            if (filename != null) {
                Path jarPath = pluginsPath.resolve(filename);
                Files.copy(file.getInputStream(), jarPath, StandardCopyOption.REPLACE_EXISTING); // 保存上传的 JAR 文件
                pluginKeyNew = pluginManager.loadPlugin(jarPath.toAbsolutePath()); // 加载插件
                log.info("已加载插件: {}", pluginKeyNew);
            } else {
                throw exception(ErrorCodeConstants.PLUGIN_INSTALL_FAILED);
            }
        } catch (IOException e) {
            log.error("[uploadAndLoadNewPlugin][上传插件文件失败]", e);
            throw exception(ErrorCodeConstants.PLUGIN_INSTALL_FAILED, e);
        } catch (Exception e) {
            log.error("[uploadAndLoadNewPlugin][加载插件失败]", e);
            throw exception(ErrorCodeConstants.PLUGIN_INSTALL_FAILED, e);
        }
        return pluginKeyNew;
    }

    @Override
    public void updatePluginStatus(IotPluginInfoDO pluginInfoDo, Integer status) {
        String pluginKey = pluginInfoDo.getPluginKey();
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);

        if (plugin == null) {
            // 插件不存在且状态为停止，抛出异常
            if (IotPluginStatusEnum.STOPPED.getStatus().equals(pluginInfoDo.getStatus())) {
                throw exception(ErrorCodeConstants.PLUGIN_STATUS_INVALID);
            }
            return;
        }

        // 启动插件
        if (status.equals(IotPluginStatusEnum.RUNNING.getStatus())
                && plugin.getPluginState() != PluginState.STARTED) {
            pluginManager.startPlugin(pluginKey);
            log.info("已启动插件: {}", pluginKey);
        }
        // 停止插件
        else if (status.equals(IotPluginStatusEnum.STOPPED.getStatus())
                && plugin.getPluginState() == PluginState.STARTED) {
            pluginManager.stopPlugin(pluginKey);
            log.info("已停止插件: {}", pluginKey);
        }
    }

    @Override
    public void reportPluginInstances() {
        // 1.1 获取 pf4j 插件列表
        List<PluginWrapper> plugins = pluginManager.getPlugins();

        // 1.2 获取插件信息列表并转换为 Map 以便快速查找
        List<IotPluginInfoDO> pluginInfos = pluginInfoMapper.selectList();
        Map<String, IotPluginInfoDO> pluginInfoMap = pluginInfos.stream()
                .collect(Collectors.toMap(IotPluginInfoDO::getPluginKey, Function.identity()));

        // 1.3 获取本机 IP 和 MAC 地址,mac@uuid
        String ip = NetUtil.getLocalhostStr();
        String mac = NetUtil.getLocalMacAddress();
        String mainId = mac + "@" + MAIN_ID;

        // 2. 遍历插件列表，并保存为插件实例
        for (PluginWrapper plugin : plugins) {
            String pluginKey = plugin.getPluginId();

            // 2.1 查找插件信息
            IotPluginInfoDO pluginInfo = pluginInfoMap.get(pluginKey);
            if (pluginInfo == null) {
                log.error("插件信息不存在，pluginKey = {}", pluginKey);
                continue;
            }

            // 2.2 情况一：如果插件实例不存在，则创建
            IotPluginInstanceDO pluginInstance = pluginInstanceMapper.selectByMainIdAndPluginId(mainId,
                    pluginInfo.getId());
            // TODO @芋艿：稍后优化；
//            if (pluginInstance == null) {
//                // 4.4 如果插件实例不存在，则创建
//                pluginInstance = PluginInstanceDO.builder()
//                        .pluginId(pluginInfo.getId())
//                        .mainId(MAIN_ID + "-" + mac)
//                        .hostIp(ip)
//                        .port(port)
//                        .heartbeatAt(System.currentTimeMillis())
//                        .build();
//                pluginInstanceMapper.insert(pluginInstance);
//            } else {
//                // 2.2 情况二：如果存在，则更新 heartbeatAt
//                PluginInstanceDO updatePluginInstance = PluginInstanceDO.builder()
//                        .id(pluginInstance.getId())
//                        .heartbeatAt(System.currentTimeMillis())
//                        .build();
//                pluginInstanceMapper.updateById(updatePluginInstance);
//            }
        }
    }

    // ========== 设备与插件的映射操作 ==========

    @Override
    public void updateDevicePluginInstanceProcessIdAsync(String deviceKey, String processId) {
        devicePluginProcessIdRedisDAO.put(deviceKey, processId);
    }

}
package cn.iocoder.yudao.module.iot.service.plugin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance.PluginInstanceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.PluginInfoMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.PluginInstanceMapper;
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
public class PluginInstanceServiceImpl implements PluginInstanceService {

    // TODO @haohao：这个可以后续确认下，有没更合适的标识。例如说 mac 地址之类的
    // 简化的UUID + mac 地址 会不会好一些，一台机子有可能会部署多个插件
    public static final String MAIN_ID = IdUtil.fastSimpleUUID();

    @Resource
    private PluginInfoMapper pluginInfoMapper;
    @Resource
    private PluginInstanceMapper pluginInstanceMapper;
    @Resource
    private SpringPluginManager pluginManager;

    @Value("${pf4j.pluginsDir}")
    private String pluginsDir;

    @Value("${server.port:48080}")
    private int port;

    @Override
    public void stopAndUnloadPlugin(String pluginKey) {
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);
        if (plugin != null) {
            if (plugin.getPluginState().equals(PluginState.STARTED)) {
                pluginManager.stopPlugin(pluginKey); // 停止插件
                log.info("已停止插件: {}", pluginKey);
            }
            pluginManager.unloadPlugin(pluginKey); // 卸载插件
            log.info("已卸载插件: {}", pluginKey);
        } else {
            log.warn("插件不存在或已卸载: {}", pluginKey);
        }
    }

    @Override
    public void deletePluginFile(PluginInfoDO pluginInfoDO) {
        File file = new File(pluginsDir, pluginInfoDO.getFileName());
        if (file.exists()) {
            try {
                TimeUnit.SECONDS.sleep(1); // 等待 1 秒，避免插件未卸载完毕
                if (!file.delete()) {
                    log.error("[deletePluginInfo][删除插件文件({}) 失败]", pluginInfoDO.getFileName());
                }
            } catch (InterruptedException e) {
                log.error("[deletePluginInfo][删除插件文件({}) 失败]", pluginInfoDO.getFileName(),
                        e);
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
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
    public void updatePluginStatusFile(String pluginKeyNew, boolean isEnabled) {
        // TODO @haohao：疑问，这里写 enabled.txt 和 disabled.txt 的目的是啥哈？
        // pf4j 的插件状态文件，需要 2 个文件，一个 enabled.txt 一个 disabled.txt
        Path enabledFilePath = Paths.get(pluginsDir, "enabled.txt");
        Path disabledFilePath = Paths.get(pluginsDir, "disabled.txt");
        Path targetFilePath = isEnabled ? enabledFilePath : disabledFilePath;
        Path oppositeFilePath = isEnabled ? disabledFilePath : enabledFilePath;

        try {
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginKeyNew);
            if (pluginWrapper == null) {
                throw exception(ErrorCodeConstants.PLUGIN_INSTALL_FAILED);
            }
            List<String> targetLines = Files.exists(targetFilePath) ? Files.readAllLines(targetFilePath)
                    : new ArrayList<>();
            List<String> oppositeLines = Files.exists(oppositeFilePath) ? Files.readAllLines(oppositeFilePath)
                    : new ArrayList<>();

            if (!targetLines.contains(pluginKeyNew)) {
                targetLines.add(pluginKeyNew);
                Files.write(targetFilePath, targetLines, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                log.info("已添加插件 {} 到 {}", pluginKeyNew, targetFilePath.getFileName());
            }

            if (oppositeLines.contains(pluginKeyNew)) {
                oppositeLines.remove(pluginKeyNew);
                Files.write(oppositeFilePath, oppositeLines, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                log.info("已从 {} 移除插件 {}", oppositeFilePath.getFileName(), pluginKeyNew);
            }
        } catch (IOException e) {
            log.error("[updatePluginStatusFile][更新插件状态文件失败]", e);
            throw exception(ErrorCodeConstants.PLUGIN_INSTALL_FAILED, e);
        }
    }

    @Override
    public void updatePluginStatus(PluginInfoDO pluginInfoDo, Integer status) {
        String pluginKey = pluginInfoDo.getPluginKey();
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);

        if (plugin != null) {
            // 启动插件
            if (status.equals(IotPluginStatusEnum.RUNNING.getStatus())
                    && plugin.getPluginState() != PluginState.STARTED) {
                pluginManager.startPlugin(pluginKey);
                updatePluginStatusFile(pluginKey, true);
                log.info("已启动插件: {}", pluginKey);
            }
            // 停止插件
            else if (status.equals(IotPluginStatusEnum.STOPPED.getStatus())
                    && plugin.getPluginState() == PluginState.STARTED) {
                pluginManager.stopPlugin(pluginKey);
                updatePluginStatusFile(pluginKey, false);
                log.info("已停止插件: {}", pluginKey);
            }
        } else {
            // 插件不存在且状态为停止，抛出异常
            if (IotPluginStatusEnum.STOPPED.getStatus().equals(pluginInfoDo.getStatus())) {
                throw exception(ErrorCodeConstants.PLUGIN_STATUS_INVALID);
            }
        }
    }

    @Override
    public void reportPluginInstances() {
        // 1. 获取 pf4j 插件列表
        List<PluginWrapper> plugins = pluginManager.getPlugins();

        // 2. 获取插件信息列表并转换为 Map 以便快速查找
        List<PluginInfoDO> pluginInfos = pluginInfoMapper.selectList();
        Map<String, PluginInfoDO> pluginInfoMap = pluginInfos.stream()
                .collect(Collectors.toMap(PluginInfoDO::getPluginKey, Function.identity()));

        // 3. 获取本机 IP 和 MAC 地址
        String ip = NetUtil.getLocalhostStr();
        String mac = NetUtil.getLocalMacAddress();
        String mainId = MAIN_ID + "-" + mac;

        // 4. 遍历插件列表，并保存为插件实例
        for (PluginWrapper plugin : plugins) {
            String pluginKey = plugin.getPluginId();

            // 4.1 查找插件信息
            PluginInfoDO pluginInfo = pluginInfoMap.get(pluginKey);
            if (pluginInfo == null) {
                // 4.2 插件信息不存在，记录错误并跳过
                log.error("插件信息不存在，插件包标识符 = {}", pluginKey);
                continue;
            }

            // 4.3 查询插件实例
            PluginInstanceDO pluginInstance = pluginInstanceMapper.selectByMainIdAndPluginId(mainId,
                    pluginInfo.getId());
            if (pluginInstance == null) {
                // 4.4 如果插件实例不存在，则创建
                pluginInstance = PluginInstanceDO.builder()
                        .pluginId(pluginInfo.getId())
                        .mainId(MAIN_ID + "-" + mac)
                        .ip(ip)
                        .port(port)
                        .heartbeatAt(System.currentTimeMillis())
                        .build();
                pluginInstanceMapper.insert(pluginInstance);
            } else {
                // 4.5 如果插件实例存在，则更新心跳时间
                pluginInstance.setHeartbeatAt(System.currentTimeMillis());
                pluginInstanceMapper.updateById(pluginInstance);
            }
        }
    }

}
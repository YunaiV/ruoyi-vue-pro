package cn.iocoder.yudao.module.iot.service.plugin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInstanceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.IotPluginInstanceMapper;
import cn.iocoder.yudao.module.iot.dal.redis.plugin.DevicePluginProcessIdRedisDAO;
import cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotPluginConfigService pluginConfigService;

    @Resource
    private IotPluginInstanceMapper pluginInstanceMapper;

    @Resource
    private DevicePluginProcessIdRedisDAO devicePluginProcessIdRedisDAO;

    @Resource
    private SpringPluginManager pluginManager;

    @Value("${pf4j.pluginsDir}")
    private String pluginsDir;

    @Override
    public void heartbeatPluginInstance(IotPluginInstanceHeartbeatReqDTO heartbeatReqDTO) {
        // 情况一：已存在，则进行更新
        IotPluginInstanceDO instance = TenantUtils.executeIgnore(
                () -> pluginInstanceMapper.selectByProcessId(heartbeatReqDTO.getProcessId()));
        if (instance != null) {
            IotPluginInstanceDO.IotPluginInstanceDOBuilder updateObj = IotPluginInstanceDO.builder().id(instance.getId())
                    .hostIp(heartbeatReqDTO.getHostIp()).downstreamPort(heartbeatReqDTO.getDownstreamPort())
                    .online(heartbeatReqDTO.getOnline()).heartbeatTime(LocalDateTime.now());
            if (Boolean.TRUE.equals(heartbeatReqDTO.getOnline())) {
                if (Boolean.FALSE.equals(instance.getOnline())) { // 当前处于离线时，才需要更新上线时间
                    updateObj.onlineTime(LocalDateTime.now());
                }
            } else {
                updateObj.offlineTime(LocalDateTime.now());
            }
            TenantUtils.execute(instance.getTenantId(),
                    () -> pluginInstanceMapper.updateById(updateObj.build()));
            return;
        }

        // 情况二：不存在，则创建
        IotPluginConfigDO info = TenantUtils.executeIgnore(
                () -> pluginConfigService.getPluginConfigByPluginKey(heartbeatReqDTO.getPluginKey()));
        if (info == null) {
            log.error("[heartbeatPluginInstance][心跳({}) 对应的插件不存在]", heartbeatReqDTO);
            return;
        }
        IotPluginInstanceDO.IotPluginInstanceDOBuilder insertObj = IotPluginInstanceDO.builder()
                .pluginId(info.getId()).processId(heartbeatReqDTO.getProcessId())
                .hostIp(heartbeatReqDTO.getHostIp()).downstreamPort(heartbeatReqDTO.getDownstreamPort())
                .online(heartbeatReqDTO.getOnline()).heartbeatTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(heartbeatReqDTO.getOnline())) {
            insertObj.onlineTime(LocalDateTime.now());
        } else {
            insertObj.offlineTime(LocalDateTime.now());
        }
        TenantUtils.execute(info.getTenantId(),
                () -> pluginInstanceMapper.insert(insertObj.build()));
    }

    @Override
    public int offlineTimeoutPluginInstance(LocalDateTime maxHeartbeatTime) {
        List<IotPluginInstanceDO> list = pluginInstanceMapper.selectListByHeartbeatTimeLt(maxHeartbeatTime);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }

        // 更新插件实例为离线
        int count = 0;
        for (IotPluginInstanceDO instance : list) {
            pluginInstanceMapper.updateById(IotPluginInstanceDO.builder().id(instance.getId())
                    .online(false).offlineTime(LocalDateTime.now()).build());
            count++;
        }
        return count;
    }

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
    public void deletePluginFile(IotPluginConfigDO pluginConfigDO) {
        File file = new File(pluginsDir, pluginConfigDO.getFileName());
        if (!file.exists()) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(1); // 等待 1 秒，避免插件未卸载完毕
            if (!file.delete()) {
                log.error("[deletePluginFile][删除插件文件({}) 失败]", pluginConfigDO.getFileName());
            }
        } catch (InterruptedException e) {
            log.error("[deletePluginFile][删除插件文件({}) 失败]", pluginConfigDO.getFileName(), e);
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
    public void updatePluginStatus(IotPluginConfigDO pluginConfigDO, Integer status) {
        String pluginKey = pluginConfigDO.getPluginKey();
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);

        if (plugin == null) {
            // 插件不存在且状态为停止，抛出异常
            if (IotPluginStatusEnum.STOPPED.getStatus().equals(pluginConfigDO.getStatus())) {
                throw exception(ErrorCodeConstants.PLUGIN_STATUS_INVALID);
            }
            return;
        }

        // 启动插件
        if (status.equals(IotPluginStatusEnum.RUNNING.getStatus())
                && plugin.getPluginState() != PluginState.STARTED) {
            try {
                pluginManager.startPlugin(pluginKey);
            } catch (Exception e) {
                log.error("[updatePluginStatus][启动插件({}) 失败]", pluginKey, e);
                throw exception(ErrorCodeConstants.PLUGIN_START_FAILED, e);
            }
            log.info("已启动插件: {}", pluginKey);
        }
        // 停止插件
        else if (status.equals(IotPluginStatusEnum.STOPPED.getStatus())
                && plugin.getPluginState() == PluginState.STARTED) {
            try {
                pluginManager.stopPlugin(pluginKey);
            } catch (Exception e) {
                log.error("[updatePluginStatus][停止插件({}) 失败]", pluginKey, e);
                throw exception(ErrorCodeConstants.PLUGIN_STOP_FAILED, e);
            }
            log.info("已停止插件: {}", pluginKey);
        }
    }

    // ========== 设备与插件的映射操作 ==========

    @Override
    public void updateDevicePluginInstanceProcessIdAsync(String deviceKey, String processId) {
        devicePluginProcessIdRedisDAO.put(deviceKey, processId);
    }

    @Override
    public IotPluginInstanceDO getPluginInstanceByDeviceKey(String deviceKey) {
        String processId = devicePluginProcessIdRedisDAO.get(deviceKey);
        if (StrUtil.isEmpty(processId)) {
            return null;
        }
        return pluginInstanceMapper.selectByProcessId(processId);
    }

}
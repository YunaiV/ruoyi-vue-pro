package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInfoPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInfoSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.PluginInfoMapper;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginDescriptor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 插件信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PluginInfoServiceImpl implements PluginInfoService {

    @Resource
    private PluginInfoMapper pluginInfoMapper;
    @Resource
    private SpringPluginManager pluginManager;

    @Value("${pf4j.pluginsDir}")
    private String pluginsDir;

    @Override
    public Long createPluginInfo(PluginInfoSaveReqVO createReqVO) {
        // 插入
        PluginInfoDO pluginInfo = BeanUtils.toBean(createReqVO, PluginInfoDO.class);
        pluginInfoMapper.insert(pluginInfo);
        // 返回
        return pluginInfo.getId();
    }

    @Override
    public void updatePluginInfo(PluginInfoSaveReqVO updateReqVO) {
        // 校验存在
        validatePluginInfoExists(updateReqVO.getId());
        // 更新
        PluginInfoDO updateObj = BeanUtils.toBean(updateReqVO, PluginInfoDO.class);
        pluginInfoMapper.updateById(updateObj);
    }

    @Override
    public void deletePluginInfo(Long id) {
        // 校验存在
        PluginInfoDO pluginInfoDO = validatePluginInfoExists(id);

        // 停止插件
        if (IotPluginStatusEnum.RUNNING.getStatus().equals(pluginInfoDO.getStatus())) {
            throw exception(PLUGIN_INFO_DELETE_FAILED_RUNNING);
        }

        // 卸载插件
        PluginWrapper plugin = pluginManager.getPlugin(pluginInfoDO.getPluginId());
        if (plugin != null) {
            // 查询插件是否是启动状态
            if (plugin.getPluginState().equals(PluginState.STARTED)) {
                // 停止插件
                pluginManager.stopPlugin(plugin.getPluginId());
            }
            // 卸载插件
            pluginManager.unloadPlugin(plugin.getPluginId());
        }

        // 删除
        pluginInfoMapper.deleteById(id);
        // 删除插件文件
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(1); // 等待 1 秒，避免插件未卸载完毕
                File file = new File(pluginsDir, pluginInfoDO.getFileName());
                if (file.exists() && !file.delete()) {
                    log.error("[deletePluginInfo][删除插件文件({}) 失败]", pluginInfoDO.getFileName());
                }
            } catch (InterruptedException e) {
                log.error("[deletePluginInfo][删除插件文件({}) 失败]", pluginInfoDO.getFileName(), e);
            }
        });

    }

    private PluginInfoDO validatePluginInfoExists(Long id) {
        PluginInfoDO pluginInfo = pluginInfoMapper.selectById(id);
        if (pluginInfo == null) {
            throw exception(PLUGIN_INFO_NOT_EXISTS);
        }
        return pluginInfo;
    }

    @Override
    public PluginInfoDO getPluginInfo(Long id) {
        return pluginInfoMapper.selectById(id);
    }

    @Override
    public PageResult<PluginInfoDO> getPluginInfoPage(PluginInfoPageReqVO pageReqVO) {
        return pluginInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public void uploadFile(Long id, MultipartFile file) {
        // 1. 校验插件信息是否存在
        PluginInfoDO pluginInfoDo = validatePluginInfoExists(id);

        // 2. 获取插件 ID
        String pluginId = pluginInfoDo.getPluginId();

        // 3. 停止并卸载旧的插件
        stopAndUnloadPlugin(pluginId);

        // 4. 上传新的插件文件
        String pluginIdNew = uploadAndLoadNewPlugin(file);

        // 5. 更新插件启用状态文件
        updatePluginStatusFile(pluginIdNew, false);

        // 6. 更新插件信息
        updatePluginInfo(pluginInfoDo, pluginIdNew, file);
    }

    // 停止并卸载旧的插件
    private void stopAndUnloadPlugin(String pluginId) {
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        if (plugin != null) {
            if (plugin.getPluginState().equals(PluginState.STARTED)) {
                pluginManager.stopPlugin(pluginId); // 停止插件
            }
            pluginManager.unloadPlugin(pluginId); // 卸载插件
        }
    }

    // 上传并加载新的插件文件
    private String uploadAndLoadNewPlugin(MultipartFile file) {
        Path pluginsPath = Paths.get(pluginsDir);
        try {
            if (!Files.exists(pluginsPath)) {
                Files.createDirectories(pluginsPath); // 创建插件目录
            }
            String filename = file.getOriginalFilename();
            if (filename != null) {
                Path jarPath = pluginsPath.resolve(filename);
                Files.copy(file.getInputStream(), jarPath, StandardCopyOption.REPLACE_EXISTING); // 保存上传的 JAR 文件
                return pluginManager.loadPlugin(jarPath.toAbsolutePath()); // 加载插件
            } else {
                throw exception(PLUGIN_INSTALL_FAILED);
            }
        } catch (Exception e) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }
    }

    // 更新插件状态文件
    private void updatePluginStatusFile(String pluginIdNew, boolean isEnabled) {
        Path enabledFilePath = Paths.get(pluginsDir, "enabled.txt");
        Path disabledFilePath = Paths.get(pluginsDir, "disabled.txt");
        Path targetFilePath = isEnabled ? enabledFilePath : disabledFilePath;
        Path oppositeFilePath = isEnabled ? disabledFilePath : enabledFilePath;

        try {
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginIdNew);
            if (pluginWrapper == null) {
                throw exception(PLUGIN_INSTALL_FAILED);
            }
            String pluginInfo = pluginIdNew + "@" + pluginWrapper.getDescriptor().getVersion();
            List<String> targetLines = Files.exists(targetFilePath) ? Files.readAllLines(targetFilePath)
                    : new ArrayList<>();
            List<String> oppositeLines = Files.exists(oppositeFilePath) ? Files.readAllLines(oppositeFilePath)
                    : new ArrayList<>();

            if (!targetLines.contains(pluginInfo)) {
                targetLines.add(pluginInfo);
                Files.write(targetFilePath, targetLines, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

            if (oppositeLines.contains(pluginInfo)) {
                oppositeLines.remove(pluginInfo);
                Files.write(oppositeFilePath, oppositeLines, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }
    }

    // 更新插件信息
    private void updatePluginInfo(PluginInfoDO pluginInfoDo, String pluginIdNew, MultipartFile file) {
        pluginInfoDo.setPluginId(pluginIdNew);
        pluginInfoDo.setStatus(IotPluginStatusEnum.STOPPED.getStatus());
        pluginInfoDo.setFileName(file.getOriginalFilename());
        pluginInfoDo.setScript("");

        PluginDescriptor pluginDescriptor = pluginManager.getPlugin(pluginIdNew).getDescriptor();
        pluginInfoDo.setConfigSchema(pluginDescriptor.getPluginDescription());
        pluginInfoDo.setVersion(pluginDescriptor.getVersion());
        pluginInfoDo.setDescription(pluginDescriptor.getPluginDescription());
        pluginInfoMapper.updateById(pluginInfoDo);
    }

    @Override
    public void updatePluginStatus(Long id, Integer status) {
        // 1. 校验插件信息是否存在
        PluginInfoDO pluginInfoDo = validatePluginInfoExists(id);

        // 2. 校验插件状态是否有效
        if (!IotPluginStatusEnum.contains(status)) {
            throw exception(PLUGIN_STATUS_INVALID);
        }

        // 3. 获取插件ID和插件实例
        String pluginId = pluginInfoDo.getPluginId();
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);

        // 4. 根据状态更新插件
        if (plugin != null) {
            // 4.1 如果目标状态是运行且插件未启动，则启动插件
            if (status.equals(IotPluginStatusEnum.RUNNING.getStatus())
                    && plugin.getPluginState() != PluginState.STARTED) {
                pluginManager.startPlugin(pluginId);
                updatePluginStatusFile(pluginId, true); // 更新插件状态文件为启用
            }
            // 4.2 如果目标状态是停止且插件已启动，则停止插件
            else if (status.equals(IotPluginStatusEnum.STOPPED.getStatus())
                    && plugin.getPluginState() == PluginState.STARTED) {
                pluginManager.stopPlugin(pluginId);
                updatePluginStatusFile(pluginId, false); // 更新插件状态文件为禁用
            }
        } else {
            // 5. 插件不存在且状态为停止，抛出异常
            if (IotPluginStatusEnum.STOPPED.getStatus().equals(pluginInfoDo.getStatus())) {
                throw exception(PLUGIN_STATUS_INVALID);
            }
        }

        // 6. 更新数据库中的插件状态
        pluginInfoDo.setStatus(status);
        pluginInfoMapper.updateById(pluginInfoDo);
    }

    @Override
    public List<String> getEnabledPlugins() {
        return pluginInfoMapper.selectList().stream()
                .filter(pluginInfoDO -> IotPluginStatusEnum.RUNNING.getStatus().equals(pluginInfoDO.getStatus()))
                .map(PluginInfoDO::getPluginId)
                .toList();
    }

}
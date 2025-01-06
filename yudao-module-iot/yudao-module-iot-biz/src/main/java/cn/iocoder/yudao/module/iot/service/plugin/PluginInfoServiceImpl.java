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

    // TODO @芋艿：要不要换位置
    @Value("${pf4j.pluginsDir}")
    private String pluginsDir;

    @Override
    public Long createPluginInfo(PluginInfoSaveReqVO createReqVO) {
        PluginInfoDO pluginInfo = BeanUtils.toBean(createReqVO, PluginInfoDO.class);
        pluginInfoMapper.insert(pluginInfo);
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
        // 1.1 校验存在
        PluginInfoDO pluginInfoDO = validatePluginInfoExists(id);
        // 1.2 停止插件
        if (IotPluginStatusEnum.RUNNING.getStatus().equals(pluginInfoDO.getStatus())) {
            throw exception(PLUGIN_INFO_DELETE_FAILED_RUNNING);
        }

        // 2. 卸载插件
        // TODO @haohao：可以复用 stopAndUnloadPlugin
        PluginWrapper plugin = pluginManager.getPlugin(pluginInfoDO.getPluginKey());
        if (plugin != null) {
            // 停止插件
            if (plugin.getPluginState().equals(PluginState.STARTED)) {
                pluginManager.stopPlugin(plugin.getPluginId());
            }
            // 卸载插件
            pluginManager.unloadPlugin(plugin.getPluginId());
        }

        // 3.1 删除
        pluginInfoMapper.deleteById(id);
        // 3.2 删除插件文件
        // TODO @haohao：这个直接主线程 sleep 就好了，不用单独开线程池哈。原因是，低频操作；另外，只有存在的时候，才 sleep + 删除；
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

        // 2. 停止并卸载旧的插件
        stopAndUnloadPlugin(pluginInfoDo.getPluginKey());

        // 3.1 上传新的插件文件
        String pluginKeyNew = uploadAndLoadNewPlugin(file);
        // 3.2 更新插件启用状态文件
        updatePluginStatusFile(pluginKeyNew, false);

        // 4. 更新插件信息
        updatePluginInfo(pluginInfoDo, pluginKeyNew, file);
    }

    // TODO @haohao：注释的格式
    // 停止并卸载旧的插件
    private void stopAndUnloadPlugin(String pluginKey) {
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);
        if (plugin != null) {
            if (plugin.getPluginState().equals(PluginState.STARTED)) {
                pluginManager.stopPlugin(pluginKey); // 停止插件
            }
            pluginManager.unloadPlugin(pluginKey); // 卸载插件
        }
    }

    // TODO @haohao：注释的格式
    // 上传并加载新的插件文件
    private String uploadAndLoadNewPlugin(MultipartFile file) {
        // TODO @haohao：多节点，是不是要上传 s3 之类的存储器；然后定时去加载
        Path pluginsPath = Paths.get(pluginsDir);
        try {
            // TODO @haohao：可以使用 FileUtil 简化？
            if (!Files.exists(pluginsPath)) {
                Files.createDirectories(pluginsPath); // 创建插件目录
            }
            String filename = file.getOriginalFilename();
            if (filename != null) {
                Path jarPath = pluginsPath.resolve(filename);
                Files.copy(file.getInputStream(), jarPath, StandardCopyOption.REPLACE_EXISTING); // 保存上传的 JAR 文件
                return pluginManager.loadPlugin(jarPath.toAbsolutePath()); // 加载插件
            }
            throw exception(PLUGIN_INSTALL_FAILED); // TODO @haohao：这么抛的话，貌似会被 catch (Exception e) {
        } catch (Exception e) {
            // TODO @haohao：打个 error log，方便排查
            throw exception(PLUGIN_INSTALL_FAILED);
        }
    }

    // TODO @haohao：注释的格式
    // 更新插件状态文件
    private void updatePluginStatusFile(String pluginKeyNew, boolean isEnabled) {
        // TODO @haohao：疑问，这里写 enabled.txt 和 disabled.txt 的目的是啥哈？
        Path enabledFilePath = Paths.get(pluginsDir, "enabled.txt");
        Path disabledFilePath = Paths.get(pluginsDir, "disabled.txt");
        Path targetFilePath = isEnabled ? enabledFilePath : disabledFilePath;
        Path oppositeFilePath = isEnabled ? disabledFilePath : enabledFilePath;

        try {
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginKeyNew);
            if (pluginWrapper == null) {
                throw exception(PLUGIN_INSTALL_FAILED);
            }
            List<String> targetLines = Files.exists(targetFilePath) ? Files.readAllLines(targetFilePath) : new ArrayList<>();
            List<String> oppositeLines = Files.exists(oppositeFilePath) ? Files.readAllLines(oppositeFilePath) : new ArrayList<>();

            if (!targetLines.contains(pluginKeyNew)) {
                targetLines.add(pluginKeyNew);
                Files.write(targetFilePath, targetLines, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

            if (oppositeLines.contains(pluginKeyNew)) {
                oppositeLines.remove(pluginKeyNew);
                Files.write(oppositeFilePath, oppositeLines, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }
    }

    // TODO @haohao：注释的格式
    // 更新插件信息
    private void updatePluginInfo(PluginInfoDO pluginInfoDo, String pluginKeyNew, MultipartFile file) {
        // TODO @haohao：更新实体的时候，最好 new 一个新的！
        // TODO @haohao：可以链式调用，简化下代码；
        pluginInfoDo.setPluginKey(pluginKeyNew);
        pluginInfoDo.setStatus(IotPluginStatusEnum.STOPPED.getStatus());
        pluginInfoDo.setFileName(file.getOriginalFilename());
        pluginInfoDo.setScript("");
        // 解析 pf4j 插件
        PluginDescriptor pluginDescriptor = pluginManager.getPlugin(pluginKeyNew).getDescriptor();
        pluginInfoDo.setConfigSchema(pluginDescriptor.getPluginDescription());
        pluginInfoDo.setVersion(pluginDescriptor.getVersion());
        pluginInfoDo.setDescription(pluginDescriptor.getPluginDescription());

        // 执行更新
        pluginInfoMapper.updateById(pluginInfoDo);
    }

    // TODO @haohao：status、state 字段命名，要统一下~
    @Override
    public void updatePluginStatus(Long id, Integer status) {
        // 1. 校验插件信息是否存在
        PluginInfoDO pluginInfoDo = validatePluginInfoExists(id);

        // 2. 校验插件状态是否有效
        // TODO @haohao：直接参数校验掉。通过 @InEnum
        if (!IotPluginStatusEnum.contains(status)) {
            throw exception(PLUGIN_STATUS_INVALID);
        }

        // 3. 获取插件标识和插件实例
        String pluginKey = pluginInfoDo.getPluginKey();
        PluginWrapper plugin = pluginManager.getPlugin(pluginKey);

        // 4. 根据状态更新插件
        if (plugin != null) {
            // 4.1 启动：如果目标状态是运行且插件未启动，则启动插件
            if (status.equals(IotPluginStatusEnum.RUNNING.getStatus())
                    && plugin.getPluginState() != PluginState.STARTED) {
                pluginManager.startPlugin(pluginKey);
                updatePluginStatusFile(pluginKey, true);
            // 4.2 停止：如果目标状态是停止且插件已启动，则停止插件
            } else if (status.equals(IotPluginStatusEnum.STOPPED.getStatus())
                    && plugin.getPluginState() == PluginState.STARTED) {
                pluginManager.stopPlugin(pluginKey);
                updatePluginStatusFile(pluginKey, false);
            }
        } else {
            // 5. 插件不存在且状态为停止，抛出异常
            if (IotPluginStatusEnum.STOPPED.getStatus().equals(pluginInfoDo.getStatus())) {
                throw exception(PLUGIN_STATUS_INVALID);
            }
        }

        // 6. 更新数据库中的插件状态
        // TODO @haohao：新建新建 pluginInfoDo 哈！
        pluginInfoDo.setStatus(status);
        pluginInfoMapper.updateById(pluginInfoDo);
    }

    @Override
    public List<PluginInfoDO> getPluginInfoList() {
        return pluginInfoMapper.selectList();
    }

    // TODO @haohao：可以改成 getPluginInfoListByStatus 更通用哈。
    @Override
    public List<PluginInfoDO> getRunningPluginInfoList() {
        return pluginInfoMapper.selectListByStatus(IotPluginStatusEnum.RUNNING.getStatus());
    }

}
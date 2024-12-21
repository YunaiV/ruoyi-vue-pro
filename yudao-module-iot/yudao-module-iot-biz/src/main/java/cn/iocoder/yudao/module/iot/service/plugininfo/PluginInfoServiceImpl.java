package cn.iocoder.yudao.module.iot.service.plugininfo;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.PluginInfoPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.PluginInfoSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugininfo.PluginInfoMapper;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    @Resource
    private FileApi fileApi;

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
    public void uploadJar(Long id, MultipartFile file) {
        // 1. 校验存在
        PluginInfoDO pluginInfoDo = validatePluginInfoExists(id);

        // 2. 判断文件名称与插件 ID 是否匹配
        String pluginId = pluginInfoDo.getPluginId();

        // 3. 停止卸载旧的插件
        // 3.1. 获取插件信息
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        if (plugin != null) {
            // 3.2. 如果插件状态是启动的，停止插件
            if (plugin.getPluginState().equals(PluginState.STARTED)) {
                pluginManager.stopPlugin(pluginId);
            }
            // 3.3. 卸载插件
            pluginManager.unloadPlugin(pluginId);
        }

        // 4. 上传插件
        String pluginIdNew;
        try {
            String path = fileApi.createFile(pluginsDir, IoUtil.readBytes(file.getInputStream()));
            Path pluginPath = Path.of(path);
            pluginIdNew = pluginManager.loadPlugin(pluginPath);
        } catch (Exception e) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }

        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginIdNew);
        if (pluginWrapper == null) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }

        // 5. 读取配置文件和脚本
        String configJson = "";
        String script = "";
        try (JarFile jarFile = new JarFile(pluginWrapper.getPluginPath().toFile())) {
            // 5.1 获取config文件在jar包中的路径
            String configFile = "classes/config.json";
            JarEntry configEntry = jarFile.getJarEntry(configFile);

            if (configEntry != null) {
                // 5.2 读取配置文件
                configJson = IoUtil.readUtf8(jarFile.getInputStream(configEntry));
                log.info("configJson:{}", configJson);
            }

            // 5.3 读取script.js脚本
            String scriptFile = "classes/script.js";
            JarEntry scriptEntity = jarFile.getJarEntry(scriptFile);
            if (scriptEntity != null) {
                // 5.4 读取脚本文件
                script = IoUtil.readUtf8(jarFile.getInputStream(scriptEntity));
                log.info("script:{}", script);
            }
        } catch (Exception e) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }

        pluginInfoDo.setPluginId(pluginIdNew);
        pluginInfoDo.setStatus(IotPluginStatusEnum.STOPPED.getStatus());
        pluginInfoDo.setFile(file.getOriginalFilename());
        pluginInfoDo.setConfigSchema(configJson);
        pluginInfoDo.setScript(script);

        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        pluginInfoDo.setVersion(pluginDescriptor.getVersion());
        pluginInfoDo.setDescription(pluginDescriptor.getPluginDescription());
        pluginInfoMapper.updateById(pluginInfoDo);
    }

    @Override
    public void updatePluginStatus(Long id, Integer status) {
        // 1. 校验存在
        PluginInfoDO pluginInfoDo = validatePluginInfoExists(id);

        // 插件状态无效
        if (!IotPluginStatusEnum.contains(status)) {
            throw exception(PLUGIN_STATUS_INVALID);
        }

        String pluginId = pluginInfoDo.getPluginId();
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        if (plugin != null) {
            if (status.equals(IotPluginStatusEnum.RUNNING.getStatus()) && plugin.getPluginState() != PluginState.STARTED) {
                // 启动插件
                pluginManager.startPlugin(pluginId);
            } else if (status.equals(IotPluginStatusEnum.STOPPED.getStatus()) && plugin.getPluginState() == PluginState.STARTED) {
                // 停止插件
                pluginManager.stopPlugin(pluginId);
            }
        } else {
            // 已经停止，未获取到插件
            if (IotPluginStatusEnum.STOPPED.getStatus().equals(pluginInfoDo.getStatus())) {
                throw exception(PLUGIN_STATUS_INVALID);
            }
        }
        pluginInfoDo.setStatus(status);
        pluginInfoMapper.updateById(pluginInfoDo);
    }

//    @PostConstruct
//    public void init() {
//        Executors.newSingleThreadScheduledExecutor().schedule(this::startPlugins, 3, TimeUnit.SECONDS);
//    }
//
//    @SneakyThrows
//    private void startPlugins() {
//        for (PluginInfoDO pluginInfoDO : pluginInfoMapper.selectList()) {
//            if (!IotPluginStatusEnum.RUNNING.getStatus().equals(pluginInfoDO.getStatus())) {
//                continue;
//            }
//            log.info("start plugin:{}", pluginInfoDO.getPluginId());
//            try {
//                pluginManager.startPlugin(pluginInfoDO.getPluginId());
//            } catch (Exception e) {
//                log.error("start plugin error", e);
//            }
//        }
//    }

}
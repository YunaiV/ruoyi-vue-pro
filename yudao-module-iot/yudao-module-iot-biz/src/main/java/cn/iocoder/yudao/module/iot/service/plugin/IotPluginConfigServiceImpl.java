package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.config.PluginConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.config.PluginConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.IotPluginConfigMapper;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
/**
 * IoT 插件配置 Service 实现类
 *
 * @author haohao
 */
@Service
@Validated
@Slf4j
public class IotPluginConfigServiceImpl implements IotPluginConfigService {

    @Resource
    private IotPluginConfigMapper pluginConfigMapper;

    @Resource
    private IotPluginInstanceService pluginInstanceService;

    @Resource
    private SpringPluginManager springPluginManager;

    @Override
    public Long createPluginConfig(PluginConfigSaveReqVO createReqVO) {
        // 1. 校验插件标识唯一性：确保没有其他配置使用相同的 pluginKey（新建时 id 为 null）
        validatePluginKeyUnique(null, createReqVO.getPluginKey());
        IotPluginConfigDO pluginConfig = BeanUtils.toBean(createReqVO, IotPluginConfigDO.class);
        // 2. 插入插件配置到数据库
        pluginConfigMapper.insert(pluginConfig);
        return pluginConfig.getId();
    }

    @Override
    public void updatePluginConfig(PluginConfigSaveReqVO updateReqVO) {
        // 1. 校验插件配置是否存在：根据传入 ID 判断记录是否存在
        validatePluginConfigExists(updateReqVO.getId());
        // 2. 校验插件标识唯一性：确保更新后的 pluginKey 没有被其他记录占用
        validatePluginKeyUnique(updateReqVO.getId(), updateReqVO.getPluginKey());
        // 3. 将更新请求对象转换为插件配置数据对象
        IotPluginConfigDO updateObj = BeanUtils.toBean(updateReqVO, IotPluginConfigDO.class);
        pluginConfigMapper.updateById(updateObj);
    }

    /**
     * 校验插件标识唯一性
     *
     * @param id        当前插件配置的 ID（如果为 null 则说明为新建操作）
     * @param pluginKey 待校验的插件标识
     */
    private void validatePluginKeyUnique(Long id, String pluginKey) {
        // 1. 根据 pluginKey 从数据库中查询已有的插件配置
        IotPluginConfigDO pluginConfig = pluginConfigMapper.selectByPluginKey(pluginKey);
        // 2. 如果查询到记录且记录的 ID 与当前 ID 不相同，则认为存在重复，抛出异常
        if (pluginConfig != null && !pluginConfig.getId().equals(id)) {
            throw exception(PLUGIN_CONFIG_KEY_DUPLICATE);
        }
    }

    @Override
    public void deletePluginConfig(Long id) {
        // 1. 校验存在
        IotPluginConfigDO pluginConfigDO = validatePluginConfigExists(id);
        // 2. 未开启状态，才允许删除
        if (IotPluginStatusEnum.RUNNING.getStatus().equals(pluginConfigDO.getStatus())) {
            throw exception(PLUGIN_CONFIG_DELETE_FAILED_RUNNING);
        }

        // 3. 卸载插件
        pluginInstanceService.stopAndUnloadPlugin(pluginConfigDO.getPluginKey());
        // 4. 删除插件文件
        pluginInstanceService.deletePluginFile(pluginConfigDO);

        // 5. 删除插件配置
        pluginConfigMapper.deleteById(id);
    }

    /**
     * 校验插件配置是否存在
     *
     * @param id 插件配置编号
     * @return 插件配置
     */
    private IotPluginConfigDO validatePluginConfigExists(Long id) {
        IotPluginConfigDO pluginConfig = pluginConfigMapper.selectById(id);
        if (pluginConfig == null) {
            throw exception(PLUGIN_CONFIG_NOT_EXISTS);
        }
        return pluginConfig;
    }

    @Override
    public IotPluginConfigDO getPluginConfig(Long id) {
        return pluginConfigMapper.selectById(id);
    }

    @Override
    public PageResult<IotPluginConfigDO> getPluginConfigPage(PluginConfigPageReqVO pageReqVO) {
        return pluginConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public void uploadFile(Long id, MultipartFile file) {
        // 1. 校验插件配置是否存在
        IotPluginConfigDO pluginConfigDO = validatePluginConfigExists(id);

        // 2.1 停止并卸载旧的插件
        pluginInstanceService.stopAndUnloadPlugin(pluginConfigDO.getPluginKey());
        // 2.2 上传新的插件文件，更新插件启用状态文件
        String pluginKeyNew = pluginInstanceService.uploadAndLoadNewPlugin(file);

        // 3. 校验 file 相关参数，是否完整
        validatePluginConfigFile(pluginKeyNew);

        // 4. 更新插件配置
        IotPluginConfigDO updatedPluginConfig = new IotPluginConfigDO()
                .setId(pluginConfigDO.getId())
                .setPluginKey(pluginKeyNew)
                .setStatus(IotPluginStatusEnum.STOPPED.getStatus()) // TODO @haohao：这个状态，是不是非 stop 哈？
                .setFileName(file.getOriginalFilename())
                .setScript("") // TODO @haohao：这个设置为 "" 会不会覆盖数据里的哈？应该从插件里读取？未来？
                .setConfigSchema(springPluginManager.getPlugin(pluginKeyNew).getDescriptor().getPluginDescription())
                .setVersion(springPluginManager.getPlugin(pluginKeyNew).getDescriptor().getVersion())
                .setDescription(springPluginManager.getPlugin(pluginKeyNew).getDescriptor().getPluginDescription());
        pluginConfigMapper.updateById(updatedPluginConfig);
    }

    /**
     * 校验 file 相关参数
     *
     * @param pluginKeyNew 插件标识符
     */
    private void validatePluginConfigFile(String pluginKeyNew) {
        // TODO @haohao：校验 file 相关参数，是否完整，类似：version 之类是不是可以解析到
        PluginWrapper plugin = springPluginManager.getPlugin(pluginKeyNew);
        if (plugin == null) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }
        if (plugin.getDescriptor().getVersion() == null) {
            throw exception(PLUGIN_INSTALL_FAILED);
        }
    }

    @Override
    public void updatePluginStatus(Long id, Integer status) {
        // 1. 校验插件配置是否存在
        IotPluginConfigDO pluginConfigDo = validatePluginConfigExists(id);

        // 2. 更新插件状态
        pluginInstanceService.updatePluginStatus(pluginConfigDo, status);

        // 3. 更新数据库中的插件状态
        pluginConfigMapper.updateById(new IotPluginConfigDO().setId(id).setStatus(status));
    }

    @Override
    public List<IotPluginConfigDO> getPluginConfigList() {
        return pluginConfigMapper.selectList();
    }

    @Override
    public List<IotPluginConfigDO> getPluginConfigListByStatusAndDeployType(Integer status, Integer deployType) {
        return pluginConfigMapper.selectListByStatusAndDeployType(status, deployType);
    }

    @Override
    public IotPluginConfigDO getPluginConfigByPluginKey(String pluginKey) {
        return pluginConfigMapper.selectByPluginKey(pluginKey);
    }

}
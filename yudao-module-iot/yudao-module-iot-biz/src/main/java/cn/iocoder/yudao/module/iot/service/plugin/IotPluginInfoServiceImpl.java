package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.info.PluginInfoPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.info.PluginInfoSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugin.IotPluginInfoMapper;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PLUGIN_INFO_DELETE_FAILED_RUNNING;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PLUGIN_INFO_NOT_EXISTS;

/**
 * IoT 插件信息 Service 实现类
 *
 * @author haohao
 */
@Service
@Validated
@Slf4j
public class IotPluginInfoServiceImpl implements IotPluginInfoService {

    @Resource
    private IotPluginInfoMapper pluginInfoMapper;

    @Resource
    private IotPluginInstanceService pluginInstanceService;

    @Resource
    private SpringPluginManager springPluginManager;

    @Override
    public Long createPluginInfo(PluginInfoSaveReqVO createReqVO) {
        // TODO @haohao：pluginKey 唯一值
        IotPluginInfoDO pluginInfo = BeanUtils.toBean(createReqVO, IotPluginInfoDO.class);
        pluginInfoMapper.insert(pluginInfo);
        return pluginInfo.getId();
    }

    @Override
    public void updatePluginInfo(PluginInfoSaveReqVO updateReqVO) {
        // 校验存在
        validatePluginInfoExists(updateReqVO.getId());
        // 更新
        IotPluginInfoDO updateObj = BeanUtils.toBean(updateReqVO, IotPluginInfoDO.class);
        pluginInfoMapper.updateById(updateObj);
    }

    @Override
    public void deletePluginInfo(Long id) {
        // 1.1 校验存在
        IotPluginInfoDO pluginInfoDO = validatePluginInfoExists(id);
        // 1.2 未开启状态，才允许删除
        if (IotPluginStatusEnum.RUNNING.getStatus().equals(pluginInfoDO.getStatus())) {
            throw exception(PLUGIN_INFO_DELETE_FAILED_RUNNING);
        }

        // 2.1 卸载插件
        pluginInstanceService.stopAndUnloadPlugin(pluginInfoDO.getPluginKey());
        // 2.2 删除插件文件
        pluginInstanceService.deletePluginFile(pluginInfoDO);

        // 3. 删除插件信息
        pluginInfoMapper.deleteById(id);
    }

    private IotPluginInfoDO validatePluginInfoExists(Long id) {
        IotPluginInfoDO pluginInfo = pluginInfoMapper.selectById(id);
        if (pluginInfo == null) {
            throw exception(PLUGIN_INFO_NOT_EXISTS);
        }
        return pluginInfo;
    }

    @Override
    public IotPluginInfoDO getPluginInfo(Long id) {
        return pluginInfoMapper.selectById(id);
    }

    @Override
    public PageResult<IotPluginInfoDO> getPluginInfoPage(PluginInfoPageReqVO pageReqVO) {
        return pluginInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public void uploadFile(Long id, MultipartFile file) {
        // 1. 校验插件信息是否存在
        IotPluginInfoDO pluginInfoDo = validatePluginInfoExists(id);
        // TODO @haohao：最好校验下 file 相关参数，是否完整，类似：version 之类是不是可以解析到

        // 2.1 停止并卸载旧的插件
        pluginInstanceService.stopAndUnloadPlugin(pluginInfoDo.getPluginKey());
        // 2.2 上传新的插件文件，更新插件启用状态文件
        String pluginKeyNew = pluginInstanceService.uploadAndLoadNewPlugin(file);

        // 3. 更新插件信息
        updatePluginInfo(pluginInfoDo, pluginKeyNew, file);
    }

    // TODO @haohao：这个方法，要不合并到 uploadFile 里；
    /**
     * 更新插件信息
     *
     * @param pluginInfoDo 插件信息
     * @param pluginKeyNew 插件标识符
     * @param file         文件
     */
    private void updatePluginInfo(IotPluginInfoDO pluginInfoDo, String pluginKeyNew, MultipartFile file) {
        IotPluginInfoDO updatedPluginInfo = new IotPluginInfoDO()
                .setId(pluginInfoDo.getId())
                .setPluginKey(pluginKeyNew)
                .setStatus(IotPluginStatusEnum.STOPPED.getStatus()) // TODO @haohao：这个状态，是不是非 stop 哈？
                .setFileName(file.getOriginalFilename())
                .setScript("") // TODO @haohao：这个设置为 "" 会不会覆盖数据里的哈？应该从插件里读取？未来？
                .setConfigSchema(springPluginManager.getPlugin(pluginKeyNew).getDescriptor().getPluginDescription())
                .setVersion(springPluginManager.getPlugin(pluginKeyNew).getDescriptor().getVersion())
                .setDescription(springPluginManager.getPlugin(pluginKeyNew).getDescriptor().getPluginDescription());
        pluginInfoMapper.updateById(updatedPluginInfo);
    }

    @Override
    public void updatePluginStatus(Long id, Integer status) {
        // 1. 校验插件信息是否存在
        IotPluginInfoDO pluginInfoDo = validatePluginInfoExists(id);

        // 2. 更新插件状态
        pluginInstanceService.updatePluginStatus(pluginInfoDo, status);

        // 3. 更新数据库中的插件状态
        pluginInfoMapper.updateById(new IotPluginInfoDO().setId(id).setStatus(status));
    }

    @Override
    public List<IotPluginInfoDO> getPluginInfoList() {
        return pluginInfoMapper.selectList();
    }

    @Override
    public List<IotPluginInfoDO> getPluginInfoListByStatus(Integer status) {
        return pluginInfoMapper.selectListByStatus(status);
    }

    @Override
    public IotPluginInfoDO getPluginInfoByPluginKey(String pluginKey) {
        return pluginInfoMapper.selectByPluginKey(pluginKey);
    }

}
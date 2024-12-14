package cn.iocoder.yudao.module.iot.service.plugininstance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugininstance.vo.PluginInstancePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugininstance.vo.PluginInstanceSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance.PluginInstanceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugininstance.PluginInstanceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PLUGIN_INSTANCE_NOT_EXISTS;

/**
 * IoT 插件实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PluginInstanceServiceImpl implements PluginInstanceService {

    @Resource
    private PluginInstanceMapper pluginInstanceMapper;

    @Override
    public Long createPluginInstance(PluginInstanceSaveReqVO createReqVO) {
        // 插入
        PluginInstanceDO pluginInstance = BeanUtils.toBean(createReqVO, PluginInstanceDO.class);
        pluginInstanceMapper.insert(pluginInstance);
        // 返回
        return pluginInstance.getId();
    }

    @Override
    public void updatePluginInstance(PluginInstanceSaveReqVO updateReqVO) {
        // 校验存在
        validatePluginInstanceExists(updateReqVO.getId());
        // 更新
        PluginInstanceDO updateObj = BeanUtils.toBean(updateReqVO, PluginInstanceDO.class);
        pluginInstanceMapper.updateById(updateObj);
    }

    @Override
    public void deletePluginInstance(Long id) {
        // 校验存在
        validatePluginInstanceExists(id);
        // 删除
        pluginInstanceMapper.deleteById(id);
    }

    private void validatePluginInstanceExists(Long id) {
        if (pluginInstanceMapper.selectById(id) == null) {
            throw exception(PLUGIN_INSTANCE_NOT_EXISTS);
        }
    }

    @Override
    public PluginInstanceDO getPluginInstance(Long id) {
        return pluginInstanceMapper.selectById(id);
    }

    @Override
    public PageResult<PluginInstanceDO> getPluginInstancePage(PluginInstancePageReqVO pageReqVO) {
        return pluginInstanceMapper.selectPage(pageReqVO);
    }

}
package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInstancePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInstanceSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance.PluginInstanceDO;
import jakarta.validation.Valid;

/**
 * IoT 插件实例 Service 接口
 *
 * @author 芋道源码
 */
public interface PluginInstanceService {

    /**
     * 创建IoT 插件实例
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPluginInstance(@Valid PluginInstanceSaveReqVO createReqVO);

    /**
     * 更新IoT 插件实例
     *
     * @param updateReqVO 更新信息
     */
    void updatePluginInstance(@Valid PluginInstanceSaveReqVO updateReqVO);

    /**
     * 删除IoT 插件实例
     *
     * @param id 编号
     */
    void deletePluginInstance(Long id);

    /**
     * 获得IoT 插件实例
     *
     * @param id 编号
     * @return IoT 插件实例
     */
    PluginInstanceDO getPluginInstance(Long id);

    /**
     * 获得IoT 插件实例分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 插件实例分页
     */
    PageResult<PluginInstanceDO> getPluginInstancePage(PluginInstancePageReqVO pageReqVO);

}
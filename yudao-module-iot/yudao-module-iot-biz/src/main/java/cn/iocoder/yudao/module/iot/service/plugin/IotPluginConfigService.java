package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.config.PluginConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.config.PluginConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginConfigDO;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginDeployTypeEnum;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * IoT 插件配置 Service 接口
 *
 * @author haohao
 */
public interface IotPluginConfigService {

    /**
     * 创建插件配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPluginConfig(@Valid PluginConfigSaveReqVO createReqVO);

    /**
     * 更新插件配置
     *
     * @param updateReqVO 更新信息
     */
    void updatePluginConfig(@Valid PluginConfigSaveReqVO updateReqVO);

    /**
     * 删除插件配置
     *
     * @param id 编号
     */
    void deletePluginConfig(Long id);

    /**
     * 获得插件配置
     *
     * @param id 编号
     * @return 插件配置
     */
    IotPluginConfigDO getPluginConfig(Long id);

    /**
     * 获得插件配置分页
     *
     * @param pageReqVO 分页查询
     * @return 插件配置分页
     */
    PageResult<IotPluginConfigDO> getPluginConfigPage(PluginConfigPageReqVO pageReqVO);

    /**
     * 上传插件的 JAR 包
     *
     * @param id   插件id
     * @param file 文件
     */
    void uploadFile(Long id, MultipartFile file);

    /**
     * 更新插件的状态
     *
     * @param id     插件id
     * @param status 状态 {@link IotPluginStatusEnum}
     */
    void updatePluginStatus(Long id, Integer status);

    /**
     * 获得插件配置列表
     *
     * @return 插件配置列表
     */
    List<IotPluginConfigDO> getPluginConfigList();

    /**
     * 根据状态和部署类型获得插件配置列表
     *
     * @param status     状态 {@link IotPluginStatusEnum}
     * @param deployType 部署类型 {@link IotPluginDeployTypeEnum}
     * @return 插件配置列表
     */
    List<IotPluginConfigDO> getPluginConfigListByStatusAndDeployType(Integer status, Integer deployType);

    /**
     * 根据插件包标识符获取插件配置
     *
     * @param pluginKey 插件包标识符
     * @return 插件配置
     */
    IotPluginConfigDO getPluginConfigByPluginKey(@NotEmpty(message = "插件包标识符不能为空") String pluginKey);

}

package cn.iocoder.yudao.module.iot.service.alert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config.IotAlertConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config.IotAlertConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 告警配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAlertConfigService {

    /**
     * 创建告警配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAlertConfig(@Valid IotAlertConfigSaveReqVO createReqVO);

    /**
     * 更新告警配置
     *
     * @param updateReqVO 更新信息
     */
    void updateAlertConfig(@Valid IotAlertConfigSaveReqVO updateReqVO);

    /**
     * 删除告警配置
     *
     * @param id 编号
     */
    void deleteAlertConfig(Long id);

    /**
     * 获得告警配置
     *
     * @param id 编号
     * @return 告警配置
     */
    IotAlertConfigDO getAlertConfig(Long id);

    /**
     * 获得告警配置分页
     *
     * @param pageReqVO 分页查询
     * @return 告警配置分页
     */
    PageResult<IotAlertConfigDO> getAlertConfigPage(IotAlertConfigPageReqVO pageReqVO);

    /**
     * 获得告警配置列表
     *
     * @param status 状态
     * @return 告警配置列表
     */
    List<IotAlertConfigDO> getAlertConfigListByStatus(Integer status);

    /**
     * 获得告警配置列表
     *
     * @param sceneRuleId 场景流动规则编号
     * @return 告警配置列表
     */
    List<IotAlertConfigDO> getAlertConfigListBySceneRuleIdAndStatus(Long sceneRuleId, Integer status);

}
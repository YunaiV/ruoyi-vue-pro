package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * IoT 规则场景规则 Service 接口
 *
 * @author 芋道源码
 */
public interface IotSceneRuleService {

    /**
     * 创建场景联动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSceneRule(@Valid IotSceneRuleSaveReqVO createReqVO);

    /**
     * 更新场景联动
     *
     * @param updateReqVO 更新信息
     */
    void updateSceneRule(@Valid IotSceneRuleSaveReqVO updateReqVO);

    /**
     * 更新场景联动状态
     *
     * @param id     场景联动编号
     * @param status 状态
     */
    void updateSceneRuleStatus(Long id, Integer status);

    /**
     * 删除场景联动
     *
     * @param id 编号
     */
    void deleteSceneRule(Long id);

    /**
     * 获得场景联动
     *
     * @param id 编号
     * @return 场景联动
     */
    IotSceneRuleDO getSceneRule(Long id);

    /**
     * 获得场景联动分页
     *
     * @param pageReqVO 分页查询
     * @return 场景联动分页
     */
    PageResult<IotSceneRuleDO> getSceneRulePage(IotSceneRulePageReqVO pageReqVO);

    /**
     * 校验规则场景联动规则编号们是否存在。如下情况，视为无效：
     * 1. 规则场景联动规则编号不存在
     *
     * @param ids 场景联动规则编号数组
     */
    void validateSceneRuleList(Collection<Long> ids);

    /**
     * 获得指定状态的场景联动列表
     *
     * @param status 状态
     * @return 场景联动列表
     */
    List<IotSceneRuleDO> getSceneRuleListByStatus(Integer status);

    /**
     * 【缓存】获得指定设备的场景列表
     *
     * @param productId 产品 ID
     * @param deviceId  设备 ID
     * @return 场景列表
     */
    List<IotSceneRuleDO> getSceneRuleListByProductIdAndDeviceIdFromCache(Long productId, Long deviceId);

    /**
     * 基于 {@link IotSceneRuleTriggerTypeEnum} 场景，执行规则场景
     * 1. {@link IotSceneRuleTriggerTypeEnum#DEVICE_STATE_UPDATE}
     * 2. {@link IotSceneRuleTriggerTypeEnum#DEVICE_PROPERTY_POST}
     *    {@link IotSceneRuleTriggerTypeEnum#DEVICE_EVENT_POST}
     * 3. {@link IotSceneRuleTriggerTypeEnum#DEVICE_EVENT_POST}
     *    {@link IotSceneRuleTriggerTypeEnum#DEVICE_SERVICE_INVOKE}
     * @param message 消息
     */
    void executeSceneRuleByDevice(IotDeviceMessage message);

    /**
     * 基于 {@link IotSceneRuleTriggerTypeEnum#TIMER} 场景，执行规则场景
     *
     * @param id 场景联动规则编号
     */
    void executeSceneRuleByTimer(Long id);

}

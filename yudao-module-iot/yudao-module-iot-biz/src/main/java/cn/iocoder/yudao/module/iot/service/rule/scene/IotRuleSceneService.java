package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * IoT 规则场景规则 Service 接口
 *
 * @author 芋道源码
 */
public interface IotRuleSceneService {

    /**
     * 创建场景联动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRuleScene(@Valid IotRuleSceneSaveReqVO createReqVO);

    /**
     * 更新场景联动
     *
     * @param updateReqVO 更新信息
     */
    void updateRuleScene(@Valid IotRuleSceneSaveReqVO updateReqVO);

    /**
     * 更新场景联动状态
     *
     * @param id     场景联动编号
     * @param status 状态
     */
    void updateRuleSceneStatus(Long id, Integer status);

    /**
     * 删除场景联动
     *
     * @param id 编号
     */
    void deleteRuleScene(Long id);

    /**
     * 获得场景联动
     *
     * @param id 编号
     * @return 场景联动
     */
    IotSceneRuleDO getRuleScene(Long id);

    /**
     * 获得场景联动分页
     *
     * @param pageReqVO 分页查询
     * @return 场景联动分页
     */
    PageResult<IotSceneRuleDO> getRuleScenePage(IotRuleScenePageReqVO pageReqVO);

    /**
     * 校验规则场景联动规则编号们是否存在。如下情况，视为无效：
     * 1. 规则场景联动规则编号不存在
     *
     * @param ids 场景联动规则编号数组
     */
    void validateRuleSceneList(Collection<Long> ids);

    /**
     * 获得指定状态的场景联动列表
     *
     * @param status 状态
     * @return 场景联动列表
     */
    List<IotSceneRuleDO> getRuleSceneListByStatus(Integer status);

    /**
     * 【缓存】获得指定设备的场景列表
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 场景列表
     */
    List<IotSceneRuleDO> getRuleSceneListByProductKeyAndDeviceNameFromCache(String productKey, String deviceName);

    /**
     * 基于 {@link IotRuleSceneTriggerTypeEnum#DEVICE} 场景，执行规则场景
     *
     * @param message 消息
     */
    void executeRuleSceneByDevice(IotDeviceMessage message);

    /**
     * 基于 {@link IotRuleSceneTriggerTypeEnum#TIMER} 场景，执行规则场景
     *
     * @param id 场景联动规则编号
     */
    void executeRuleSceneByTimer(Long id);

}

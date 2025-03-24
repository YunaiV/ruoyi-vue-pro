package cn.iocoder.yudao.module.iot.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 规则场景 Service 接口
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
    IotRuleSceneDO getRuleScene(Long id);

    /**
     * 获得场景联动分页
     *
     * @param pageReqVO 分页查询
     * @return 场景联动分页
     */
    PageResult<IotRuleSceneDO> getRuleScenePage(IotRuleScenePageReqVO pageReqVO);

    /**
     * 【缓存】获得指定设备的场景列表
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 场景列表
     */
    List<IotRuleSceneDO> getRuleSceneListByProductKeyAndDeviceNameFromCache(String productKey, String deviceName);

    /**
     * 基于 {@link IotRuleSceneTriggerTypeEnum#DEVICE} 场景，执行规则场景
     *
     * @param message 消息
     */
    void executeRuleSceneByDevice(IotDeviceMessage message);

    /**
     * 基于 {@link IotRuleSceneTriggerTypeEnum#TIMER} 场景，执行规则场景
     *
     * @param id 场景编号
     */
    void executeRuleSceneByTimer(Long id);

    /**
     * TODO 芋艿：测试方法，需要删除
     */
    void test();

}

package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotSceneRuleMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.scene.action.IotSceneRuleAction;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherManager;
import cn.iocoder.yudao.module.iot.service.rule.scene.timer.IotSceneRuleTimerHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.RULE_SCENE_NOT_EXISTS;

/**
 * IoT 规则场景 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotSceneRuleServiceImpl implements IotSceneRuleService {

    @Resource
    private IotSceneRuleMapper sceneRuleMapper;

    @Resource
    private IotProductService productService;
    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotSceneRuleMatcherManager sceneRuleMatcherManager;
    @Resource
    private List<IotSceneRuleAction> sceneRuleActions;
    @Resource
    private IotSceneRuleTimerHandler timerHandler;

    @Override
    @CacheEvict(value = RedisKeyConstants.SCENE_RULE_LIST, allEntries = true)
    public Long createSceneRule(IotSceneRuleSaveReqVO createReqVO) {
        IotSceneRuleDO sceneRule = BeanUtils.toBean(createReqVO, IotSceneRuleDO.class);
        sceneRuleMapper.insert(sceneRule);

        // 注册定时触发器
        timerHandler.registerTimerTriggers(sceneRule);

        return sceneRule.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.SCENE_RULE_LIST, allEntries = true)
    public void updateSceneRule(IotSceneRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateSceneRuleExists(updateReqVO.getId());
        // 更新
        IotSceneRuleDO updateObj = BeanUtils.toBean(updateReqVO, IotSceneRuleDO.class);
        sceneRuleMapper.updateById(updateObj);

        // 更新定时触发器
        timerHandler.updateTimerTriggers(updateObj);
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.SCENE_RULE_LIST, allEntries = true)
    public void updateSceneRuleStatus(Long id, Integer status) {
        // 1. 校验存在
        validateSceneRuleExists(id);

        // 2. 更新状态
        IotSceneRuleDO updateObj = new IotSceneRuleDO().setId(id).setStatus(status);
        sceneRuleMapper.updateById(updateObj);

        // 3. 根据状态管理定时触发器
        if (CommonStatusEnum.isEnable(status)) {
            // 启用时，获取完整的场景规则信息并注册定时触发器
            IotSceneRuleDO sceneRule = sceneRuleMapper.selectById(id);
            if (sceneRule != null) {
                timerHandler.registerTimerTriggers(sceneRule);
            }
        } else {
            // 禁用时，暂停定时触发器
            timerHandler.pauseTimerTriggers(id);
        }
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.SCENE_RULE_LIST, allEntries = true)
    public void deleteSceneRule(Long id) {
        // 1. 校验存在
        validateSceneRuleExists(id);

        // 2. 删除
        sceneRuleMapper.deleteById(id);

        // 3. 删除定时触发器
        timerHandler.unregisterTimerTriggers(id);
    }

    private void validateSceneRuleExists(Long id) {
        if (sceneRuleMapper.selectById(id) == null) {
            throw exception(RULE_SCENE_NOT_EXISTS);
        }
    }

    @Override
    public IotSceneRuleDO getSceneRule(Long id) {
        return sceneRuleMapper.selectById(id);
    }

    @Override
    public PageResult<IotSceneRuleDO> getSceneRulePage(IotSceneRulePageReqVO pageReqVO) {
        return sceneRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public void validateSceneRuleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量查询存在的规则场景
        List<IotSceneRuleDO> existingScenes = sceneRuleMapper.selectByIds(ids);
        if (existingScenes.size() != ids.size()) {
            throw exception(RULE_SCENE_NOT_EXISTS);
        }
    }

    @Override
    public List<IotSceneRuleDO> getSceneRuleListByStatus(Integer status) {
        return sceneRuleMapper.selectListByStatus(status);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.SCENE_RULE_LIST, key = "#productId + '_' + #deviceId ")
    @TenantIgnore // 忽略租户隔离：因为 IotSceneRuleMessageHandler 调用时，一般未传递租户，所以需要忽略
    public List<IotSceneRuleDO> getSceneRuleListByProductIdAndDeviceIdFromCache(Long productId, Long deviceId) {
        // 1. 查询启用状态的规则场景
        List<IotSceneRuleDO> enabledList = sceneRuleMapper.selectList(IotSceneRuleDO::getStatus, CommonStatusEnum.ENABLE.getStatus());

        // 2. 根据 productKey 和 deviceName 进行匹配
        return filterList(enabledList, sceneRule -> {
            if (CollUtil.isEmpty(sceneRule.getTriggers())) {
                return false;
            }

            for (IotSceneRuleDO.Trigger trigger : sceneRule.getTriggers()) {
                // 检查触发器是否匹配指定的产品和设备
                try {
                    // 检查产品是否匹配
                    if (trigger.getProductId() == null || trigger.getDeviceId() == null) {
                        return false;
                    }
                    // 检查是否是全部设备的特殊标识
                    if (IotDeviceDO.DEVICE_ID_ALL.equals(trigger.getDeviceId())) {
                        return true;
                    }
                    // 检查具体设备 ID 是否匹配
                    return ObjUtil.equal(productId, trigger.getProductId()) && ObjUtil.equal(deviceId, trigger.getDeviceId());
                } catch (Exception e) {
                    log.warn("[getSceneRuleListByProductIdAndDeviceIdFromCache][产品({}) 设备({}) 匹配触发器异常]",
                            productId, deviceId, e);
                    return false;
                }
            }
            return false;
        });
    }

    @Override
    public void executeSceneRuleByDevice(IotDeviceMessage message) {
        // 1.1 这里的 tenantId，通过设备获取；
        IotDeviceDO device = deviceService.getDeviceFromCache(message.getDeviceId());
        TenantUtils.execute(device.getTenantId(), () -> {
            // 1.2 获得设备匹配的规则场景
            List<IotSceneRuleDO> sceneRules = getMatchedSceneRuleListByMessage(message);
            if (CollUtil.isEmpty(sceneRules)) {
                return;
            }

            // 2. 执行规则场景
            executeSceneRuleAction(message, sceneRules);
        });
    }

    @Override
    public void executeSceneRuleByTimer(Long id) {
        // 1.1 获得规则场景
        IotSceneRuleDO scene = TenantUtils.executeIgnore(() -> sceneRuleMapper.selectById(id));
        if (scene == null) {
            log.error("[executeSceneRuleByTimer][规则场景({}) 不存在]", id);
            return;
        }
        if (CommonStatusEnum.isDisable(scene.getStatus())) {
            log.info("[executeSceneRuleByTimer][规则场景({}) 已被禁用]", id);
            return;
        }
        // 1.2 判断是否有定时触发器，避免脏数据
        IotSceneRuleDO.Trigger config = CollUtil.findOne(scene.getTriggers(),
                trigger -> ObjUtil.equals(trigger.getType(), IotSceneRuleTriggerTypeEnum.TIMER.getType()));
        if (config == null) {
            log.error("[executeSceneRuleByTimer][规则场景({}) 不存在定时触发器]", scene);
            return;
        }

        // 2. 执行规则场景
        TenantUtils.execute(scene.getTenantId(),
                () -> executeSceneRuleAction(null, ListUtil.toList(scene)));
    }

    /**
     * 基于消息，获得匹配的规则场景列表
     *
     * @param message 设备消息
     * @return 规则场景列表
     */
    private List<IotSceneRuleDO> getMatchedSceneRuleListByMessage(IotDeviceMessage message) {
        // 1.1 通过 deviceId 获取设备信息
        IotDeviceDO device = getSelf().deviceService.getDeviceFromCache(message.getDeviceId());
        if (device == null) {
            log.warn("[getMatchedSceneRuleListByMessage][设备({}) 不存在]", message.getDeviceId());
            return ListUtil.of();
        }
        // 1.2 通过 productId 获取产品信息
        IotProductDO product = getSelf().productService.getProductFromCache(device.getProductId());
        if (product == null) {
            log.warn("[getMatchedSceneRuleListByMessage][产品({}) 不存在]", device.getProductId());
            return ListUtil.of();
        }
        // 1.3 获取匹配的规则场景
        List<IotSceneRuleDO> sceneRules = getSelf().getSceneRuleListByProductIdAndDeviceIdFromCache(
                product.getId(), device.getId());
        if (CollUtil.isEmpty(sceneRules)) {
            return sceneRules;
        }

        // 2. 使用重构后的触发器匹配逻辑
        return filterList(sceneRules, sceneRule -> matchSceneRuleTriggers(message, sceneRule));
    }

    /**
     * 匹配场景规则的所有触发器
     *
     * @param message   设备消息
     * @param sceneRule 场景规则
     * @return 是否匹配
     */
    private boolean matchSceneRuleTriggers(IotDeviceMessage message, IotSceneRuleDO sceneRule) {
        if (CollUtil.isEmpty(sceneRule.getTriggers())) {
            log.debug("[matchSceneRuleTriggers][规则场景({}) 没有配置触发器]", sceneRule.getId());
            return false;
        }

        for (IotSceneRuleDO.Trigger trigger : sceneRule.getTriggers()) {
            if (matchSingleTrigger(message, trigger, sceneRule)) {
                log.info("[matchSceneRuleTriggers][消息({}) 匹配到规则场景编号({}) 的触发器({})]",
                        message.getRequestId(), sceneRule.getId(), trigger.getType());
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配单个触发器
     *
     * @param message   设备消息
     * @param trigger   触发器
     * @param sceneRule 场景规则（用于日志）
     * @return 是否匹配
     */
    private boolean matchSingleTrigger(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger, IotSceneRuleDO sceneRule) {
        try {
            return sceneRuleMatcherManager.isMatched(message, trigger) && isTriggerConditionGroupsMatched(message, trigger, sceneRule);
        } catch (Exception e) {
            log.error("[matchSingleTrigger][触发器匹配异常] sceneRuleId: {}, triggerType: {}, message: {}",
                    sceneRule.getId(), trigger.getType(), message, e);
            return false;
        }
    }

    /**
     * 检查触发器的条件分组是否匹配
     *
     * @param message   设备消息
     * @param trigger   触发器
     * @param sceneRule 场景规则（用于日志）
     * @return 是否匹配
     */
    private boolean isTriggerConditionGroupsMatched(IotDeviceMessage message,
                                                    IotSceneRuleDO.Trigger trigger,
                                                    IotSceneRuleDO sceneRule) {
        // 1. 如果没有条件分组，则认为匹配成功（只依赖基础触发器匹配）
        if (CollUtil.isEmpty(trigger.getConditionGroups())) {
            return true;
        }

        // 2. 检查条件分组：分组与分组之间是"或"的关系，条件与条件之间是"且"的关系
        for (List<IotSceneRuleDO.TriggerCondition> conditionGroup : trigger.getConditionGroups()) {
            if (CollUtil.isEmpty(conditionGroup)) {
                continue;
            }
            // 检查当前分组中的所有条件是否都匹配（且关系）
            boolean allConditionsMatched = true;
            for (IotSceneRuleDO.TriggerCondition condition : conditionGroup) {
                if (!isTriggerConditionMatched(message, condition, sceneRule, trigger)) {
                    allConditionsMatched = false;
                    break;
                }
            }
            // 如果当前分组的所有条件都匹配，则整个触发器匹配成功
            if (allConditionsMatched) {
                return true;
            }
        }

        // 3. 所有分组都不匹配
        return false;
    }

    /**
     * 基于消息，判断触发器的子条件是否匹配
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @param sceneRule 规则场景（用于日志，无其它作用）
     * @param trigger   触发器（用于日志，无其它作用）
     * @return 是否匹配
     */
    private boolean isTriggerConditionMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition,
                                              IotSceneRuleDO sceneRule, IotSceneRuleDO.Trigger trigger) {
        try {
            return sceneRuleMatcherManager.isConditionMatched(message, condition);
        } catch (Exception e) {
            log.error("[isTriggerConditionMatched][规则场景编号({}) 的触发器({}) 条件匹配异常]",
                    sceneRule.getId(), trigger, e);
            return false;
        }
    }

    /**
     * 执行规则场景的动作
     *
     * @param message    设备消息
     * @param sceneRules 规则场景列表
     */
    private void executeSceneRuleAction(IotDeviceMessage message, List<IotSceneRuleDO> sceneRules) {
        // 1. 遍历规则场景
        sceneRules.forEach(sceneRule -> {
            // 2. 遍历规则场景的动作
            sceneRule.getActions().forEach(actionConfig -> {
                // 2.1 获取对应的动作 Action 数组
                List<IotSceneRuleAction> actions = filterList(sceneRuleActions,
                        action -> action.getType().getType().equals(actionConfig.getType()));
                if (CollUtil.isEmpty(actions)) {
                    return;
                }
                // 2.2 执行动作
                actions.forEach(action -> {
                    try {
                        action.execute(message, sceneRule, actionConfig);
                        log.info("[executeSceneRuleAction][消息({}) 规则场景编号({}) 的执行动作({}) 成功]",
                                message, sceneRule.getId(), actionConfig);
                    } catch (Exception e) {
                        log.error("[executeSceneRuleAction][消息({}) 规则场景编号({}) 的执行动作({}) 执行异常]",
                                message, sceneRule.getId(), actionConfig, e);
                    }
                });
            });
        });
    }

    private IotSceneRuleServiceImpl getSelf() {
        return SpringUtil.getBean(IotSceneRuleServiceImpl.class);
    }

}

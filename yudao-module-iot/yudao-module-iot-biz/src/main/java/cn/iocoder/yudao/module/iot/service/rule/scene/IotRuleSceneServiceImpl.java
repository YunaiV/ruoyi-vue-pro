package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotRuleSceneMapper;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.framework.job.core.IotSchedulerManager;
import cn.iocoder.yudao.module.iot.job.rule.IotRuleSceneJob;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.scene.action.IotSceneRuleAction;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
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
public class IotRuleSceneServiceImpl implements IotRuleSceneService {

    @Resource
    private IotRuleSceneMapper ruleSceneMapper;

    @Resource
    private List<IotSceneRuleAction> ruleSceneActions;

    @Resource(name = "iotSchedulerManager")
    private IotSchedulerManager schedulerManager;

    @Resource
    private IotProductService productService;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public Long createRuleScene(IotRuleSceneSaveReqVO createReqVO) {
        IotSceneRuleDO ruleScene = BeanUtils.toBean(createReqVO, IotSceneRuleDO.class);
        ruleSceneMapper.insert(ruleScene);
        return ruleScene.getId();
    }

    @Override
    public void updateRuleScene(IotRuleSceneSaveReqVO updateReqVO) {
        // 校验存在
        validateRuleSceneExists(updateReqVO.getId());
        // 更新
        IotSceneRuleDO updateObj = BeanUtils.toBean(updateReqVO, IotSceneRuleDO.class);
        ruleSceneMapper.updateById(updateObj);
    }

    @Override
    public void updateRuleSceneStatus(Long id, Integer status) {
        // 校验存在
        validateRuleSceneExists(id);
        // 更新状态
        IotSceneRuleDO updateObj = new IotSceneRuleDO().setId(id).setStatus(status);
        ruleSceneMapper.updateById(updateObj);
    }

    @Override
    public void deleteRuleScene(Long id) {
        // 校验存在
        validateRuleSceneExists(id);
        // 删除
        ruleSceneMapper.deleteById(id);
    }

    private void validateRuleSceneExists(Long id) {
        if (ruleSceneMapper.selectById(id) == null) {
            throw exception(RULE_SCENE_NOT_EXISTS);
        }
    }

    @Override
    public IotSceneRuleDO getRuleScene(Long id) {
        return ruleSceneMapper.selectById(id);
    }

    @Override
    public PageResult<IotSceneRuleDO> getRuleScenePage(IotRuleScenePageReqVO pageReqVO) {
        return ruleSceneMapper.selectPage(pageReqVO);
    }

    @Override
    public void validateRuleSceneList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量查询存在的规则场景
        List<IotSceneRuleDO> existingScenes = ruleSceneMapper.selectByIds(ids);
        if (existingScenes.size() != ids.size()) {
            throw exception(RULE_SCENE_NOT_EXISTS);
        }
    }

    @Override
    public List<IotSceneRuleDO> getRuleSceneListByStatus(Integer status) {
        return ruleSceneMapper.selectListByStatus(status);
    }

    // TODO 芋艿，缓存待实现
    @Override
    @TenantIgnore // 忽略租户隔离：因为 IotRuleSceneMessageHandler 调用时，一般未传递租户，所以需要忽略
    public List<IotSceneRuleDO> getRuleSceneListByProductKeyAndDeviceNameFromCache(String productKey, String deviceName) {
        // TODO @puhui999：一些注释，看看要不要优化下；
        // 注意：旧的测试代码已删除，因为使用了废弃的数据结构
        // 如需测试，请使用上面的新结构测试代码示例
        List<IotSceneRuleDO> list = ruleSceneMapper.selectList();
        // 只返回启用状态的规则场景
        List<IotSceneRuleDO> enabledList = filterList(list,
                ruleScene -> CommonStatusEnum.ENABLE.getStatus().equals(ruleScene.getStatus()));

        // 根据 productKey 和 deviceName 进行匹配
        return filterList(enabledList, ruleScene -> {
            if (CollUtil.isEmpty(ruleScene.getTriggers())) {
                return false;
            }

            for (IotSceneRuleDO.Trigger trigger : ruleScene.getTriggers()) {
                // 检查触发器是否匹配指定的产品和设备
                if (isMatchProductAndDevice(trigger, productKey, deviceName)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * 检查触发器是否匹配指定的产品和设备
     *
     * @param trigger    触发器
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 是否匹配
     */
    private boolean isMatchProductAndDevice(IotSceneRuleDO.Trigger trigger, String productKey, String deviceName) {
        try {
            // 1. 检查产品是否匹配
            if (trigger.getProductId() != null) {
                // 通过 productKey 获取产品信息
                IotProductDO product = productService.getProductByProductKey(productKey);
                if (product == null || !trigger.getProductId().equals(product.getId())) {
                    return false;
                }
            }

            // 2. 检查设备是否匹配
            if (trigger.getDeviceId() != null) {
                // 通过 productKey 和 deviceName 获取设备信息
                IotDeviceDO device = deviceService.getDeviceFromCache(productKey, deviceName);
                if (device == null) {
                    return false;
                }

                // 检查是否是全部设备的特殊标识
                if (IotDeviceDO.DEVICE_ID_ALL.equals(trigger.getDeviceId())) {
                    return true; // 匹配所有设备
                }

                // 检查具体设备ID是否匹配
                if (!trigger.getDeviceId().equals(device.getId())) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("[isMatchProductAndDevice][产品({}) 设备({}) 匹配触发器异常]", productKey, deviceName, e);
            return false;
        }
    }

    @Override
    public void executeRuleSceneByDevice(IotDeviceMessage message) {
        // TODO @芋艿：这里的 tenantId，通过设备获取；
        TenantUtils.execute(message.getTenantId(), () -> {
            // 1. 获得设备匹配的规则场景
            List<IotSceneRuleDO> ruleScenes = getMatchedRuleSceneListByMessage(message);
            if (CollUtil.isEmpty(ruleScenes)) {
                return;
            }

            // 2. 执行规则场景
            executeRuleSceneAction(message, ruleScenes);
        });
    }

    @Override
    public void executeRuleSceneByTimer(Long id) {
        // 1.1 获得规则场景
        IotSceneRuleDO scene = TenantUtils.executeIgnore(() -> ruleSceneMapper.selectById(id));
        if (scene == null) {
            log.error("[executeRuleSceneByTimer][规则场景({}) 不存在]", id);
            return;
        }
        if (CommonStatusEnum.isDisable(scene.getStatus())) {
            log.info("[executeRuleSceneByTimer][规则场景({}) 已被禁用]", id);
            return;
        }
        // 1.2 判断是否有定时触发器，避免脏数据
        IotSceneRuleDO.Trigger config = CollUtil.findOne(scene.getTriggers(),
                trigger -> ObjUtil.equals(trigger.getType(), IotRuleSceneTriggerTypeEnum.TIMER.getType()));
        if (config == null) {
            log.error("[executeRuleSceneByTimer][规则场景({}) 不存在定时触发器]", scene);
            return;
        }

        // 2. 执行规则场景
        TenantUtils.execute(scene.getTenantId(),
                () -> executeRuleSceneAction(null, ListUtil.toList(scene)));
    }

    /**
     * 基于消息，获得匹配的规则场景列表
     *
     * @param message 设备消息
     * @return 规则场景列表
     */
    private List<IotSceneRuleDO> getMatchedRuleSceneListByMessage(IotDeviceMessage message) {
        // 1. 匹配设备
        // TODO @芋艿：可能需要 getSelf(); 缓存
        // 1.1 通过 deviceId 获取设备信息
        IotDeviceDO device = deviceService.getDeviceFromCache(message.getDeviceId());
        if (device == null) {
            log.warn("[getMatchedRuleSceneListByMessage][设备({}) 不存在]", message.getDeviceId());
            return List.of();
        }

        // 1.2 通过 productId 获取产品信息
        IotProductDO product = productService.getProductFromCache(device.getProductId());
        if (product == null) {
            log.warn("[getMatchedRuleSceneListByMessage][产品({}) 不存在]", device.getProductId());
            return List.of();
        }

        // 1.3 获取匹配的规则场景
        List<IotSceneRuleDO> ruleScenes = getRuleSceneListByProductKeyAndDeviceNameFromCache(
                product.getProductKey(), device.getDeviceName());
        if (CollUtil.isEmpty(ruleScenes)) {
            return ruleScenes;
        }

        // 2. 匹配 trigger 触发器的条件
        return filterList(ruleScenes, ruleScene -> {
            for (IotSceneRuleDO.Trigger trigger : ruleScene.getTriggers()) {
                // 2.1 检查触发器类型，根据新的枚举值进行匹配
                // TODO @芋艿：需要根据新的触发器类型枚举进行适配
                // 原来使用 IotRuleSceneTriggerTypeEnum.DEVICE，新结构可能有不同的类型

                // 2.2 条件分组为空，说明没有匹配的条件，因此不匹配
                if (CollUtil.isEmpty(trigger.getConditionGroups())) {
                    return false;
                }

                // 2.3 检查条件分组：分组与分组之间是"或"的关系，条件与条件之间是"且"的关系
                boolean anyGroupMatched = false;
                for (List<IotSceneRuleDO.TriggerCondition> conditionGroup : trigger.getConditionGroups()) {
                    if (CollUtil.isEmpty(conditionGroup)) {
                        continue;
                    }

                    // 检查当前分组中的所有条件是否都匹配（且关系）
                    boolean allConditionsMatched = true;
                    for (IotSceneRuleDO.TriggerCondition condition : conditionGroup) {
                        // TODO @芋艿：这里需要实现具体的条件匹配逻辑
                        // 根据新的 TriggerCondition 结构进行匹配
                        if (!isTriggerConditionMatched(message, condition, ruleScene, trigger)) {
                            allConditionsMatched = false;
                            break;
                        }
                    }

                    if (allConditionsMatched) {
                        anyGroupMatched = true;
                        break; // 有一个分组匹配即可
                    }
                }

                if (anyGroupMatched) {
                    log.info("[getMatchedRuleSceneList][消息({}) 匹配到规则场景编号({}) 的触发器({})]", message, ruleScene.getId(), trigger);
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * 基于消息，判断触发器的条件是否匹配
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @param ruleScene 规则场景（用于日志，无其它作用）
     * @param trigger   触发器（用于日志，无其它作用）
     * @return 是否匹配
     */
    private boolean isTriggerConditionMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition,
                                              IotSceneRuleDO ruleScene, IotSceneRuleDO.Trigger trigger) {
        try {
            // 1. 根据条件类型进行匹配
            if (IotRuleSceneConditionTypeEnum.DEVICE_STATE.getType().equals(condition.getType())) {
                // 设备状态条件匹配
                return matchDeviceStateCondition(message, condition);
            } else if (IotRuleSceneConditionTypeEnum.DEVICE_PROPERTY.getType().equals(condition.getType())) {
                // 设备属性条件匹配
                return matchDevicePropertyCondition(message, condition);
            } else if (IotRuleSceneConditionTypeEnum.CURRENT_TIME.getType().equals(condition.getType())) {
                // 当前时间条件匹配
                return matchCurrentTimeCondition(condition);
            } else {
                log.warn("[isTriggerConditionMatched][规则场景编号({}) 的触发器({}) 存在未知的条件类型({})]",
                        ruleScene.getId(), trigger, condition.getType());
                return false;
            }
        } catch (Exception e) {
            log.error("[isTriggerConditionMatched][规则场景编号({}) 的触发器({}) 条件匹配异常]",
                    ruleScene.getId(), trigger, e);
            return false;
        }
    }

    /**
     * 匹配设备状态条件
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @return 是否匹配
     */
    private boolean matchDeviceStateCondition(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // TODO @芋艿：需要根据设备状态进行匹配
        // 这里需要检查消息中的设备状态是否符合条件中定义的状态
        log.debug("[matchDeviceStateCondition][设备状态条件匹配逻辑待实现] condition: {}", condition);
        return false;
    }

    /**
     * 匹配设备属性条件
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @return 是否匹配
     */
    private boolean matchDevicePropertyCondition(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // 1. 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (StrUtil.isBlank(condition.getIdentifier()) || !condition.getIdentifier().equals(messageIdentifier)) {
            return false;
        }

        // 2. 获取消息中的属性值
        Object messageValue = message.getData();
        if (messageValue == null) {
            return false;
        }

        // 3. 根据操作符进行匹配
        return evaluateCondition(messageValue, condition.getOperator(), condition.getParam());
    }

    /**
     * 匹配当前时间条件
     *
     * @param condition 触发条件
     * @return 是否匹配
     */
    private boolean matchCurrentTimeCondition(IotSceneRuleDO.TriggerCondition condition) {
        // TODO @芋艿：需要根据当前时间进行匹配
        // 这里需要检查当前时间是否符合条件中定义的时间范围
        log.debug("[matchCurrentTimeCondition][当前时间条件匹配逻辑待实现] condition: {}", condition);
        return false;
    }

    /**
     * 评估条件是否匹配
     *
     * @param sourceValue 源值（来自消息）
     * @param operator    操作符
     * @param paramValue  参数值（来自条件配置）
     * @return 是否匹配
     */
    private boolean evaluateCondition(Object sourceValue, String operator, String paramValue) {
        try {
            // 1. 校验操作符是否合法
            IotRuleSceneConditionOperatorEnum operatorEnum = IotRuleSceneConditionOperatorEnum.operatorOf(operator);
            if (operatorEnum == null) {
                log.warn("[evaluateCondition][存在错误的操作符({})]", operator);
                return false;
            }

            // 2.1 构建 Spring 表达式的变量
            Map<String, Object> springExpressionVariables = MapUtil.<String, Object>builder()
                    .put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_SOURCE, sourceValue)
                    .build();
            // 2.2 根据操作符类型处理参数值
            if (StrUtil.isNotBlank(paramValue)) {
                // TODO @puhui999：这里是不是在 IotRuleSceneConditionOperatorEnum 加个属性；
                if (operatorEnum == IotRuleSceneConditionOperatorEnum.IN
                        || operatorEnum == IotRuleSceneConditionOperatorEnum.NOT_IN
                        || operatorEnum == IotRuleSceneConditionOperatorEnum.BETWEEN
                        || operatorEnum == IotRuleSceneConditionOperatorEnum.NOT_BETWEEN) {
                    // 处理多值情况
                    List<String> paramValues = StrUtil.split(paramValue, CharPool.COMMA);
                    springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_VALUE_LIST,
                            convertList(paramValues, NumberUtil::parseDouble));
                } else {
                    // 处理单值情况
                    springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_VALUE,
                            NumberUtil.parseDouble(paramValue));
                }
            }

            // 3. 计算 Spring 表达式
            return (Boolean) SpringExpressionUtils.parseExpression(operatorEnum.getSpringExpression(), springExpressionVariables);
        } catch (Exception e) {
            log.error("[evaluateCondition][条件评估异常] sourceValue: {}, operator: {}, paramValue: {}",
                    sourceValue, operator, paramValue, e);
            return false;
        }
    }

    // TODO @芋艿：【可优化】可以考虑增加下单测，边界太多了。

    /**
     * 判断触发器的条件参数是否匹配
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @param ruleScene 规则场景（用于日志，无其它作用）
     * @param trigger   触发器（用于日志，无其它作用）
     * @return 是否匹配
     */
    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private boolean isTriggerConditionParameterMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition,
                                                       IotSceneRuleDO ruleScene, IotSceneRuleDO.Trigger trigger) {
        // 1.1 校验操作符是否合法
        IotRuleSceneConditionOperatorEnum operator =
                IotRuleSceneConditionOperatorEnum.operatorOf(condition.getOperator());
        if (operator == null) {
            log.error("[isTriggerConditionParameterMatched][规则场景编号({}) 的触发器({}) 存在错误的操作符({})]",
                    ruleScene.getId(), trigger, condition.getOperator());
            return false;
        }
        // 1.2 校验消息是否包含对应的值
        String messageValue = MapUtil.getStr((Map<String, Object>) message.getData(), condition.getIdentifier());
        if (messageValue == null) {
            return false;
        }

        // 2.1 构建 Spring 表达式的变量
        Map<String, Object> springExpressionVariables = new HashMap<>();
        try {
            springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_SOURCE, messageValue);
            springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_VALUE, condition.getParam());
            List<String> parameterValues = StrUtil.splitTrim(condition.getParam(), CharPool.COMMA);
            springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_VALUE_LIST, parameterValues);
            // 特殊：解决数字的比较。因为 Spring 是基于它的 compareTo 方法，对数字的比较存在问题！
            if (ObjectUtils.equalsAny(operator, IotRuleSceneConditionOperatorEnum.BETWEEN,
                    IotRuleSceneConditionOperatorEnum.NOT_BETWEEN,
                    IotRuleSceneConditionOperatorEnum.GREATER_THAN,
                    IotRuleSceneConditionOperatorEnum.GREATER_THAN_OR_EQUALS,
                    IotRuleSceneConditionOperatorEnum.LESS_THAN,
                    IotRuleSceneConditionOperatorEnum.LESS_THAN_OR_EQUALS)
                    && NumberUtil.isNumber(messageValue)
                    && NumberUtils.isAllNumber(parameterValues)) {
                springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_SOURCE,
                        NumberUtil.parseDouble(messageValue));
                springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_VALUE,
                        NumberUtil.parseDouble(condition.getParam()));
                springExpressionVariables.put(IotRuleSceneConditionOperatorEnum.SPRING_EXPRESSION_VALUE_LIST,
                        convertList(parameterValues, NumberUtil::parseDouble));
            }
            // 2.2 计算 Spring 表达式
            return (Boolean) SpringExpressionUtils.parseExpression(operator.getSpringExpression(), springExpressionVariables);
        } catch (Exception e) {
            log.error("[isTriggerConditionParameterMatched][消息({}) 规则场景编号({}) 的触发器({}) 的匹配表达式({}/{}) 计算异常]",
                    message, ruleScene.getId(), trigger, operator, springExpressionVariables, e);
            return false;
        }
    }

    /**
     * 执行规则场景的动作
     *
     * @param message    设备消息
     * @param ruleScenes 规则场景列表
     */
    private void executeRuleSceneAction(IotDeviceMessage message, List<IotSceneRuleDO> ruleScenes) {
        // 1. 遍历规则场景
        ruleScenes.forEach(ruleScene -> {
            // 2. 遍历规则场景的动作
            ruleScene.getActions().forEach(actionConfig -> {
                // 3.1 获取对应的动作 Action 数组
                List<IotSceneRuleAction> actions = filterList(ruleSceneActions,
                        action -> action.getType().getType().equals(actionConfig.getType()));
                if (CollUtil.isEmpty(actions)) {
                    return;
                }
                // 3.2 执行动作
                actions.forEach(action -> {
                    try {
                        action.execute(message, ruleScene, actionConfig);
                        log.info("[executeRuleSceneAction][消息({}) 规则场景编号({}) 的执行动作({}) 成功]",
                                message, ruleScene.getId(), actionConfig);
                    } catch (Exception e) {
                        log.error("[executeRuleSceneAction][消息({}) 规则场景编号({}) 的执行动作({}) 执行异常]",
                                message, ruleScene.getId(), actionConfig, e);
                    }
                });
            });
        });
    }

    @Override
    @SneakyThrows
    public void test() {
        // TODO @芋艿：测试思路代码，记得删除！！！
        // 1. Job 类：IotRuleSceneJob DONE
        // 2. 参数：id DONE
        // 3. jobHandlerName：IotRuleSceneJob + id DONE

        // 新增：addJob
        // 修改：不存在 addJob、存在 updateJob
        // 有 + 禁用：1）存在、停止；2）不存在：不处理；TODO 测试：直接暂停，是否可以？？？（结论：可以）pauseJob
        // 有 + 开启：1）存在，更新；2）不存在，新增；结论：使用 save(addOrUpdateJob)
        // 无 + 禁用、开启：1）存在，删除；TODO 测试：直接删除？？？（结论：可以）deleteJob

        //
        if (false) {
            Long id = 1L;
            Map<String, Object> jobDataMap = IotRuleSceneJob.buildJobDataMap(id);
            schedulerManager.addOrUpdateJob(IotRuleSceneJob.class,
                    IotRuleSceneJob.buildJobName(id),
                    "0/10 * * * * ?",
                    jobDataMap);
        }
        if (false) {
            Long id = 1L;
            schedulerManager.pauseJob(IotRuleSceneJob.buildJobName(id));
        }
        if (true) {
            Long id = 1L;
            schedulerManager.deleteJob(IotRuleSceneJob.buildJobName(id));
        }
    }

    public static void main2(String[] args) throws SchedulerException {
//        System.out.println(QuartzJobBean.class);
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        String jobHandlerName = "123";
        // 暂停 Trigger 对象
        scheduler.pauseTrigger(new TriggerKey(jobHandlerName));
        // 取消并删除 Job 调度
        scheduler.unscheduleJob(new TriggerKey(jobHandlerName));
        scheduler.deleteJob(new JobKey(jobHandlerName));
    }

}

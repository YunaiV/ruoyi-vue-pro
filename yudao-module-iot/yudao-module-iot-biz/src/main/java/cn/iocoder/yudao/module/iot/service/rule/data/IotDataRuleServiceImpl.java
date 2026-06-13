package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotDataRuleMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.data.action.IotDataRuleAction;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DATA_RULE_NAME_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DATA_RULE_NOT_EXISTS;

/**
 * IoT 数据流转规则 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDataRuleServiceImpl implements IotDataRuleService {

    @Resource
    private IotDataRuleMapper dataRuleMapper;

    @Resource
    private IotProductService productService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDataSinkService dataSinkService;

    @Resource
    private List<IotDataRuleAction> dataRuleActions;

    @Override
    @CacheEvict(value = RedisKeyConstants.DATA_RULE_LIST, allEntries = true)
    public Long createDataRule(IotDataRuleSaveReqVO createReqVO) {
        // 校验名称唯一
        validateDataRuleNameUnique(null, createReqVO.getName());
        // 校验数据源配置和数据目的
        validateDataRuleConfig(createReqVO);
        // 新增
        IotDataRuleDO dataRule = BeanUtils.toBean(createReqVO, IotDataRuleDO.class);
        dataRuleMapper.insert(dataRule);
        return dataRule.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.DATA_RULE_LIST, allEntries = true)
    public void updateDataRule(IotDataRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateDataRuleExists(updateReqVO.getId());
        // 校验名称唯一
        validateDataRuleNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 校验数据源配置和数据目的
        validateDataRuleConfig(updateReqVO);

        // 更新
        IotDataRuleDO updateObj = BeanUtils.toBean(updateReqVO, IotDataRuleDO.class);
        dataRuleMapper.updateById(updateObj);
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.DATA_RULE_LIST, allEntries = true)
    public void deleteDataRule(Long id) {
        // 校验存在
        validateDataRuleExists(id);
        // 删除
        dataRuleMapper.deleteById(id);
    }

    private void validateDataRuleExists(Long id) {
        if (dataRuleMapper.selectById(id) == null) {
            throw exception(DATA_RULE_NOT_EXISTS);
        }
    }

    /**
     * 校验数据流转规则名称唯一性
     *
     * @param id   数据流转规则编号（用于更新时排除自身）
     * @param name 数据流转规则名称
     */
    private void validateDataRuleNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        IotDataRuleDO dataRule = dataRuleMapper.selectByName(name);
        if (dataRule == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的规则
        if (id == null) {
            throw exception(DATA_RULE_NAME_EXISTS);
        }
        if (!dataRule.getId().equals(id)) {
            throw exception(DATA_RULE_NAME_EXISTS);
        }
    }

    /**
     * 校验数据流转规则配置
     *
     * @param reqVO 数据流转规则保存请求VO
     */
    private void validateDataRuleConfig(IotDataRuleSaveReqVO reqVO) {
        // 1. 校验数据源配置
        validateSourceConfigs(reqVO.getSourceConfigs());
        // 2. 校验数据目的
        dataSinkService.validateDataSinksExist(reqVO.getSinkIds());
    }

    /**
     * 校验数据源配置
     *
     * @param sourceConfigs 数据源配置列表
     */
    private void validateSourceConfigs(List<IotDataRuleDO.SourceConfig> sourceConfigs) {
        // 1. 校验产品
        productService.validateProductsExist(
                convertSet(sourceConfigs, IotDataRuleDO.SourceConfig::getProductId));

        // 2. 校验设备
        deviceService.validateDeviceListExists(convertSet(sourceConfigs, IotDataRuleDO.SourceConfig::getDeviceId,
                config -> ObjUtil.notEqual(config.getDeviceId(), IotDeviceDO.DEVICE_ID_ALL)));

        // 3. 校验物模型存在
        validateThingModelsExist(sourceConfigs);
    }

    /**
     * 校验物模型存在
     *
     * @param sourceConfigs 数据源配置列表
     */
    private void validateThingModelsExist(List<IotDataRuleDO.SourceConfig> sourceConfigs) {
        Map<Long, Set<String>> productIdIdentifiers = new HashMap<>();
        for (IotDataRuleDO.SourceConfig config : sourceConfigs) {
            if (StrUtil.isEmpty(config.getIdentifier())) {
                continue;
            }
            productIdIdentifiers.computeIfAbsent(config.getProductId(),
                            productId -> new HashSet<>()).add(config.getIdentifier());
        }
        for (Map.Entry<Long, Set<String>> entry : productIdIdentifiers.entrySet()) {
            thingModelService.validateThingModelListExists(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public IotDataRuleDO getDataRule(Long id) {
        return dataRuleMapper.selectById(id);
    }

    @Override
    public PageResult<IotDataRuleDO> getDataRulePage(IotDataRulePageReqVO pageReqVO) {
        return dataRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDataRuleDO> getDataRuleListBySinkId(Long sinkId) {
        return dataRuleMapper.selectListBySinkId(sinkId);
    }

    @Cacheable(value = RedisKeyConstants.DATA_RULE_LIST,
               key = "#deviceId + '_' + #method + '_' + (#identifier ?: '')")
    public List<IotDataRuleDO> getDataRuleListByConditionFromCache(Long deviceId, String method, String identifier) {
        // 1. 查询所有开启的数据流转规则
        List<IotDataRuleDO> rules = dataRuleMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 2. 内存里过滤匹配的规则
        List<IotDataRuleDO> matchedRules = new ArrayList<>();
        for (IotDataRuleDO rule : rules) {
            IotDataRuleDO.SourceConfig found = CollUtil.findOne(rule.getSourceConfigs(),
                    config -> ObjectUtils.equalsAny(config.getDeviceId(), deviceId, IotDeviceDO.DEVICE_ID_ALL)
                            && Objects.equals(config.getMethod(), method)
                            && (StrUtil.isEmpty(config.getIdentifier()) || ObjUtil.equal(config.getIdentifier(), identifier)));
            if (found != null) {
                matchedRules.add(new IotDataRuleDO().setId(rule.getId()).setSinkIds(rule.getSinkIds()));
            }
        }
        return matchedRules;
    }

    @Override
    public void executeDataRule(IotDeviceMessage message) {
        try {
            Long deviceId = message.getDeviceId();
            String method = message.getMethod();
            // 1. 匹配命中的规则
            List<IotDataRuleDO> matchedRules;
            Object identifierForLog;
            if (IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod().equals(method)) {
                // 属性上报：params 含多个属性 key，每个 key 都可能命中规则
                Set<String> identifiers = IotDeviceMessageUtils.getPropertyIdentifiers(message);
                matchedRules = matchPropertyPostDataRules(deviceId, method, identifiers);
                identifierForLog = identifiers;
            } else {
                // 其他消息（事件 / 服务调用 / 状态）：单一 identifier
                String identifier = IotDeviceMessageUtils.getIdentifier(message);
                matchedRules = getSelf().getDataRuleListByConditionFromCache(deviceId, method, identifier);
                identifierForLog = identifier;
            }
            if (CollUtil.isEmpty(matchedRules)) {
                log.debug("[executeDataRule][设备({}) 方法({}) 标识符({}) 没有匹配的数据流转规则]",
                        deviceId, method, identifierForLog);
                return;
            }
            log.info("[executeDataRule][设备({}) 方法({}) 标识符({}) 匹配到 {} 条数据流转规则]",
                    deviceId, method, identifierForLog, matchedRules.size());

            // 2. 跨规则去重 sink，避免多条规则命中同一数据目的时重复推送
            Set<Long> processedSinkIds = new HashSet<>();
            matchedRules.forEach(rule -> executeDataRule(message, rule, processedSinkIds));
        } catch (Exception e) {
            log.error("[executeDataRule][消息({}) 执行数据流转规则异常]", message, e);
        }
    }

    /**
     * 匹配属性上报场景下命中的数据流转规则
     *
     * 同一规则可能同时匹配「任意属性」与具体 identifier，按 ruleId 去重后返回
     *
     * @param deviceId    设备编号
     * @param method      消息方法
     * @param identifiers 上报消息中包含的属性标识符集合
     * @return 命中的数据流转规则列表
     */
    private List<IotDataRuleDO> matchPropertyPostDataRules(Long deviceId, String method, Set<String> identifiers) {
        LinkedHashMap<Long, IotDataRuleDO> matchedRuleMap = new LinkedHashMap<>();
        // 情况一：先匹配未填 identifier 的「任意属性」规则，默认就匹配
        collectMatchedRules(matchedRuleMap, deviceId, method, null);
        // 情况二：再针对每个上报的属性标识符匹配限定具体 identifier 的规则
        identifiers.forEach(identifier -> collectMatchedRules(matchedRuleMap, deviceId, method, identifier));
        return new ArrayList<>(matchedRuleMap.values());
    }

    private void collectMatchedRules(Map<Long, IotDataRuleDO> matchedRuleMap,
                                     Long deviceId, String method, String identifier) {
        getSelf().getDataRuleListByConditionFromCache(deviceId, method, identifier)
                .forEach(rule -> matchedRuleMap.putIfAbsent(rule.getId(), rule));
    }

    /**
     * 为指定规则的所有数据目的执行数据流转
     *
     * @param message          设备消息
     * @param rule             数据流转规则
     * @param processedSinkIds 已处理的数据目的编号集合，跨规则去重，避免同一数据目的被重复推送
     */
    private void executeDataRule(IotDeviceMessage message, IotDataRuleDO rule, Set<Long> processedSinkIds) {
        rule.getSinkIds().forEach(sinkId -> {
            // 同一消息下，多条规则命中同一数据目的时只推送一次
            if (!processedSinkIds.add(sinkId)) {
                return;
            }
            try {
                // 获取数据目的配置
                IotDataSinkDO dataSink = dataSinkService.getDataSinkFromCache(sinkId);
                if (dataSink == null) {
                    log.error("[executeDataRule][规则({}) 对应的数据目的({}) 不存在]", rule.getId(), sinkId);
                    return;
                }
                if (CommonStatusEnum.isDisable(dataSink.getStatus())) {
                    log.info("[executeDataRule][规则({}) 对应的数据目的({}) 状态为禁用]", rule.getId(), sinkId);
                    return;
                }

                // 执行数据桥接操作
                executeDataRuleAction(message, dataSink);
            } catch (Exception e) {
                log.error("[executeDataRule][规则({}) 数据目的({}) 执行异常]", rule.getId(), sinkId, e);
            }
        });
    }

    /**
     * 执行数据流转操作
     *
     * @param message  设备消息
     * @param dataSink 数据目的
     */
    private void executeDataRuleAction(IotDeviceMessage message, IotDataSinkDO dataSink) {
        dataRuleActions.forEach(action -> {
            if (ObjUtil.notEqual(action.getType(), dataSink.getType())) {
                return;
            }
            if (CommonStatusEnum.isDisable(dataSink.getStatus())) {
                log.warn("[executeDataRuleAction][消息({}) 数据目的({}) 状态为禁用]", message.getId(), dataSink.getId());
                return;
            }
            try {
                action.execute(message, dataSink);
                log.info("[executeDataRuleAction][消息({}) 数据目的({}) 执行成功]", message.getId(), dataSink.getId());
            } catch (Exception e) {
                log.error("[executeDataRuleAction][消息({}) 数据目的({}) 执行异常]", message.getId(), dataSink.getId(), e);
            }
        });
    }

    private IotDataRuleServiceImpl getSelf() {
        return SpringUtils.getBean(IotDataRuleServiceImpl.class);
    }

}
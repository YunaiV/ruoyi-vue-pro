package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotDataRuleMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DATA_RULE_NOT_EXISTS;

/**
 * IoT 数据流转规则 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
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

    @Override
    public Long createDataRule(IotDataRuleSaveReqVO createReqVO) {
        // 校验数据源配置和数据目的
        validateDataRuleConfig(createReqVO);
        // 新增
        IotDataRuleDO dataRule = BeanUtils.toBean(createReqVO, IotDataRuleDO.class);
        dataRuleMapper.insert(dataRule);
        return dataRule.getId();
    }

    @Override
    public void updateDataRule(IotDataRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateDataRuleExists(updateReqVO.getId());
        // 校验数据源配置和数据目的
        validateDataRuleConfig(updateReqVO);

        // 更新
        IotDataRuleDO updateObj = BeanUtils.toBean(updateReqVO, IotDataRuleDO.class);
        dataRuleMapper.updateById(updateObj);
    }

    @Override
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
        deviceService.validateDevicesExist(convertSet(sourceConfigs, IotDataRuleDO.SourceConfig::getDeviceId,
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
        Map<Long, Set<String>> productIdToIdentifiers = new HashMap<>();
        for (IotDataRuleDO.SourceConfig config : sourceConfigs) {
            if (StrUtil.isEmpty(config.getIdentifier())) {
                continue;
            }
            productIdToIdentifiers.computeIfAbsent(config.getProductId(),
                            productId -> new HashSet<>()).add(config.getIdentifier());
        }
        for (Map.Entry<Long, Set<String>> entry : productIdToIdentifiers.entrySet()) {
            thingModelService.validateThingModelsExist(entry.getKey(), entry.getValue());
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
    public List<IotDataRuleDO> getDataRuleBySinkId(Long sinkId) {
        return dataRuleMapper.selectListBySinkId(sinkId);
    }

}
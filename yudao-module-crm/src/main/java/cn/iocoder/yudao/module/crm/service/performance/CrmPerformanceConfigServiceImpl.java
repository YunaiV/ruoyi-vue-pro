package cn.iocoder.yudao.module.crm.service.performance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.performance.CrmPerformanceConfigDO;
import cn.iocoder.yudao.module.crm.dal.mysql.performance.CrmPerformanceConfigMapper;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigObjectTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;

/**
 * CRM 业绩目标设置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmPerformanceConfigServiceImpl implements CrmPerformanceConfigService {

    @Resource
    private CrmPerformanceConfigMapper performanceConfigMapper;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createPerformanceConfig(CrmPerformanceConfigSaveReqVO createReqVO) {
        // 1.1 校验目标对象
        validatePerformanceConfigObject(createReqVO.getObjectType(), createReqVO.getObjectId());
        // 1.2 校验唯一
        validatePerformanceConfigNotExists(null, createReqVO);

        // 2. 插入
        CrmPerformanceConfigDO performanceConfig = BeanUtils.toBean(createReqVO, CrmPerformanceConfigDO.class);
        fillTargetPrice(performanceConfig);
        performanceConfigMapper.insert(performanceConfig);
        return performanceConfig.getId();
    }

    @Override
    public void updatePerformanceConfig(CrmPerformanceConfigSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePerformanceConfigExists(updateReqVO.getId());
        // 1.2 校验目标对象
        validatePerformanceConfigObject(updateReqVO.getObjectType(), updateReqVO.getObjectId());
        // 1.3 校验唯一
        validatePerformanceConfigNotExists(updateReqVO.getId(), updateReqVO);

        // 2. 更新
        CrmPerformanceConfigDO updateObj = BeanUtils.toBean(updateReqVO, CrmPerformanceConfigDO.class);
        fillTargetPrice(updateObj);
        performanceConfigMapper.updateById(updateObj);
    }

    @Override
    public void deletePerformanceConfig(Long id) {
        // 1. 校验存在
        validatePerformanceConfigExists(id);
        // 2. 删除
        performanceConfigMapper.deleteById(id);
    }

    @Override
    public CrmPerformanceConfigDO getPerformanceConfig(Long id) {
        return validatePerformanceConfigExists(id);
    }

    @Override
    public PageResult<CrmPerformanceConfigDO> getPerformanceConfigPage(CrmPerformanceConfigPageReqVO pageReqVO) {
        return performanceConfigMapper.selectPage(pageReqVO);
    }

    private CrmPerformanceConfigDO validatePerformanceConfigExists(Long id) {
        CrmPerformanceConfigDO performanceConfig = performanceConfigMapper.selectById(id);
        if (performanceConfig == null) {
            throw exception(PERFORMANCE_CONFIG_NOT_EXISTS);
        }
        return performanceConfig;
    }

    private void validatePerformanceConfigNotExists(Long id, CrmPerformanceConfigSaveReqVO reqVO) {
        CrmPerformanceConfigDO performanceConfig = performanceConfigMapper.selectByObjectIdAndObjectTypeAndYearAndBizType(
                reqVO.getObjectId(), reqVO.getObjectType(), reqVO.getYear(), reqVO.getBizType());
        if (performanceConfig == null || Objects.equals(performanceConfig.getId(), id)) {
            return;
        }
        throw exception(PERFORMANCE_CONFIG_EXISTS);
    }

    private void validatePerformanceConfigObject(Integer objectType, Long objectId) {
        // 情况一：部门
        if (Objects.equals(objectType, CrmPerformanceConfigObjectTypeEnum.DEPT.getObjectType())) {
            deptApi.validateDeptList(singleton(objectId));
            return;
        }
        // 情况二：员工
        if (Objects.equals(objectType, CrmPerformanceConfigObjectTypeEnum.USER.getObjectType())) {
            adminUserApi.validateUserList(singleton(objectId));
            return;
        }
        throw exception(PERFORMANCE_CONFIG_TYPE_INVALID);
    }

    @Override
    public CrmPerformanceConfigDO getPerformanceConfig(Long objectId, Integer objectType, Integer year, Integer bizType) {
        return performanceConfigMapper.selectByObjectIdAndObjectTypeAndYearAndBizType(objectId, objectType, year, bizType);
    }

    @Override
    public List<CrmPerformanceConfigDO> getPerformanceConfigList(Collection<Long> objectIds, Integer objectType,
                                                                 Integer year, Integer bizType) {
        if (isEmpty(objectIds)) {
            return Collections.emptyList();
        }
        return performanceConfigMapper.selectListByObjectIdsAndObjectTypeAndYearAndBizType(objectIds, objectType,
                year, bizType);
    }

    private void fillTargetPrice(CrmPerformanceConfigDO performanceConfig) {
        // 1. 空值设置为 0
        performanceConfig.setJanuaryTargetPrice(defaultIfNull(performanceConfig.getJanuaryTargetPrice()));
        performanceConfig.setFebruaryTargetPrice(defaultIfNull(performanceConfig.getFebruaryTargetPrice()));
        performanceConfig.setMarchTargetPrice(defaultIfNull(performanceConfig.getMarchTargetPrice()));
        performanceConfig.setAprilTargetPrice(defaultIfNull(performanceConfig.getAprilTargetPrice()));
        performanceConfig.setMayTargetPrice(defaultIfNull(performanceConfig.getMayTargetPrice()));
        performanceConfig.setJuneTargetPrice(defaultIfNull(performanceConfig.getJuneTargetPrice()));
        performanceConfig.setJulyTargetPrice(defaultIfNull(performanceConfig.getJulyTargetPrice()));
        performanceConfig.setAugustTargetPrice(defaultIfNull(performanceConfig.getAugustTargetPrice()));
        performanceConfig.setSeptemberTargetPrice(defaultIfNull(performanceConfig.getSeptemberTargetPrice()));
        performanceConfig.setOctoberTargetPrice(defaultIfNull(performanceConfig.getOctoberTargetPrice()));
        performanceConfig.setNovemberTargetPrice(defaultIfNull(performanceConfig.getNovemberTargetPrice()));
        performanceConfig.setDecemberTargetPrice(defaultIfNull(performanceConfig.getDecemberTargetPrice()));
        // 2. 计算年度目标
        performanceConfig.setYearTargetPrice(performanceConfig.getJanuaryTargetPrice()
                .add(performanceConfig.getFebruaryTargetPrice())
                .add(performanceConfig.getMarchTargetPrice())
                .add(performanceConfig.getAprilTargetPrice())
                .add(performanceConfig.getMayTargetPrice())
                .add(performanceConfig.getJuneTargetPrice())
                .add(performanceConfig.getJulyTargetPrice())
                .add(performanceConfig.getAugustTargetPrice())
                .add(performanceConfig.getSeptemberTargetPrice())
                .add(performanceConfig.getOctoberTargetPrice())
                .add(performanceConfig.getNovemberTargetPrice())
                .add(performanceConfig.getDecemberTargetPrice()));
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}

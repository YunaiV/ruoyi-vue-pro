package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliarytype.FmsAuxiliaryTypeSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAuxiliaryTypeMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherTemplateService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.*;

/**
 * FMS 辅助核算类别 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsAuxiliaryTypeServiceImpl implements FmsAuxiliaryTypeService {

    @Resource
    private FmsAuxiliaryTypeMapper auxiliaryTypeMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private FmsAuxiliaryItemService auxiliaryItemService;
    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private FmsSubjectService subjectService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private FmsVoucherService voucherService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private FmsVoucherTemplateService voucherTemplateService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private FmsInitialBalanceService initialBalanceService;

    @Override
    public void initializeDefaultTypes(Long accountSetId) {
        List<FmsAuxiliaryTypeDO> auxiliaryTypes = convertList(FmsAuxiliaryTypeEnum.SYSTEM_PRESET_TYPES,
                type -> new FmsAuxiliaryTypeDO().setName(type.getName()).setSystemPreset(true)
                        .setAccountSetId(accountSetId).setType(type.getType()));
        auxiliaryTypeMapper.insertBatch(auxiliaryTypes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_TYPE, subType = FMS_AUXILIARY_TYPE_CREATE_SUB_TYPE,
            bizNo = "{{#auxiliaryTypeId}}", success = FMS_AUXILIARY_TYPE_CREATE_SUCCESS)
    public Long createAuxiliaryType(FmsAuxiliaryTypeSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验名称唯一
        validateAuxiliaryTypeNameUnique(null, createReqVO.getAccountSetId(), createReqVO.getName());

        // 2. 创建自定义辅助核算类别
        FmsAuxiliaryTypeDO auxiliaryType = BeanUtils.toBean(createReqVO, FmsAuxiliaryTypeDO.class)
                .setId(null).setSystemPreset(false).setType(FmsAuxiliaryTypeEnum.CUSTOM.getType());
        auxiliaryTypeMapper.insert(auxiliaryType);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("auxiliaryTypeId", auxiliaryType.getId());
        return auxiliaryType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_TYPE, subType = FMS_AUXILIARY_TYPE_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_AUXILIARY_TYPE_UPDATE_SUCCESS)
    public void updateAuxiliaryType(FmsAuxiliaryTypeSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验辅助核算类别
        FmsAuxiliaryTypeDO auxiliaryType = validateAuxiliaryTypeExists(
                updateReqVO.getAccountSetId(), updateReqVO.getId());
        validateCustomAuxiliaryType(auxiliaryType);
        // 1.3 校验名称唯一
        validateAuxiliaryTypeNameUnique(auxiliaryType.getId(), updateReqVO.getAccountSetId(), updateReqVO.getName());

        // 2. 更新辅助核算类别
        auxiliaryTypeMapper.updateById(new FmsAuxiliaryTypeDO()
                .setId(auxiliaryType.getId()).setName(updateReqVO.getName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_TYPE, subType = FMS_AUXILIARY_TYPE_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_AUXILIARY_TYPE_DELETE_SUCCESS)
    public void deleteAuxiliaryType(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验辅助核算类别
        FmsAuxiliaryTypeDO auxiliaryType = validateAuxiliaryTypeExists(accountSetId, id);
        validateCustomAuxiliaryType(auxiliaryType);
        // 1.3 校验辅助核算类别未被业务引用
        // 1.3.1 校验辅助核算类别下没有项目
        Long auxiliaryItemCount = auxiliaryItemService.getAuxiliaryItemCountByAuxiliaryTypeId(accountSetId, id);
        if (auxiliaryItemCount > 0) {
            throw exception(AUXILIARY_TYPE_HAS_ITEM, auxiliaryItemCount);
        }
        // 1.3.2 校验辅助核算类别未被科目使用
        Long subjectCount = subjectService.getSubjectCountByAuxiliaryTypeId(accountSetId, id);
        if (subjectCount > 0) {
            throw exception(AUXILIARY_TYPE_SUBJECT_IN_USE, subjectCount);
        }
        // 1.3.3 校验辅助核算类别未被凭证使用
        Long voucherEntryCount = voucherService.getVoucherEntryCountByAuxiliaryTypeId(accountSetId, id);
        if (voucherEntryCount > 0) {
            throw exception(AUXILIARY_TYPE_VOUCHER_IN_USE, voucherEntryCount);
        }
        // 1.3.4 校验辅助核算类别未被凭证模板使用
        Long voucherTemplateCount = voucherTemplateService
                .getVoucherTemplateCountByAuxiliaryTypeId(accountSetId, id);
        if (voucherTemplateCount > 0) {
            throw exception(AUXILIARY_TYPE_VOUCHER_TEMPLATE_IN_USE, voucherTemplateCount);
        }
        // 1.3.5 校验辅助核算类别未被初始余额使用
        Long initialBalanceCount = initialBalanceService.getInitialBalanceCountByAuxiliaryTypeId(accountSetId, id);
        if (initialBalanceCount > 0) {
            throw exception(AUXILIARY_TYPE_INITIAL_BALANCE_IN_USE, initialBalanceCount);
        }

        // 2. 删除辅助核算类别
        auxiliaryTypeMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("auxiliaryType", auxiliaryType);
    }

    @Override
    public FmsAuxiliaryTypeDO getAuxiliaryType(Long accountSetId, Long id, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询并校验辅助核算类别
        return validateAuxiliaryTypeExists(accountSetId, id);
    }

    @Override
    public List<FmsAuxiliaryTypeDO> getAuxiliaryTypeList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询辅助核算类别
        return auxiliaryTypeMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public List<FmsAuxiliaryTypeDO> getAuxiliaryTypeList(Long accountSetId) {
        return auxiliaryTypeMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public List<FmsAuxiliaryTypeDO> getAuxiliaryTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return auxiliaryTypeMapper.selectByIds(ids);
    }

    @Override
    public List<FmsAuxiliaryTypeDO> validateAuxiliaryTypeList(Collection<Long> ids) {
        List<FmsAuxiliaryTypeDO> types = getAuxiliaryTypeList(ids);
        if (types.size() != CollUtil.size(ids)) {
            throw exception(AUXILIARY_TYPE_NOT_EXISTS);
        }
        return types;
    }

    @Override
    public List<FmsAuxiliaryTypeDO> validateAuxiliaryTypeList(Long accountSetId, Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<FmsAuxiliaryTypeDO> types = auxiliaryTypeMapper.selectListByIdsAndAccountSetId(ids, accountSetId);
        if (types.size() != CollUtil.distinct(ids).size()) {
            throw exception(AUXILIARY_TYPE_NOT_EXISTS);
        }
        return types;
    }

    private FmsAuxiliaryTypeDO validateAuxiliaryTypeExists(Long accountSetId, Long id) {
        FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeMapper.selectById(id);
        if (auxiliaryType == null || ObjUtil.notEqual(auxiliaryType.getAccountSetId(), accountSetId)) {
            throw exception(AUXILIARY_TYPE_NOT_EXISTS);
        }
        return auxiliaryType;
    }

    private void validateAuxiliaryTypeNameUnique(Long id, Long accountSetId, String name) {
        FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeMapper.selectByAccountSetIdAndName(accountSetId, name);
        if (auxiliaryType != null && ObjUtil.notEqual(auxiliaryType.getId(), id)) {
            throw exception(AUXILIARY_TYPE_NAME_DUPLICATE);
        }
    }

    private void validateCustomAuxiliaryType(FmsAuxiliaryTypeDO auxiliaryType) {
        if (Boolean.TRUE.equals(auxiliaryType.getSystemPreset())) {
            throw exception(AUXILIARY_TYPE_SYSTEM_NOT_EDITABLE);
        }
    }

}

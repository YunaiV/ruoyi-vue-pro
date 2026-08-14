package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAuxiliaryItemMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherTemplateService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.*;

/**
 * FMS 辅助核算项目 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsAuxiliaryItemServiceImpl implements FmsAuxiliaryItemService {

    @Resource
    private FmsAuxiliaryItemMapper auxiliaryItemMapper;

    @Resource
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;
    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private FmsVoucherService voucherService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private FmsVoucherTemplateService voucherTemplateService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private FmsInitialBalanceService initialBalanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_ITEM_TYPE, subType = FMS_AUXILIARY_ITEM_CREATE_SUB_TYPE,
            bizNo = "{{#auxiliaryItemId}}", success = FMS_AUXILIARY_ITEM_CREATE_SUCCESS)
    public Long createAuxiliaryItem(FmsAuxiliaryItemSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验辅助核算类别和编码唯一
        validateAuxiliaryType(createReqVO.getAccountSetId(), createReqVO.getAuxiliaryTypeId());
        validateAuxiliaryItemCodeUnique(null, createReqVO.getAccountSetId(),
                createReqVO.getAuxiliaryTypeId(), createReqVO.getCode());

        // 2. 创建辅助核算项目
        FmsAuxiliaryItemDO auxiliaryItem = BeanUtils.toBean(createReqVO, FmsAuxiliaryItemDO.class)
                .setId(null).setStatus(CommonStatusEnum.ENABLE.getStatus());
        auxiliaryItemMapper.insert(auxiliaryItem);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("auxiliaryItemId", auxiliaryItem.getId());
        return auxiliaryItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_ITEM_TYPE, subType = FMS_AUXILIARY_ITEM_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_AUXILIARY_ITEM_UPDATE_SUCCESS)
    public void updateAuxiliaryItem(FmsAuxiliaryItemSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验辅助核算项目和所属类别
        FmsAuxiliaryItemDO auxiliaryItem = validateAuxiliaryItemExists(
                updateReqVO.getAccountSetId(), updateReqVO.getId());
        if (ObjUtil.notEqual(auxiliaryItem.getAuxiliaryTypeId(), updateReqVO.getAuxiliaryTypeId())) {
            throw exception(AUXILIARY_TYPE_NOT_EXISTS);
        }
        validateAuxiliaryType(updateReqVO.getAccountSetId(), updateReqVO.getAuxiliaryTypeId());
        // 1.3 校验编码唯一
        validateAuxiliaryItemCodeUnique(auxiliaryItem.getId(), updateReqVO.getAccountSetId(),
                updateReqVO.getAuxiliaryTypeId(), updateReqVO.getCode());

        // 2. 更新辅助核算项目
        auxiliaryItemMapper.updateById(BeanUtils.toBean(updateReqVO, FmsAuxiliaryItemDO.class)
                .setStatus(auxiliaryItem.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_ITEM_TYPE, subType = FMS_AUXILIARY_ITEM_DELETE_SUB_TYPE,
            bizNo = "{{#accountSetId}}", success = FMS_AUXILIARY_ITEM_DELETE_SUCCESS)
    public void deleteAuxiliaryItemList(Long accountSetId, List<Long> ids, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验辅助核算项目
        List<FmsAuxiliaryItemDO> auxiliaryItems = auxiliaryItemMapper
                .selectListByIdsAndAccountSetId(ids, accountSetId);
        if (auxiliaryItems.size() != new LinkedHashSet<>(ids).size()) {
            throw exception(AUXILIARY_ITEM_NOT_EXISTS);
        }
        Set<Long> idSet = new LinkedHashSet<>(ids);
        // 1.3 校验辅助核算项目未被业务引用
        // 1.3.1 校验辅助核算项目未被凭证使用
        Long voucherEntryCount = voucherService.getVoucherEntryCountByAuxiliaryItemIds(accountSetId, idSet);
        if (voucherEntryCount > 0) {
            throw exception(AUXILIARY_ITEM_VOUCHER_IN_USE, voucherEntryCount);
        }
        // 1.3.2 校验辅助核算项目未被凭证模板使用
        Long voucherTemplateCount = voucherTemplateService
                .getVoucherTemplateCountByAuxiliaryItemIds(accountSetId, idSet);
        if (voucherTemplateCount > 0) {
            throw exception(AUXILIARY_ITEM_VOUCHER_TEMPLATE_IN_USE, voucherTemplateCount);
        }
        // 1.3.3 校验辅助核算项目未被初始余额使用
        Long initialBalanceCount = initialBalanceService
                .getInitialBalanceCountByAuxiliaryItemIds(accountSetId, idSet);
        if (initialBalanceCount > 0) {
            throw exception(AUXILIARY_ITEM_INITIAL_BALANCE_IN_USE, initialBalanceCount);
        }
        // 2. 删除派生的辅助核算组合和辅助核算项目
        auxiliaryCombinationService.deleteAuxiliaryCombinationByAuxiliaryItemIds(accountSetId, idSet);
        auxiliaryItemMapper.deleteByIds(ids);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("auxiliaryItem", CollUtil.getFirst(auxiliaryItems));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_AUXILIARY_ITEM_TYPE, subType = FMS_AUXILIARY_ITEM_STATUS_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_AUXILIARY_ITEM_STATUS_SUCCESS)
    public void updateAuxiliaryItemStatus(Long accountSetId, Long id, Integer status, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验辅助核算项目
        FmsAuxiliaryItemDO auxiliaryItem = validateAuxiliaryItemExists(accountSetId, id);

        // 2. 更新状态
        auxiliaryItemMapper.updateById(new FmsAuxiliaryItemDO()
                .setId(auxiliaryItem.getId()).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmsAuxiliaryItemImportRespVO importAuxiliaryItemList(Long accountSetId, Long auxiliaryTypeId,
            List<FmsAuxiliaryItemImportExcelVO> importItems, Long userId) {
        // 1.1 校验导入数据非空
        if (CollUtil.isEmpty(importItems)) {
            throw exception(AUXILIARY_ITEM_IMPORT_LIST_IS_EMPTY);
        }
        // 1.2 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.3 校验辅助核算类别
        FmsAuxiliaryTypeDO auxiliaryType = CollUtil.getFirst(auxiliaryTypeService
                .validateAuxiliaryTypeList(accountSetId, Collections.singletonList(auxiliaryTypeId)));
        // 1.4 加载已有辅助核算项目
        Map<String, FmsAuxiliaryItemDO> itemMap = convertMap(
                auxiliaryItemMapper.selectListByAccountSetIdAndAuxiliaryTypeId(accountSetId, auxiliaryTypeId),
                FmsAuxiliaryItemDO::getCode);

        // 2. 逐行导入辅助核算项目
        Set<String> importCodes = new HashSet<>();
        FmsAuxiliaryItemImportRespVO respVO = new FmsAuxiliaryItemImportRespVO()
                .setTotalCount(importItems.size()).setSuccessItemCodes(new ArrayList<>())
                .setFailureReasons(new LinkedHashMap<>());
        for (int index = 0; index < importItems.size(); index++) {
            FmsAuxiliaryItemImportExcelVO importItem = importItems.get(index);
            String label = buildImportLabel(index + 2, importItem.getCode(), importItem.getName());
            try {
                // 2.1 校验导入字段和编码唯一
                ValidationUtils.validate(importItem);
                if (!importCodes.add(importItem.getCode())) {
                    throw new IllegalArgumentException("导入文件内编码重复");
                }
                if (itemMap.containsKey(importItem.getCode())) {
                    throw new IllegalArgumentException("编码已存在");
                }

                // 2.2 构造并创建辅助核算项目
                FmsAuxiliaryItemDO item = new FmsAuxiliaryItemDO().setAccountSetId(accountSetId)
                        .setAuxiliaryTypeId(auxiliaryTypeId).setCode(importItem.getCode())
                        .setName(importItem.getName()).setRemark(importItem.getRemark())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
                if (FmsAuxiliaryTypeEnum.INVENTORY.getType().equals(auxiliaryType.getType())) {
                    item.setSpecification(importItem.getSpecification()).setUnit(importItem.getUnit());
                }
                auxiliaryItemMapper.insert(item);
                itemMap.put(importItem.getCode(), item);

                // 2.3 记录导入结果
                respVO.getSuccessItemCodes().add(importItem.getCode());
            } catch (ConstraintViolationException exception) {
                respVO.getFailureReasons().put(label, exception.getMessage());
            } catch (RuntimeException exception) {
                respVO.getFailureReasons().put(
                        label, StrUtil.blankToDefault(exception.getMessage(), "导入失败"));
            }
        }
        return respVO;
    }

    @Override
    public PageResult<FmsAuxiliaryItemDO> getAuxiliaryItemPage(FmsAuxiliaryItemPageReqVO pageReqVO, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(pageReqVO.getAccountSetId(), userId);
        // 1.2 校验辅助核算类别
        validateAuxiliaryType(pageReqVO.getAccountSetId(), pageReqVO.getAuxiliaryTypeId());

        // 2. 查询辅助核算项目分页
        return auxiliaryItemMapper.selectPage(pageReqVO);
    }

    @Override
    public FmsAuxiliaryItemDO getAuxiliaryItem(Long accountSetId, Long id, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询并校验辅助核算项目
        return validateAuxiliaryItemExists(accountSetId, id);
    }

    @Override
    public List<FmsAuxiliaryItemDO> getAuxiliaryItemList(Long accountSetId, Long auxiliaryTypeId, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 校验辅助核算类别
        validateAuxiliaryType(accountSetId, auxiliaryTypeId);

        // 2. 查询辅助核算项目
        return auxiliaryItemMapper.selectListByAccountSetIdAndAuxiliaryTypeId(
                accountSetId, auxiliaryTypeId);
    }

    @Override
    public List<FmsAuxiliaryItemDO> getAuxiliaryItemListByAccountSetIdAndAuxiliaryTypeIdAndStatus(
            Long accountSetId, Long auxiliaryTypeId, Integer status, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 校验辅助核算类别
        validateAuxiliaryType(accountSetId, auxiliaryTypeId);

        // 2. 查询指定状态的辅助核算项目
        return auxiliaryItemMapper.selectListByAccountSetIdAndAuxiliaryTypeIdAndStatus(
                accountSetId, auxiliaryTypeId, status);
    }

    @Override
    public List<FmsAuxiliaryItemDO> getAuxiliaryItemListByAccountSetId(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询账套的全部辅助核算项目
        return auxiliaryItemMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public Long getAuxiliaryItemCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId) {
        return auxiliaryItemMapper.selectCountByAuxiliaryTypeId(accountSetId, auxiliaryTypeId);
    }

    @Override
    public List<FmsAuxiliaryItemDO> validateAuxiliaryItemList(Long accountSetId, Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<FmsAuxiliaryItemDO> items = auxiliaryItemMapper.selectListByIdsAndAccountSetId(ids, accountSetId);
        if (items.size() != new LinkedHashSet<>(ids).size()) {
            throw exception(AUXILIARY_ITEM_NOT_EXISTS);
        }
        return items;
    }

    private FmsAuxiliaryItemDO validateAuxiliaryItemExists(Long accountSetId, Long id) {
        FmsAuxiliaryItemDO auxiliaryItem = auxiliaryItemMapper.selectById(id);
        if (auxiliaryItem == null || ObjUtil.notEqual(auxiliaryItem.getAccountSetId(), accountSetId)) {
            throw exception(AUXILIARY_ITEM_NOT_EXISTS);
        }
        return auxiliaryItem;
    }

    private void validateAuxiliaryType(Long accountSetId, Long auxiliaryTypeId) {
        auxiliaryTypeService.validateAuxiliaryTypeList(accountSetId, Collections.singletonList(auxiliaryTypeId));
    }

    private void validateAuxiliaryItemCodeUnique(Long id, Long accountSetId, Long auxiliaryTypeId, String code) {
        FmsAuxiliaryItemDO auxiliaryItem = auxiliaryItemMapper
                .selectByTypeIdAndCode(accountSetId, auxiliaryTypeId, code);
        if (auxiliaryItem != null && ObjUtil.notEqual(auxiliaryItem.getId(), id)) {
            throw exception(AUXILIARY_ITEM_CODE_DUPLICATE);
        }
    }

    private String buildImportLabel(Integer rowNumber, String code, String name) {
        return "第 " + rowNumber + " 行（" + StrUtil.blankToDefault(code, "未填写编码")
                + (StrUtil.isBlank(name) ? "" : " " + name) + "）";
    }

}

package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplatecategory.FmsVoucherTemplateCategorySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplate.FmsVoucherTemplateEntryVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplate.FmsVoucherTemplateSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateCategoryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherTemplateCategoryMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherTemplateMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.*;

/**
 * FMS 凭证模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsVoucherTemplateServiceImpl implements FmsVoucherTemplateService {

    @Resource
    private FmsVoucherTemplateCategoryMapper templateCategoryMapper;

    @Resource
    private FmsVoucherTemplateMapper templateMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsAuxiliaryItemService auxiliaryItemService;
    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;

    @Override
    @LogRecord(type = FMS_VOUCHER_TEMPLATE_CATEGORY_TYPE,
            subType = FMS_VOUCHER_TEMPLATE_CATEGORY_CREATE_SUB_TYPE,
            bizNo = "{{#categoryId}}", success = FMS_VOUCHER_TEMPLATE_CATEGORY_CREATE_SUCCESS)
    public Long createTemplateCategory(FmsVoucherTemplateCategorySaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验分类名称
        validateTemplateCategoryNameUnique(null, createReqVO.getAccountSetId(), createReqVO.getName());

        // 2. 创建模板分类
        FmsVoucherTemplateCategoryDO category = BeanUtils.toBean(
                createReqVO, FmsVoucherTemplateCategoryDO.class).setId(null);
        templateCategoryMapper.insert(category);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("categoryId", category.getId());
        return category.getId();
    }

    @Override
    @LogRecord(type = FMS_VOUCHER_TEMPLATE_CATEGORY_TYPE,
            subType = FMS_VOUCHER_TEMPLATE_CATEGORY_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_VOUCHER_TEMPLATE_CATEGORY_UPDATE_SUCCESS)
    public void updateTemplateCategory(FmsVoucherTemplateCategorySaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验模板分类
        validateTemplateCategory(updateReqVO.getAccountSetId(), updateReqVO.getId());
        // 1.3 校验分类名称唯一
        validateTemplateCategoryNameUnique(updateReqVO.getId(), updateReqVO.getAccountSetId(), updateReqVO.getName());

        // 2. 更新模板分类
        templateCategoryMapper.updateById(new FmsVoucherTemplateCategoryDO()
                .setId(updateReqVO.getId()).setName(updateReqVO.getName()));
    }

    @Override
    @LogRecord(type = FMS_VOUCHER_TEMPLATE_CATEGORY_TYPE,
            subType = FMS_VOUCHER_TEMPLATE_CATEGORY_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_VOUCHER_TEMPLATE_CATEGORY_DELETE_SUCCESS)
    public void deleteTemplateCategory(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验模板分类
        FmsVoucherTemplateCategoryDO category = validateTemplateCategory(accountSetId, id);
        // 1.3 校验模板分类未被使用
        if (templateMapper.selectCountByCategoryId(id) > 0) {
            throw exception(VOUCHER_TEMPLATE_CATEGORY_IN_USE);
        }

        // 2. 删除模板分类
        templateCategoryMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("category", category);
    }

    @Override
    public List<FmsVoucherTemplateCategoryDO> getTemplateCategoryList(Long accountSetId) {
        return templateCategoryMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    @LogRecord(type = FMS_VOUCHER_TEMPLATE_TYPE, subType = FMS_VOUCHER_TEMPLATE_CREATE_SUB_TYPE,
            bizNo = "{{#templateId}}", success = FMS_VOUCHER_TEMPLATE_CREATE_SUCCESS)
    public Long createVoucherTemplate(FmsVoucherTemplateSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验模板分类
        validateTemplateCategory(createReqVO.getAccountSetId(), createReqVO.getCategoryId());
        // 1.3 校验并构造模板分录
        List<FmsVoucherTemplateDO.Entry> entries = validateAndBuildTemplateEntries(
                createReqVO.getAccountSetId(), createReqVO.getEntries(), userId);

        // 2. 创建凭证模板
        FmsVoucherTemplateDO template = BeanUtils.toBean(createReqVO, FmsVoucherTemplateDO.class)
                .setId(null).setEntries(entries);
        templateMapper.insert(template);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("templateId", template.getId());
        return template.getId();
    }

    @Override
    @LogRecord(type = FMS_VOUCHER_TEMPLATE_TYPE, subType = FMS_VOUCHER_TEMPLATE_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_VOUCHER_TEMPLATE_UPDATE_SUCCESS)
    public void updateVoucherTemplate(FmsVoucherTemplateSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验凭证模板
        validateVoucherTemplate(updateReqVO.getAccountSetId(), updateReqVO.getId());
        // 1.3 校验模板分类
        validateTemplateCategory(updateReqVO.getAccountSetId(), updateReqVO.getCategoryId());
        // 1.4 校验并构造模板分录
        List<FmsVoucherTemplateDO.Entry> entries = validateAndBuildTemplateEntries(
                updateReqVO.getAccountSetId(), updateReqVO.getEntries(), userId);

        // 2. 更新凭证模板
        templateMapper.updateById(new FmsVoucherTemplateDO().setId(updateReqVO.getId())
                .setName(updateReqVO.getName())
                .setCategoryId(updateReqVO.getCategoryId()).setEntries(entries));
    }

    @Override
    @LogRecord(type = FMS_VOUCHER_TEMPLATE_TYPE, subType = FMS_VOUCHER_TEMPLATE_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_VOUCHER_TEMPLATE_DELETE_SUCCESS)
    public void deleteVoucherTemplate(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验凭证模板
        FmsVoucherTemplateDO template = validateVoucherTemplate(accountSetId, id);

        // 2. 删除凭证模板
        templateMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("template", template);
    }

    @Override
    public List<FmsVoucherTemplateDO> getVoucherTemplateList(Long accountSetId) {
        return templateMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public Long getVoucherTemplateCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        // 1. 处理空参数
        if (CollUtil.isEmpty(subjectIds)) {
            return 0L;
        }

        // 2. 查询凭证模板列表
        List<FmsVoucherTemplateDO> templates = templateMapper.selectListByAccountSetId(accountSetId);

        // 3. 计算使用指定科目的凭证模板数量
        Set<Long> subjectIdSet = new HashSet<>(subjectIds);
        return (long) filterList(templates, template -> CollUtil.isNotEmpty(template.getEntries())
                && template.getEntries().stream()
                        .map(FmsVoucherTemplateDO.Entry::getSubjectId)
                        .anyMatch(subjectIdSet::contains)).size();
    }

    @Override
    public Long getVoucherTemplateCountByAuxiliaryItemIds(
            Long accountSetId, Collection<Long> auxiliaryItemIds) {
        // 1. 处理空参数
        if (CollUtil.isEmpty(auxiliaryItemIds)) {
            return 0L;
        }

        // 2. 计算使用指定辅助核算项目的凭证模板数量
        Set<Long> auxiliaryItemIdSet = new HashSet<>(auxiliaryItemIds);
        return getVoucherTemplateCountByAuxiliary(accountSetId,
                item -> auxiliaryItemIdSet.contains(item.getItemId()));
    }

    @Override
    public Long getVoucherTemplateCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId) {
        return getVoucherTemplateCountByAuxiliary(accountSetId,
                item -> ObjUtil.equal(item.getTypeId(), auxiliaryTypeId));
    }

    private Long getVoucherTemplateCountByAuxiliary(Long accountSetId,
            Predicate<FmsVoucherEntryDO.AuxiliaryItem> predicate) {
        List<FmsVoucherTemplateDO> templates = templateMapper.selectListByAccountSetId(accountSetId);
        return (long) filterList(templates, template -> CollUtil.isNotEmpty(template.getEntries())
                && template.getEntries().stream()
                        .filter(entry -> CollUtil.isNotEmpty(entry.getAuxiliaries()))
                        .flatMap(entry -> entry.getAuxiliaries().stream())
                        .anyMatch(predicate)).size();
    }

    private FmsVoucherTemplateCategoryDO validateTemplateCategory(Long accountSetId, Long id) {
        FmsVoucherTemplateCategoryDO category = templateCategoryMapper.selectById(id);
        if (category == null || ObjUtil.notEqual(category.getAccountSetId(), accountSetId)) {
            throw exception(VOUCHER_TEMPLATE_CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    private void validateTemplateCategoryNameUnique(Long id, Long accountSetId, String name) {
        FmsVoucherTemplateCategoryDO category = templateCategoryMapper
                .selectByAccountSetIdAndName(accountSetId, name);
        if (category != null && ObjUtil.notEqual(category.getId(), id)) {
            throw exception(VOUCHER_TEMPLATE_CATEGORY_NAME_DUPLICATE);
        }
    }

    private FmsVoucherTemplateDO validateVoucherTemplate(Long accountSetId, Long id) {
        FmsVoucherTemplateDO template = templateMapper.selectById(id);
        if (template == null || ObjUtil.notEqual(template.getAccountSetId(), accountSetId)) {
            throw exception(VOUCHER_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private List<FmsVoucherTemplateDO.Entry> validateAndBuildTemplateEntries(
            Long accountSetId, List<FmsVoucherTemplateEntryVO> entries, Long userId) {
        // 1. 批量校验科目
        Map<Long, FmsSubjectDO> subjectMap = convertMap(
                subjectService.getSubjectList(accountSetId, null, userId), FmsSubjectDO::getId);
        Set<Long> subjectIds = convertSet(entries, FmsVoucherTemplateEntryVO::getSubjectId);
        if (!subjectMap.keySet().containsAll(subjectIds)) {
            throw exception(SUBJECT_NOT_EXISTS);
        }
        Set<Long> parentSubjectIds = convertSet(subjectMap.values(), FmsSubjectDO::getParentId);
        BigDecimal debitAmount = BigDecimal.ZERO;
        BigDecimal creditAmount = BigDecimal.ZERO;
        boolean saveAmount = false;
        for (FmsVoucherTemplateEntryVO entry : entries) {
            FmsSubjectDO subject = subjectMap.get(entry.getSubjectId());
            if (ObjUtil.notEqual(subject.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                throw exception(VOUCHER_SUBJECT_DISABLED);
            }
            if (parentSubjectIds.contains(subject.getId())) {
                throw exception(VOUCHER_SUBJECT_HAS_CHILDREN);
            }
            BigDecimal entryDebitAmount = ObjUtil.defaultIfNull(entry.getDebitAmount(), BigDecimal.ZERO);
            BigDecimal entryCreditAmount = ObjUtil.defaultIfNull(entry.getCreditAmount(), BigDecimal.ZERO);
            if (entryDebitAmount.signum() != 0 && entryCreditAmount.signum() != 0) {
                throw exception(VOUCHER_ENTRY_AMOUNT_INVALID);
            }
            BigDecimal entryAmount = entryDebitAmount.abs().max(entryCreditAmount.abs());
            validateTemplateQuantity(entry, subject, entryAmount);
            debitAmount = debitAmount.add(entryDebitAmount);
            creditAmount = creditAmount.add(entryCreditAmount);
            saveAmount = saveAmount || entryAmount.signum() > 0;
        }
        if (saveAmount && debitAmount.compareTo(creditAmount) != 0) {
            throw exception(VOUCHER_AMOUNT_UNBALANCED);
        }

        // 2. 批量校验辅助核算类别和项目
        Set<Long> auxiliaryItemIds = CollUtil.newHashSet();
        Set<Long> auxiliaryTypeIds = CollUtil.newHashSet();
        entries.forEach(entry -> {
            if (CollUtil.isNotEmpty(entry.getAuxiliaries())) {
                auxiliaryItemIds.addAll(convertSet(entry.getAuxiliaries(), FmsVoucherTemplateEntryVO.AuxiliaryItem::getItemId));
                auxiliaryTypeIds.addAll(convertSet(entry.getAuxiliaries(), FmsVoucherTemplateEntryVO.AuxiliaryItem::getTypeId));
            }
        });
        Map<Long, FmsAuxiliaryItemDO> auxiliaryItemMap = convertMap(
                auxiliaryItemService.validateAuxiliaryItemList(accountSetId, auxiliaryItemIds),
                FmsAuxiliaryItemDO::getId);
        Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap = convertMap(
                auxiliaryTypeService.validateAuxiliaryTypeList(accountSetId, auxiliaryTypeIds),
                FmsAuxiliaryTypeDO::getId);

        // 3. 构造模板 JSON 分录
        return convertList(entries, source -> {
            validateTemplateAuxiliaries(source, subjectMap.get(source.getSubjectId()));
            FmsVoucherTemplateDO.Entry target = BeanUtils.toBean(source, FmsVoucherTemplateDO.Entry.class);
            if (CollUtil.isEmpty(source.getAuxiliaries())) {
                target.setAuxiliaries(Collections.emptyList());
                return target;
            }
            target.setAuxiliaries(convertList(source.getAuxiliaries(), itemSource -> {
                FmsAuxiliaryItemDO auxiliaryItem = auxiliaryItemMap.get(itemSource.getItemId());
                if (auxiliaryItem == null
                        || ObjUtil.notEqual(auxiliaryItem.getAuxiliaryTypeId(), itemSource.getTypeId())) {
                    throw exception(AUXILIARY_ITEM_NOT_EXISTS);
                }
                if (ObjUtil.notEqual(auxiliaryItem.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                    throw exception(VOUCHER_AUXILIARY_REQUIRED);
                }
                return BeanUtils.toBean(itemSource, FmsVoucherEntryDO.AuxiliaryItem.class)
                        .setType(auxiliaryTypeMap.get(itemSource.getTypeId()).getType())
                        .setName(auxiliaryItem.getName());
            }));
            return target;
        });
    }

    private void validateTemplateQuantity(FmsVoucherTemplateEntryVO entry,
            FmsSubjectDO subject, BigDecimal amount) {
        BigDecimal quantity = ObjUtil.defaultIfNull(entry.getQuantity(), BigDecimal.ZERO);
        BigDecimal unitPrice = ObjUtil.defaultIfNull(entry.getUnitPrice(), BigDecimal.ZERO);
        if (Boolean.FALSE.equals(subject.getQuantityAccounting())) {
            if (quantity.signum() > 0 || unitPrice.signum() > 0) {
                throw exception(VOUCHER_QUANTITY_INVALID);
            }
            return;
        }
        if (quantity.signum() == 0 && unitPrice.signum() == 0) {
            return;
        }
        if (quantity.signum() <= 0 || unitPrice.signum() <= 0
                || quantity.multiply(unitPrice).setScale(2, RoundingMode.FLOOR).compareTo(amount) != 0) {
            throw exception(VOUCHER_QUANTITY_INVALID);
        }
    }

    private void validateTemplateAuxiliaries(FmsVoucherTemplateEntryVO entry, FmsSubjectDO subject) {
        List<Long> auxiliaryTypeIds = ObjUtil.defaultIfNull(subject.getAuxiliaryTypeIds(), Collections.emptyList());
        List<FmsVoucherTemplateEntryVO.AuxiliaryItem> auxiliaries = ObjUtil.defaultIfNull(entry.getAuxiliaries(), Collections.emptyList());
        Set<Long> requestedTypeIds = convertSet(auxiliaries, FmsVoucherTemplateEntryVO.AuxiliaryItem::getTypeId);
        if (auxiliaryTypeIds.size() != auxiliaries.size()
                || !requestedTypeIds.containsAll(auxiliaryTypeIds)) {
            throw exception(VOUCHER_AUXILIARY_REQUIRED);
        }
    }

}

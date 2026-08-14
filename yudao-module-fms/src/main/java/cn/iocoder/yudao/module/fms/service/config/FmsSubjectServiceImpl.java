package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectDeleteReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectStatusReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectUsageRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectTemplateDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsSubjectMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsSubjectTemplateMapper;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectCategoryEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingTemplateService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_CODE_RULE_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_CURRENCY_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_HAS_CHILDREN;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_AUXILIARY_COMBINATION_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_AUXILIARY_MIGRATION_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_CLOSING_SCHEME_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_CLOSING_TEMPLATE_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_INITIAL_BALANCE_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_IMPORT_LIST_IS_EMPTY;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_NON_LEAF_CONFIG_IMMUTABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_PARENT_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_PARENT_CONFIG_INCOMPATIBLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_PARENT_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_QUANTITY_ACCOUNTING_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_USED_AUXILIARY_IMMUTABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_USED_BALANCE_DIRECTION_IMMUTABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_VOUCHER_ENTRY_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_VOUCHER_TEMPLATE_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_STATUS_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_STATUS_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_SUBJECT_UPDATE_SUCCESS;

/**
 * FMS 会计科目 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsSubjectServiceImpl implements FmsSubjectService {

    @Resource
    private FmsSubjectMapper subjectMapper;
    @Resource
    private FmsSubjectTemplateMapper subjectTemplateMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsFinanceParameterService financeParameterService;
    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @Resource
    private FmsAuxiliaryItemService auxiliaryItemService;
    @Resource
    private FmsCurrencyService currencyService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private FmsVoucherService voucherService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private FmsVoucherTemplateService voucherTemplateService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private FmsInitialBalanceService initialBalanceService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private FmsClosingSchemeService closingSchemeService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private FmsClosingTemplateService closingTemplateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeDefaultSubjects(Long accountSetId) {
        // 1.1 查询标准科目模板并按编码排序
        List<FmsSubjectTemplateDO> templates = subjectTemplateMapper.selectList();
        if (CollUtil.isEmpty(templates)) {
            return;
        }
        templates.sort(Comparator.comparing(FmsSubjectTemplateDO::getCode));
        // 1.2 标准模板编码需要按账套科目编码规则转换后再创建
        String subjectCodeRule = financeParameterService.getFinanceParameter(accountSetId).getSubjectCodeRule();

        // 2. 按模板层级创建账套科目
        Map<Long, Long> templateSubjectIdMap = new HashMap<>(templates.size());
        for (FmsSubjectTemplateDO template : templates) {
            FmsSubjectDO subject = BeanUtils.toBean(template, FmsSubjectDO.class)
                    .setId(null).setAccountSetId(accountSetId)
                    .setCode(financeParameterService.convertStandardSubjectCode(template.getCode(), subjectCodeRule))
                    .setParentId(FmsSubjectTemplateDO.PARENT_ID_ROOT.equals(template.getParentId())
                            ? FmsSubjectDO.PARENT_ID_ROOT : templateSubjectIdMap.get(template.getParentId()))
                    .setAuxiliaryTypeIds(Collections.emptyList()).setCurrencyIds(Collections.emptyList());
            subjectMapper.insert(subject);
            templateSubjectIdMap.put(template.getId(), subject.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_SUBJECT_TYPE, subType = FMS_SUBJECT_CREATE_SUB_TYPE,
            bizNo = "{{#subjectId}}", success = FMS_SUBJECT_CREATE_SUCCESS)
    public Long createSubject(FmsSubjectSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 查询财务参数
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(createReqVO.getAccountSetId());
        // 1.3 校验科目编码唯一
        validateSubjectCodeUnique(null, createReqVO.getAccountSetId(), createReqVO.getCode());
        // 1.4 校验上级科目和科目编码规则
        FmsSubjectDO parent = validateParentSubject(createReqVO.getAccountSetId(),
                createReqVO.getParentId(), createReqVO.getType(), createReqVO.getCategory());
        FmsSubjectUsageRespVO parentUsage = validateParentSubjectDataMigration(createReqVO, parent);
        int level = parent == null ? 1 : parent.getLevel() + 1;
        validateSubjectCodeRule(createReqVO.getCode(), level, parent, financeParameter);
        // 1.5 校验辅助核算类别
        auxiliaryTypeService.validateAuxiliaryTypeList(
                createReqVO.getAccountSetId(), createReqVO.getAuxiliaryTypeIds());
        // 1.6 校验外币核算币别
        validateCurrencyList(createReqVO.getAccountSetId(), createReqVO.getCurrencyIds());

        // 2. 创建科目
        FmsSubjectDO subject = BeanUtils.toBean(createReqVO, FmsSubjectDO.class)
                .setId(null).setLevel(level).setStatus(CommonStatusEnum.ENABLE.getStatus());
        subjectMapper.insert(subject);

        // 3. 迁移上级科目的历史业务数据
        if (parent != null && Boolean.TRUE.equals(parentUsage.getUsed())) {
            voucherService.updateVoucherEntrySubject(createReqVO.getAccountSetId(), parent.getId(), subject);
            initialBalanceService.updateInitialBalanceSubject(
                    createReqVO.getAccountSetId(), parent.getId(), subject.getId());
            auxiliaryCombinationService.updateAuxiliaryCombinationSubject(
                    createReqVO.getAccountSetId(), parent.getId(), subject.getId());
        }
        // 父科目成为非末级后，辅助核算配置不再生效，避免残留配置继续占用辅助类别
        if (parent != null && CollUtil.isNotEmpty(parent.getAuxiliaryTypeIds())) {
            subjectMapper.updateById(new FmsSubjectDO().setId(parent.getId())
                    .setAuxiliaryTypeIds(Collections.emptyList()));
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("subjectId", subject.getId());
        return subject.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_SUBJECT_TYPE, subType = FMS_SUBJECT_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_SUBJECT_UPDATE_SUCCESS)
    public void updateSubject(FmsSubjectSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 查询财务参数
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(
                updateReqVO.getAccountSetId());
        // 1.3 校验科目存在
        FmsSubjectDO subject = validateSubjectExists(updateReqVO.getAccountSetId(), updateReqVO.getId());
        if (ObjUtil.notEqual(subject.getParentId(), updateReqVO.getParentId())) {
            throw exception(SUBJECT_PARENT_INVALID);
        }
        // 1.4 校验已用科目的配置修改范围
        FmsSubjectUsageRespVO usage = buildSubjectUsage(updateReqVO.getAccountSetId(), subject.getId());
        validateSubjectConfigUpdate(subject, updateReqVO, usage);
        // 1.5 校验科目编码唯一
        validateSubjectCodeUnique(subject.getId(), updateReqVO.getAccountSetId(), updateReqVO.getCode());
        // 1.6 校验上级科目和科目编码规则
        FmsSubjectDO parent = validateParentSubject(updateReqVO.getAccountSetId(),
                updateReqVO.getParentId(), updateReqVO.getType(), updateReqVO.getCategory());
        validateSubjectCodeRule(updateReqVO.getCode(), subject.getLevel(), parent, financeParameter);
        // 1.7 校验有下级科目时不能修改编码
        if (ObjUtil.notEqual(subject.getCode(), updateReqVO.getCode())
                && usage.getChildCount() > 0) {
            throw exception(SUBJECT_HAS_CHILDREN);
        }
        // 1.8 校验辅助核算类别和外币核算币别
        List<FmsAuxiliaryTypeDO> auxiliaryTypes = auxiliaryTypeService.validateAuxiliaryTypeList(
                updateReqVO.getAccountSetId(), updateReqVO.getAuxiliaryTypeIds());
        validateCurrencyList(updateReqVO.getAccountSetId(), updateReqVO.getCurrencyIds());
        // 1.9 校验已用科目首次启用辅助核算的历史数据迁移项目
        Map<Long, FmsAuxiliaryItemDO> migrationItemMap = validateSubjectAuxiliaryMigration(
                subject, updateReqVO, usage);

        // 2. 更新科目
        subjectMapper.updateById(BeanUtils.toBean(updateReqVO, FmsSubjectDO.class)
                .setParentId(subject.getParentId()).setLevel(subject.getLevel()));

        // 3. 迁移科目的历史辅助核算数据
        if (CollUtil.isNotEmpty(migrationItemMap)) {
            Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap = convertMap(auxiliaryTypes, FmsAuxiliaryTypeDO::getId);
            List<FmsAuxiliaryCombinationDO.AuxiliaryItem> combinationItems = convertList(updateReqVO.getAuxiliaryTypeIds(), typeId -> {
                FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeMap.get(typeId);
                FmsAuxiliaryItemDO auxiliaryItem = migrationItemMap.get(typeId);
                return FmsAuxiliaryCombinationDO.AuxiliaryItem.builder()
                        .type(auxiliaryType.getType()).typeId(typeId)
                        .itemId(auxiliaryItem.getId()).name(auxiliaryItem.getName()).build();
            });
            FmsAuxiliaryCombinationDO combination = auxiliaryCombinationService.saveAuxiliaryCombination(
                    updateReqVO.getAccountSetId(), subject.getId(), combinationItems);
            voucherService.migrateVoucherEntryAuxiliaries(updateReqVO.getAccountSetId(), subject.getId(), combination);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_SUBJECT_TYPE, subType = FMS_SUBJECT_DELETE_SUB_TYPE,
            bizNo = "{{#subjects.iterator().next().id}}", success = FMS_SUBJECT_DELETE_SUCCESS)
    public void deleteSubjectList(FmsSubjectDeleteReqVO deleteReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(deleteReqVO.getAccountSetId(), userId);
        // 1.2 校验科目存在
        List<FmsSubjectDO> subjects = subjectMapper.selectListByIdsAndAccountSetId(
                deleteReqVO.getIds(), deleteReqVO.getAccountSetId());
        if (subjects.size() != deleteReqVO.getIds().size()) {
            throw exception(SUBJECT_NOT_EXISTS);
        }
        // 1.3 校验科目没有下级
        if (subjectMapper.selectCountByParentIds(deleteReqVO.getIds()) > 0) {
            throw exception(SUBJECT_HAS_CHILDREN);
        }
        // 1.4 校验科目未被业务引用
        validateSubjectNotInUse(deleteReqVO.getAccountSetId(), deleteReqVO.getIds());

        // 2. 删除科目
        subjectMapper.deleteByIds(deleteReqVO.getIds());

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("subjects", subjects);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_SUBJECT_TYPE, subType = FMS_SUBJECT_STATUS_SUB_TYPE,
            bizNo = "{{#statusReqVO.accountSetId}}", success = FMS_SUBJECT_STATUS_SUCCESS)
    public void updateSubjectStatus(FmsSubjectStatusReqVO statusReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(statusReqVO.getAccountSetId(), userId);
        // 1.2 校验科目存在
        List<FmsSubjectDO> subjects = subjectMapper.selectListByIdsAndAccountSetId(
                statusReqVO.getIds(), statusReqVO.getAccountSetId());
        if (subjects.size() != statusReqVO.getIds().size()) {
            throw exception(SUBJECT_NOT_EXISTS);
        }

        // 2. 递归收集选中科目的全部子孙，批量更新状态
        Set<Long> subjectIds = getSubjectIdSetWithChildren(
                statusReqVO.getAccountSetId(), statusReqVO.getIds());
        subjectMapper.updateStatusByIdsAndAccountSetId(
                subjectIds, statusReqVO.getAccountSetId(),
                new FmsSubjectDO().setStatus(statusReqVO.getStatus()));
    }

    @Override
    public FmsSubjectDO getSubject(Long accountSetId, Long id, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询科目
        return subjectMapper.selectByIdAndAccountSetId(id, accountSetId);
    }

    @Override
    public FmsSubjectUsageRespVO getSubjectUsage(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 校验科目存在
        validateSubjectExists(accountSetId, id);

        // 2. 统计科目使用情况
        return buildSubjectUsage(accountSetId, id);
    }

    @Override
    public List<FmsSubjectDO> getSubjectList(Long accountSetId, Integer type, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询科目列表
        return subjectMapper.selectListByAccountSetIdAndType(accountSetId, type);
    }

    @Override
    public List<FmsSubjectDO> getSubjectList(FmsSubjectListReqVO listReqVO, Long userId) {
        return getSubjectList(listReqVO.getAccountSetId(), listReqVO.getType(), userId);
    }

    @Override
    public List<Long> getSubjectIdListWithChildren(Long accountSetId, Long id) {
        // 1. 校验科目存在
        validateSubjectExists(accountSetId, id);

        // 2. 获得科目及全部下级科目
        return new ArrayList<>(getSubjectIdSetWithChildren(accountSetId, Collections.singletonList(id)));
    }

    /**
     * 按层查询并收集指定科目及全部子孙科目编号
     *
     * @param accountSetId 账套编号
     * @param ids 起始科目编号集合
     * @return 科目编号集合
     */
    private Set<Long> getSubjectIdSetWithChildren(Long accountSetId, Collection<Long> ids) {
        Set<Long> subjectIds = new LinkedHashSet<>(ids);
        Collection<Long> parentIds = ids;
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            List<FmsSubjectDO> children = subjectMapper.selectListByParentIds(accountSetId, parentIds);
            if (CollUtil.isEmpty(children)) {
                break;
            }
            parentIds = convertList(children, FmsSubjectDO::getId);
            subjectIds.addAll(parentIds);
        }
        return subjectIds;
    }

    @Override
    public Long getSubjectCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId) {
        List<FmsSubjectDO> subjects = subjectMapper.selectListByAccountSetIdAndType(accountSetId, null);
        return (long) filterList(subjects,
                subject -> CollUtil.contains(subject.getAuxiliaryTypeIds(), auxiliaryTypeId)).size();
    }

    @Override
    public Long getSubjectCountByCurrencyId(Long accountSetId, Long currencyId) {
        List<FmsSubjectDO> subjects = subjectMapper.selectListByAccountSetIdAndType(accountSetId, null);
        return (long) filterList(subjects,
                subject -> CollUtil.contains(subject.getCurrencyIds(), currencyId)).size();
    }

    @Override
    public void expandSubjectCodes(Long accountSetId, List<Integer> oldRules, List<Integer> newRules) {
        // 1. 查询账套全部会计科目
        List<FmsSubjectDO> subjects = subjectMapper.selectListByAccountSetIdAndType(accountSetId, null);
        if (CollUtil.isEmpty(subjects)) {
            return;
        }

        // 2. 按各级编码增量扩展会计科目编码
        List<FmsSubjectDO> updateSubjects = new ArrayList<>();
        for (FmsSubjectDO subject : subjects) {
            String expandedCode = expandSubjectCode(subject.getCode(), subject.getLevel(), oldRules, newRules);
            if (ObjUtil.notEqual(expandedCode, subject.getCode())) {
                updateSubjects.add(new FmsSubjectDO().setId(subject.getId()).setCode(expandedCode));
            }
        }
        if (CollUtil.isNotEmpty(updateSubjects)) {
            subjectMapper.updateBatch(updateSubjects);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmsSubjectImportRespVO importSubjectList(Long accountSetId,
            List<FmsSubjectImportExcelVO> importSubjects, Long userId) {
        // 1.1 校验导入数据非空
        if (CollUtil.isEmpty(importSubjects)) {
            throw exception(SUBJECT_IMPORT_LIST_IS_EMPTY);
        }
        // 1.2 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.3 加载导入依赖
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(accountSetId);
        Map<String, FmsSubjectDO> subjectMap = convertMap(
                subjectMapper.selectListByAccountSetIdAndType(accountSetId, null), FmsSubjectDO::getCode);
        Map<String, Long> auxiliaryTypeIdMap = auxiliaryTypeService.getAuxiliaryTypeIdMap(accountSetId);

        // 2. 按科目级次依次导入
        List<FmsSubjectImportExcelVO> sortedSubjects = new ArrayList<>(importSubjects);
        sortedSubjects.sort(Comparator.comparingInt(item -> StrUtil.length(item.getCode())));
        Set<String> importCodes = new HashSet<>();
        FmsSubjectImportRespVO respVO = new FmsSubjectImportRespVO()
                .setTotalCount(importSubjects.size()).setSuccessSubjectCodes(new ArrayList<>())
                .setFailureReasons(new LinkedHashMap<>());
        for (int index = 0; index < sortedSubjects.size(); index++) {
            FmsSubjectImportExcelVO importSubject = sortedSubjects.get(index);
            String subjectCode = importSubject.getCode();
            String label = buildImportLabel(index + 2, subjectCode, importSubject.getName());
            try {
                // 2.1 校验导入字段和科目编码
                ValidationUtils.validate(importSubject);
                if (!importCodes.add(subjectCode)) {
                    throw new IllegalArgumentException("导入文件内科目编码重复");
                }
                FmsSubjectDO existingSubject = subjectMap.get(subjectCode);
                FmsSubjectCategoryEnum category = FmsSubjectCategoryEnum.valueOfName(
                        importSubject.getCategoryName());
                if (category == null) {
                    throw new IllegalArgumentException("科目类别不存在");
                }
                FmsDebitCreditDirectionEnum direction = FmsDebitCreditDirectionEnum.valueOfName(
                        importSubject.getBalanceDirection());
                if (direction == null) {
                    throw new IllegalArgumentException("余额方向只能填写借或贷");
                }

                // 2.2 解析上级科目和辅助核算类别
                FmsSubjectDO parent = resolveImportParent(importSubject.getParentSubjectCode(), subjectMap);
                if (parent != null && (ObjUtil.notEqual(parent.getType(), category.getType())
                        || ObjUtil.notEqual(parent.getCategory(), category.getCategory()))) {
                    throw new IllegalArgumentException("上级科目与当前科目类型或类别不一致");
                }
                List<Long> auxiliaryTypeIds = resolveImportAuxiliaryTypeIds(
                        importSubject.getAuxiliaryNames(), auxiliaryTypeIdMap);

                // 2.3 构造并创建或安全覆盖科目
                FmsSubjectSaveReqVO reqVO = new FmsSubjectSaveReqVO();
                reqVO.setAccountSetId(accountSetId).setCode(subjectCode)
                        .setName(importSubject.getName())
                        .setParentId(parent == null ? FmsSubjectDO.PARENT_ID_ROOT : parent.getId())
                        .setType(category.getType()).setCategory(category.getCategory())
                        .setBalanceDirection(direction.getType()).setAuxiliaryTypeIds(auxiliaryTypeIds)
                        .setCurrencyIds(Collections.emptyList()).setQuantityAccounting(false).setCash(false);
                FmsSubjectDO subject;
                if (existingSubject == null) {
                    subject = createImportedSubject(reqVO, parent, financeParameter);
                } else {
                    subject = updateImportedSubject(existingSubject, reqVO, parent, financeParameter);
                }

                // 2.4 记录导入结果
                subjectMap.put(subjectCode, subject);
                respVO.getSuccessSubjectCodes().add(subjectCode);
            } catch (ConstraintViolationException exception) {
                respVO.getFailureReasons().put(label, exception.getMessage());
            } catch (RuntimeException exception) {
                respVO.getFailureReasons().put(label, StrUtil.blankToDefault(exception.getMessage(), "导入失败"));
            }
        }
        return respVO;
    }

    private FmsSubjectDO createImportedSubject(FmsSubjectSaveReqVO reqVO, FmsSubjectDO parent,
            FmsFinanceParameterDO financeParameter) {
        // 1.1 校验科目编码唯一
        validateSubjectCodeUnique(null, reqVO.getAccountSetId(), reqVO.getCode());
        // 1.2 校验科目层级和编码规则
        int level = parent == null ? 1 : parent.getLevel() + 1;
        validateSubjectCodeRule(reqVO.getCode(), level, parent, financeParameter);
        // 1.3 校验上级科目没有历史业务数据
        validateParentSubjectUnused(reqVO.getAccountSetId(), parent);
        // 1.4 校验辅助核算类别
        auxiliaryTypeService.validateAuxiliaryTypeList(reqVO.getAuxiliaryTypeIds());

        // 2. 创建科目
        FmsSubjectDO subject = BeanUtils.toBean(reqVO, FmsSubjectDO.class)
                .setId(null).setLevel(level).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setCurrencyIds(Collections.emptyList());
        subjectMapper.insert(subject);
        return subject;
    }

    /**
     * 覆盖导入时已存在但未被业务使用的科目
     *
     * @param subject 已存在的科目
     * @param reqVO 导入科目信息
     * @param parent 新上级科目
     * @param financeParameter 财务参数
     * @return 覆盖后的科目
     */
    private FmsSubjectDO updateImportedSubject(FmsSubjectDO subject, FmsSubjectSaveReqVO reqVO,
            FmsSubjectDO parent, FmsFinanceParameterDO financeParameter) {
        // 1. 仅允许覆盖没有下级、没有任何业务引用的科目
        if (subjectMapper.selectCountByParentId(subject.getId()) > 0) {
            throw new IllegalArgumentException("已有下级科目，不能覆盖");
        }
        validateSubjectNotInUse(reqVO.getAccountSetId(), Collections.singletonList(subject.getId()));

        // 2. 校验新上级和导入配置
        int level = parent == null ? 1 : parent.getLevel() + 1;
        validateSubjectCodeRule(reqVO.getCode(), level, parent, financeParameter);
        validateParentSubjectUnused(reqVO.getAccountSetId(), parent);
        auxiliaryTypeService.validateAuxiliaryTypeList(reqVO.getAuxiliaryTypeIds());

        // 3. 保留主键和状态，覆盖可导入的科目配置
        FmsSubjectDO updateObj = BeanUtils.toBean(reqVO, FmsSubjectDO.class)
                .setId(subject.getId()).setLevel(level).setStatus(subject.getStatus())
                .setCurrencyIds(Collections.emptyList());
        subjectMapper.updateById(updateObj);
        if (parent != null && CollUtil.isNotEmpty(parent.getAuxiliaryTypeIds())) {
            subjectMapper.updateById(new FmsSubjectDO().setId(parent.getId()).setAuxiliaryTypeIds(Collections.emptyList()));
        }
        return updateObj;
    }

    /**
     * 按新旧编码规则扩展科目编码
     *
     * @param subjectCode 科目编码
     * @param level 科目层级
     * @param oldRules 原编码规则
     * @param newRules 新编码规则
     * @return 扩展后的科目编码
     */
    private String expandSubjectCode(String subjectCode, Integer level,
            List<Integer> oldRules, List<Integer> newRules) {
        StringBuilder result = new StringBuilder();
        int offset = 0;
        for (int index = 0; index < level; index++) {
            int oldLength = oldRules.get(index);
            int increment = newRules.get(index) - oldLength;
            String segment = subjectCode.substring(offset, offset + oldLength);
            if (increment > 0) {
                if (index == 0) {
                    result.append(segment.charAt(0)).append(CharSequenceUtil.repeat('0', increment))
                            .append(segment.substring(1));
                } else {
                    result.append(CharSequenceUtil.repeat('0', increment)).append(segment);
                }
            } else {
                result.append(segment);
            }
            offset += oldLength;
        }
        return result.toString();
    }

    private FmsSubjectDO resolveImportParent(String parentSubjectCode, Map<String, FmsSubjectDO> subjectMap) {
        if (StrUtil.isBlank(parentSubjectCode) || "0".equals(parentSubjectCode)) {
            return null;
        }
        FmsSubjectDO parent = subjectMap.get(parentSubjectCode);
        if (parent == null) {
            throw new IllegalArgumentException("上级科目编码不存在");
        }
        return parent;
    }

    private List<Long> resolveImportAuxiliaryTypeIds(String auxiliaryNames, Map<String, Long> auxiliaryTypeIdMap) {
        if (StrUtil.isBlank(auxiliaryNames)) {
            return Collections.emptyList();
        }
        List<Long> auxiliaryTypeIds = new ArrayList<>();
        for (String auxiliaryName : StrUtil.split(auxiliaryNames.replace('、', '/'), '/')) {
            Long auxiliaryTypeId = auxiliaryTypeIdMap.get(auxiliaryName);
            if (auxiliaryTypeId == null) {
                throw new IllegalArgumentException("辅助核算类别“" + auxiliaryName + "”不存在");
            }
            auxiliaryTypeIds.add(auxiliaryTypeId);
        }
        return auxiliaryTypeIds;
    }

    private String buildImportLabel(int rowNumber, String subjectCode, String name) {
        return "第 " + rowNumber + " 行（" + StrUtil.blankToDefault(subjectCode, "未填写编码")
                + (StrUtil.isBlank(name) ? "" : " " + name) + "）";
    }

    private FmsSubjectDO validateSubjectExists(Long accountSetId, Long id) {
        FmsSubjectDO subject = subjectMapper.selectById(id);
        if (subject == null || ObjUtil.notEqual(subject.getAccountSetId(), accountSetId)) {
            throw exception(SUBJECT_NOT_EXISTS);
        }
        return subject;
    }

    private void validateSubjectNotInUse(Long accountSetId, List<Long> subjectIds) {
        // 1. 校验科目未被凭证分录使用
        Long voucherEntryCount = voucherService.getVoucherEntryCountBySubjectIds(accountSetId, subjectIds);
        if (voucherEntryCount > 0) {
            throw exception(SUBJECT_VOUCHER_ENTRY_IN_USE, voucherEntryCount);
        }
        // 2. 校验科目未被凭证模板使用
        Long voucherTemplateCount = voucherTemplateService.getVoucherTemplateCountBySubjectIds(
                accountSetId, subjectIds);
        if (voucherTemplateCount > 0) {
            throw exception(SUBJECT_VOUCHER_TEMPLATE_IN_USE, voucherTemplateCount);
        }
        // 3. 校验科目未被初始余额使用
        Long initialBalanceCount = initialBalanceService.getInitialBalanceCountBySubjectIds(
                accountSetId, subjectIds);
        if (initialBalanceCount > 0) {
            throw exception(SUBJECT_INITIAL_BALANCE_IN_USE, initialBalanceCount);
        }
        // 4. 校验科目未被辅助核算组合使用
        Long auxiliaryCombinationCount = auxiliaryCombinationService.getAuxiliaryCombinationCountBySubjectIds(
                accountSetId, subjectIds);
        if (auxiliaryCombinationCount > 0) {
            throw exception(SUBJECT_AUXILIARY_COMBINATION_IN_USE, auxiliaryCombinationCount);
        }
        // 5. 校验科目未被结账方案使用
        Long closingSchemeCount = closingSchemeService.getClosingSchemeCountBySubjectIds(accountSetId, subjectIds);
        if (closingSchemeCount > 0) {
            throw exception(SUBJECT_CLOSING_SCHEME_IN_USE, closingSchemeCount);
        }
        // 6. 校验科目未被结账模板使用
        Long closingTemplateCount = closingTemplateService.getClosingTemplateCountBySubjectIds(
                accountSetId, subjectIds);
        if (closingTemplateCount > 0) {
            throw exception(SUBJECT_CLOSING_TEMPLATE_IN_USE, closingTemplateCount);
        }
    }

    /**
     * 构建科目使用情况
     *
     * @param accountSetId 账套编号
     * @param subjectId 科目编号
     * @return 科目使用情况
     */
    private FmsSubjectUsageRespVO buildSubjectUsage(Long accountSetId, Long subjectId) {
        // 1. 查询科目的业务引用数量
        List<Long> subjectIds = Collections.singletonList(subjectId);
        Long voucherEntryCount = ObjUtil.defaultIfNull(
                voucherService.getVoucherEntryCountBySubjectIds(accountSetId, subjectIds), 0L);
        Long initialBalanceCount = ObjUtil.defaultIfNull(
                initialBalanceService.getInitialBalanceCountBySubjectIds(accountSetId, subjectIds), 0L);
        Long auxiliaryCombinationCount = ObjUtil.defaultIfNull(
                auxiliaryCombinationService.getAuxiliaryCombinationCountBySubjectIds(accountSetId, subjectIds), 0L);

        // 2. 查询科目的数量核算数据数量
        Long voucherEntryQuantityCount = ObjUtil.defaultIfNull(
                voucherService.getVoucherEntryQuantityCountBySubjectIds(accountSetId, subjectIds), 0L);
        Long initialBalanceQuantityCount = ObjUtil.defaultIfNull(
                initialBalanceService.getInitialBalanceQuantityCountBySubjectIds(accountSetId, subjectIds), 0L);
        Long quantityDataCount = voucherEntryQuantityCount + initialBalanceQuantityCount;

        // 3. 构建科目使用情况
        return new FmsSubjectUsageRespVO()
                .setChildCount(subjectMapper.selectCountByParentId(subjectId))
                .setVoucherEntryCount(voucherEntryCount).setInitialBalanceCount(initialBalanceCount)
                .setAuxiliaryCombinationCount(auxiliaryCombinationCount).setQuantityDataCount(quantityDataCount)
                .setUsed(voucherEntryCount + initialBalanceCount + auxiliaryCombinationCount > 0);
    }

    /**
     * 校验上级科目未被业务使用
     *
     * @param accountSetId 账套编号
     * @param parent 上级科目
     */
    private void validateParentSubjectUnused(Long accountSetId, FmsSubjectDO parent) {
        if (parent != null && Boolean.TRUE.equals(buildSubjectUsage(accountSetId, parent.getId()).getUsed())) {
            throw exception(SUBJECT_PARENT_IN_USE);
        }
    }

    /**
     * 校验上级科目历史数据迁移
     *
     * @param createReqVO 创建信息
     * @param parent 上级科目
     * @return 上级科目使用情况
     */
    private FmsSubjectUsageRespVO validateParentSubjectDataMigration(
            FmsSubjectSaveReqVO createReqVO, FmsSubjectDO parent) {
        if (parent == null) {
            return new FmsSubjectUsageRespVO().setUsed(false);
        }
        FmsSubjectUsageRespVO usage = buildSubjectUsage(createReqVO.getAccountSetId(), parent.getId());
        if (Boolean.FALSE.equals(usage.getUsed())) {
            return usage;
        }
        // 只有新增第一个下级科目并明确确认时，才允许迁移上级科目的历史数据
        if (usage.getChildCount() > 0 || !Boolean.TRUE.equals(createReqVO.getMigrateParentData())) {
            throw exception(SUBJECT_PARENT_IN_USE);
        }
        // 历史数据使用原科目核算配置，新子科目必须完整继承相关配置
        boolean auxiliarySame = new HashSet<>(CollUtil.emptyIfNull(parent.getAuxiliaryTypeIds()))
                .equals(new HashSet<>(CollUtil.emptyIfNull(createReqVO.getAuxiliaryTypeIds())));
        boolean currencySame = new HashSet<>(CollUtil.emptyIfNull(parent.getCurrencyIds()))
                .equals(new HashSet<>(CollUtil.emptyIfNull(createReqVO.getCurrencyIds())));
        boolean quantityUnitSame = Boolean.FALSE.equals(parent.getQuantityAccounting())
                || ObjUtil.equal(parent.getQuantityUnit(), createReqVO.getQuantityUnit());
        if (ObjUtil.notEqual(parent.getBalanceDirection(), createReqVO.getBalanceDirection())
                || !auxiliarySame || !currencySame
                || ObjUtil.notEqual(parent.getQuantityAccounting(), createReqVO.getQuantityAccounting())
                || !quantityUnitSame) {
            throw exception(SUBJECT_PARENT_CONFIG_INCOMPATIBLE);
        }
        return usage;
    }

    /**
     * 校验已用科目的配置修改范围
     *
     * @param subject 科目
     * @param updateReqVO 更新信息
     * @param usage 科目使用情况
     */
    private void validateSubjectConfigUpdate(FmsSubjectDO subject, FmsSubjectSaveReqVO updateReqVO,
            FmsSubjectUsageRespVO usage) {
        boolean auxiliaryChanged = !new HashSet<>(CollUtil.emptyIfNull(subject.getAuxiliaryTypeIds()))
                .equals(new HashSet<>(CollUtil.emptyIfNull(updateReqVO.getAuxiliaryTypeIds())));
        // 非末级科目不允许修改类型、类别和辅助核算
        if (usage.getChildCount() > 0 && (ObjUtil.notEqual(subject.getType(), updateReqVO.getType())
                || ObjUtil.notEqual(subject.getCategory(), updateReqVO.getCategory()) || auxiliaryChanged)) {
            throw exception(SUBJECT_NON_LEAF_CONFIG_IMMUTABLE);
        }
        // 已用科目不允许修改余额方向
        if (Boolean.TRUE.equals(usage.getUsed())
                && ObjUtil.notEqual(subject.getBalanceDirection(), updateReqVO.getBalanceDirection())) {
            throw exception(SUBJECT_USED_BALANCE_DIRECTION_IMMUTABLE);
        }
        // 已用科目仅允许从未启用辅助核算迁移为启用状态，其他辅助核算变更仍禁止
        boolean auxiliaryMigration = CollUtil.isEmpty(subject.getAuxiliaryTypeIds())
                && CollUtil.isNotEmpty(updateReqVO.getAuxiliaryTypeIds());
        if (Boolean.TRUE.equals(usage.getUsed()) && auxiliaryChanged && !auxiliaryMigration) {
            throw exception(SUBJECT_USED_AUXILIARY_IMMUTABLE);
        }
        // 存在实际数量时不允许关闭数量核算
        if (Boolean.TRUE.equals(subject.getQuantityAccounting())
                && Boolean.FALSE.equals(updateReqVO.getQuantityAccounting()) && usage.getQuantityDataCount() > 0) {
            throw exception(SUBJECT_QUANTITY_ACCOUNTING_IN_USE);
        }
    }

    /**
     * 校验已用科目首次启用辅助核算的历史数据迁移项目
     *
     * @param subject 科目
     * @param updateReqVO 更新信息
     * @param usage 科目使用情况
     * @return 迁移项目 Map，键为辅助核算类别编号
     */
    private Map<Long, FmsAuxiliaryItemDO> validateSubjectAuxiliaryMigration(FmsSubjectDO subject,
            FmsSubjectSaveReqVO updateReqVO, FmsSubjectUsageRespVO usage) {
        // 1. 未触发历史数据迁移时，无需校验迁移项目
        if (usage.getVoucherEntryCount() <= 0 || CollUtil.isNotEmpty(subject.getAuxiliaryTypeIds())
                || CollUtil.isEmpty(updateReqVO.getAuxiliaryTypeIds())) {
            return Collections.emptyMap();
        }

        // 2. 校验每个新增辅助核算类别只配置一个迁移项目
        List<FmsSubjectSaveReqVO.AuxiliaryMapping> mappings = updateReqVO.getAuxiliaryMappings();
        if (CollUtil.size(mappings) != updateReqVO.getAuxiliaryTypeIds().size()) {
            throw exception(SUBJECT_AUXILIARY_MIGRATION_INVALID);
        }
        Map<Long, Long> itemIdMap = new LinkedHashMap<>();
        for (FmsSubjectSaveReqVO.AuxiliaryMapping mapping : mappings) {
            if (mapping == null || mapping.getTypeId() == null || mapping.getItemId() == null
                    || itemIdMap.put(mapping.getTypeId(), mapping.getItemId()) != null) {
                throw exception(SUBJECT_AUXILIARY_MIGRATION_INVALID);
            }
        }
        if (!new HashSet<>(updateReqVO.getAuxiliaryTypeIds()).equals(itemIdMap.keySet())) {
            throw exception(SUBJECT_AUXILIARY_MIGRATION_INVALID);
        }

        // 3. 校验迁移项目属于对应类别且处于启用状态
        List<FmsAuxiliaryItemDO> items = auxiliaryItemService.validateAuxiliaryItemList(
                updateReqVO.getAccountSetId(), itemIdMap.values());
        Map<Long, FmsAuxiliaryItemDO> itemMap = convertMap(items, FmsAuxiliaryItemDO::getAuxiliaryTypeId);
        if (itemMap.size() != itemIdMap.size()) {
            throw exception(SUBJECT_AUXILIARY_MIGRATION_INVALID);
        }
        for (Map.Entry<Long, Long> entry : itemIdMap.entrySet()) {
            FmsAuxiliaryItemDO item = itemMap.get(entry.getKey());
            if (item == null || ObjUtil.notEqual(item.getId(), entry.getValue())
                    || ObjUtil.notEqual(item.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                throw exception(SUBJECT_AUXILIARY_MIGRATION_INVALID);
            }
        }
        return itemMap;
    }

    private void validateSubjectCodeUnique(Long id, Long accountSetId, String subjectCode) {
        FmsSubjectDO subject = subjectMapper.selectByAccountSetIdAndCode(accountSetId, subjectCode);
        if (subject != null && ObjUtil.notEqual(subject.getId(), id)) {
            throw exception(SUBJECT_CODE_DUPLICATE);
        }
    }

    private FmsSubjectDO validateParentSubject(Long accountSetId, Long parentId, Integer type, Integer category) {
        if (FmsSubjectDO.PARENT_ID_ROOT.equals(parentId)) {
            return null;
        }
        FmsSubjectDO parent = subjectMapper.selectById(parentId);
        if (parent == null || ObjUtil.notEqual(parent.getAccountSetId(), accountSetId)
                || ObjUtil.notEqual(parent.getType(), type)
                || ObjUtil.notEqual(parent.getCategory(), category)) {
            throw exception(SUBJECT_PARENT_INVALID);
        }
        return parent;
    }

    /**
     * 校验科目编码是否符合当前科目级次规则
     *
     * @param subjectCode 科目编码
     * @param level 科目层级
     * @param parent 上级科目
     * @param financeParameter 财务参数
     */
    private void validateSubjectCodeRule(String subjectCode, int level, FmsSubjectDO parent,
            FmsFinanceParameterDO financeParameter) {
        String[] levels = financeParameter.getSubjectCodeRule().split("-");
        if (level > levels.length || !subjectCode.matches("\\d+")) {
            throw exception(SUBJECT_CODE_RULE_INVALID);
        }
        int expectedLength = 0;
        for (int index = 0; index < level; index++) {
            expectedLength += Integer.parseInt(levels[index]);
        }
        if (subjectCode.length() != expectedLength
                || parent != null && !subjectCode.startsWith(parent.getCode())) {
            throw exception(SUBJECT_CODE_RULE_INVALID);
        }
    }

    /**
     * 校验外币核算币别，不允许选择本位币
     *
     * @param accountSetId 账套编号
     * @param currencyIds 币别编号数组
     */
    private void validateCurrencyList(Long accountSetId, List<Long> currencyIds) {
        List<FmsCurrencyDO> currencies = currencyService.validateCurrencyList(accountSetId, currencyIds);
        if (CollUtil.findOne(currencies, currency -> Boolean.TRUE.equals(currency.getStandard())) != null) {
            throw exception(SUBJECT_CURRENCY_INVALID);
        }
    }

}

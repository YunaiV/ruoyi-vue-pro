package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsInitialBalanceDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsInitialBalanceMapper;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.sumBigDecimal;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_AUXILIARY_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_IMPORT_ROW_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_PERIOD_CLOSED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_PROFIT_LOSS_YEAR_OPENING_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_SUBJECT_NOT_LEAF;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.*;

/**
 * FMS 初始余额 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsInitialBalanceServiceImpl implements FmsInitialBalanceService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Resource
    private FmsInitialBalanceMapper initialBalanceMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsAuxiliaryItemService auxiliaryItemService;
    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @Resource
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;
    @Resource
    @Lazy // 延迟加载，避免与结账 Service 循环依赖
    private FmsClosingPeriodService closingPeriodService;

    @Override
    public List<FmsInitialBalanceRespVO> getInitialBalanceList(Long accountSetId, Integer subjectType, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 加载科目、辅助核算类别和科目余额
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, subjectType, userId);
        Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap = getAuxiliaryTypeMap(accountSetId, subjects);
        Map<Long, FmsInitialBalanceDO> balanceMap = convertMap(
                initialBalanceMapper.selectListByAccountSetId(accountSetId), FmsInitialBalanceDO::getSubjectId);

        // 2.1 构建平铺列表，父级科目按编码排在子级之前
        Map<Long, FmsInitialBalanceRespVO> balanceVOMap = new LinkedHashMap<>();
        subjects.forEach(subject -> balanceVOMap.put(subject.getId(),
                buildInitialBalanceRespVO(subject, balanceMap.get(subject.getId()), auxiliaryTypeMap)));
        List<FmsInitialBalanceRespVO> list = new ArrayList<>(balanceVOMap.values());
        list.sort(Comparator.comparing(FmsInitialBalanceRespVO::getSubjectCode)
                .thenComparing(FmsInitialBalanceRespVO::getSubjectId));
        // 2.2 父级余额由末级科目逐级汇总
        aggregateParentBalances(list, balanceVOMap);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_INITIAL_BALANCE_TYPE, subType = FMS_INITIAL_BALANCE_SAVE_SUB_TYPE,
            bizNo = "{{#saveReqVO.accountSetId}}", success = FMS_INITIAL_BALANCE_SAVE_SUCCESS)
    public void saveInitialBalance(FmsInitialBalanceSaveReqVO saveReqVO, Long userId) {
        // 1. 校验账套写权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetWritePermission(
                saveReqVO.getAccountSetId(), userId);

        // 2. 保存财务初始余额
        saveInitialBalance(saveReqVO, userId, accountSet);
    }

    /**
     * 保存财务初始余额
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @param accountSet 账套
     */
    private void saveInitialBalance(FmsInitialBalanceSaveReqVO saveReqVO, Long userId, FmsAccountSetDO accountSet) {
        // 1.1 校验结账状态
        validateAccountSetEditable(accountSet);
        // 1.2 加载科目和辅助核算类别
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(saveReqVO.getAccountSetId(), null, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap = getAuxiliaryTypeMap(saveReqVO.getAccountSetId(), subjects);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId);
        // 1.3 校验科目余额
        for (FmsInitialBalanceSaveReqVO.Balance reqVO : saveReqVO.getBalances()) {
            validateSaveBalance(subjectMap.get(reqVO.getSubjectId()), parentIds, reqVO);
        }

        // 2. 构建科目余额
        List<FmsInitialBalanceDO> balances = convertList(saveReqVO.getBalances(),
                reqVO -> buildBalance(saveReqVO.getAccountSetId(), subjectMap.get(reqVO.getSubjectId()),
                        reqVO, auxiliaryTypeMap));

        // 3.1 批量查询已有科目余额
        Map<Long, FmsInitialBalanceDO> existingMap = convertMap(
                initialBalanceMapper.selectListByAccountSetIdAndSubjectIds(saveReqVO.getAccountSetId(),
                        convertList(balances, FmsInitialBalanceDO::getSubjectId)), FmsInitialBalanceDO::getSubjectId);
        // 3.2 拆分新增和更新的科目余额
        List<FmsInitialBalanceDO> insertBalances = new ArrayList<>();
        List<FmsInitialBalanceDO> updateBalances = new ArrayList<>();
        balances.forEach(balance -> {
            FmsInitialBalanceDO existing = existingMap.get(balance.getSubjectId());
            if (existing == null) {
                insertBalances.add(balance);
            } else {
                balance.setId(existing.getId());
                updateBalances.add(balance);
            }
        });
        // 3.3 批量新增或更新科目余额
        if (CollUtil.isNotEmpty(insertBalances)) {
            initialBalanceMapper.insertBatch(insertBalances);
        }
        if (CollUtil.isNotEmpty(updateBalances)) {
            initialBalanceMapper.updateBatch(updateBalances);
        }
    }

    @Override
    public FmsTrialBalanceRespVO getTrialBalance(Long accountSetId, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 加载末级科目余额
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        List<FmsInitialBalanceDO> balances = filterList(initialBalanceMapper.selectListByAccountSetId(accountSetId),
                balance -> !parentIds.contains(balance.getSubjectId())
                        && subjectMap.containsKey(balance.getSubjectId()));

        // 2.1 汇总期初和本年累计的借贷金额
        BigDecimal openingDebitAmount = sumByDirection(balances, subjectMap,
                FmsDebitCreditDirectionEnum.DEBIT.getType(), FmsInitialBalanceDO::getOpeningAmount);
        BigDecimal openingCreditAmount = sumByDirection(balances, subjectMap,
                FmsDebitCreditDirectionEnum.CREDIT.getType(), FmsInitialBalanceDO::getOpeningAmount);
        BigDecimal yearDebitAmount = sumBigDecimal(balances, FmsInitialBalanceDO::getYearDebitAmount);
        BigDecimal yearCreditAmount = sumBigDecimal(balances, FmsInitialBalanceDO::getYearCreditAmount);
        BigDecimal openingDifferenceAmount = openingDebitAmount.subtract(openingCreditAmount);
        BigDecimal yearDifferenceAmount = yearDebitAmount.subtract(yearCreditAmount);
        // 2.2 构建试算平衡结果
        return new FmsTrialBalanceRespVO().setOpeningDebitAmount(openingDebitAmount)
                .setOpeningCreditAmount(openingCreditAmount).setOpeningDifferenceAmount(openingDifferenceAmount)
                .setYearDebitAmount(yearDebitAmount).setYearCreditAmount(yearCreditAmount)
                .setYearDifferenceAmount(yearDifferenceAmount)
                .setBalanced(openingDifferenceAmount.signum() == 0 && yearDifferenceAmount.signum() == 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_INITIAL_BALANCE_TYPE, subType = FMS_INITIAL_BALANCE_IMPORT_SUB_TYPE,
            bizNo = "{{#accountSetId}}", success = FMS_INITIAL_BALANCE_IMPORT_SUCCESS)
    public int importInitialBalance(Long accountSetId,
            List<FmsInitialBalanceExcelVO> rows, Long userId) {
        // 1.1 校验账套写权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验结账状态
        validateAccountSetEditable(accountSet);
        // 1.3 加载科目和辅助核算项目
        boolean january = LocalDateTimeUtils.isJanuary(accountSet.getStartTime());
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId);
        List<FmsAuxiliaryTypeDO> auxiliaryTypes = auxiliaryTypeService
                .getAuxiliaryTypeList(accountSetId, userId);
        Map<String, FmsAuxiliaryTypeDO> auxiliaryTypeMap = convertMap(auxiliaryTypes, FmsAuxiliaryTypeDO::getName);
        Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeIdMap = convertMap(
                auxiliaryTypes, FmsAuxiliaryTypeDO::getId);
        List<FmsAuxiliaryItemDO> auxiliaryItems = auxiliaryItemService
                .getAuxiliaryItemListByAccountSetId(accountSetId, userId);
        Map<Long, List<FmsAuxiliaryItemDO>> auxiliaryItemMap = auxiliaryItems.stream()
                .collect(Collectors.groupingBy(FmsAuxiliaryItemDO::getAuxiliaryTypeId));

        // 2. 校验并转换导入数据
        Map<Long, FmsInitialBalanceSaveReqVO.Balance> balanceMap = new LinkedHashMap<>();
        Set<String> rowKeys = new HashSet<>();
        for (FmsInitialBalanceExcelVO row : rows) {
            // 2.1 校验科目和金额，辅助核算科目的全零明细行直接跳过
            FmsSubjectDO subject = subjectMap.get(row.getSubjectCode());
            validateImportSubject(row, subject, parentIds);
            if (CollUtil.isNotEmpty(subject.getAuxiliaryTypeIds())
                    && StrUtil.isBlank(row.getAuxiliaryItems()) && isZeroRow(row)) {
                continue;
            }
            // 2.2 同一科目和辅助核算项目组合只允许出现一次
            String rowKey = subject.getId() + "#" + row.getAuxiliaryItems();
            if (!rowKeys.add(rowKey)) {
                throw importRowException(row, "科目和辅助核算项目重复");
            }
            // 2.3 未启用辅助核算的科目直接复制金额，启用时按辅助核算项目逐行转换
            FmsInitialBalanceSaveReqVO.Balance balance = balanceMap.computeIfAbsent(subject.getId(),
                    key -> new FmsInitialBalanceSaveReqVO.Balance().setSubjectId(subject.getId())
                            .setAssistBalances(new ArrayList<>()));
            if (CollUtil.isEmpty(subject.getAuxiliaryTypeIds())) {
                if (StrUtil.isNotBlank(row.getAuxiliaryItems())) {
                    throw importRowException(row, "该科目未启用辅助核算");
                }
                copyAmounts(row, balance, subject, january);
                continue;
            }
            FmsInitialBalanceSaveReqVO.AssistBalance assistBalance = new FmsInitialBalanceSaveReqVO.AssistBalance()
                    .setAuxiliaryItemIds(parseAuxiliaryItemIds(row, subject, auxiliaryTypeMap, auxiliaryTypeIdMap, auxiliaryItemMap));
            copyAmounts(row, assistBalance, subject, january);
            balance.getAssistBalances().add(assistBalance);
        }
        if (MapUtil.isEmpty(balanceMap)) {
            return 0;
        }

        // 3. 复用页面保存校验和持久化逻辑
        FmsInitialBalanceSaveReqVO saveReqVO = new FmsInitialBalanceSaveReqVO()
                .setAccountSetId(accountSetId).setBalances(new ArrayList<>(balanceMap.values()));
        saveInitialBalance(saveReqVO, userId, accountSet);
        return balanceMap.size();
    }

    @Override
    public Long getInitialBalanceCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        return initialBalanceMapper.selectCountByAccountSetIdAndSubjectIds(accountSetId, subjectIds);
    }

    @Override
    public Long getInitialBalanceQuantityCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        List<FmsInitialBalanceDO> balances = initialBalanceMapper.selectListByAccountSetIdAndSubjectIds(
                accountSetId, subjectIds);
        return (long) CollUtil.count(balances, this::hasQuantityData);
    }

    @Override
    public void updateInitialBalanceSubject(Long accountSetId, Long subjectId, Long targetSubjectId) {
        initialBalanceMapper.updateSubject(accountSetId, subjectId,
                new FmsInitialBalanceDO().setSubjectId(targetSubjectId));
    }

    @Override
    public Long getInitialBalanceCountByAuxiliaryItemIds(
            Long accountSetId, Collection<Long> auxiliaryItemIds) {
        if (CollUtil.isEmpty(auxiliaryItemIds)) {
            return 0L;
        }
        Set<Long> auxiliaryItemIdSet = new HashSet<>(auxiliaryItemIds);
        return countInitialBalancesByAuxiliary(accountSetId,
                item -> auxiliaryItemIdSet.contains(item.getItemId()));
    }

    @Override
    public Long getInitialBalanceCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId) {
        return countInitialBalancesByAuxiliary(accountSetId,
                item -> ObjUtil.equal(item.getTypeId(), auxiliaryTypeId));
    }

    /**
     * 统计辅助核算项目满足条件的初始余额数量
     */
    private Long countInitialBalancesByAuxiliary(Long accountSetId,
            Predicate<FmsInitialBalanceDO.AuxiliaryItem> predicate) {
        List<FmsInitialBalanceDO> balances = initialBalanceMapper.selectListByAccountSetId(accountSetId);
        // 展开每条初始余额的辅助核算项，任一辅助核算命中条件即计入
        return (long) CollUtil.count(balances, balance -> CollUtil.isNotEmpty(balance.getAssistBalances())
                && balance.getAssistBalances().stream()
                        .filter(assistBalance -> CollUtil.isNotEmpty(assistBalance.getAuxiliaries()))
                        .flatMap(assistBalance -> assistBalance.getAuxiliaries().stream())
                        .anyMatch(predicate));
    }

    /**
     * 判断初始余额是否包含数量数据
     *
     * @param balance 初始余额
     * @return 是否包含数量数据
     */
    private boolean hasQuantityData(FmsInitialBalanceDO balance) {
        // 科目余额存在实际数量
        if (hasQuantityData(balance.getOpeningQuantity(), balance.getYearDebitQuantity(),
                balance.getYearCreditQuantity(), balance.getYearOpeningQuantity(),
                balance.getProfitLossQuantity())) {
            return true;
        }
        // 辅助核算余额存在实际数量
        return CollUtil.isNotEmpty(balance.getAssistBalances())
                && CollUtil.findOne(balance.getAssistBalances(), assistBalance -> hasQuantityData(
                        assistBalance.getOpeningQuantity(), assistBalance.getYearDebitQuantity(),
                        assistBalance.getYearCreditQuantity(), assistBalance.getYearOpeningQuantity(),
                        assistBalance.getProfitLossQuantity())) != null;
    }

    private boolean hasQuantityData(BigDecimal... quantities) {
        return Stream.of(quantities).anyMatch(quantity -> NumberUtils.zeroIfNull(quantity).signum() != 0);
    }

    /**
     * 校验账套未结账，结账后初始余额不允许修改或导入
     */
    private void validateAccountSetEditable(FmsAccountSetDO accountSet) {
        if (ObjUtil.notEqual(closingPeriodService.getCurrentMonth(accountSet.getId(),
                accountSet.getStartTime()), YearMonth.from(accountSet.getStartTime()))) {
            throw exception(INITIAL_BALANCE_PERIOD_CLOSED);
        }
    }

    /**
     * 校验科目余额：科目必须存在且为末级，损益类科目的年初余额必须为 0
     */
    private void validateSaveBalance(FmsSubjectDO subject, Set<Long> parentIds,
            FmsInitialBalanceSaveReqVO.Balance reqVO) {
        if (subject == null) {
            throw exception(SUBJECT_NOT_EXISTS);
        }
        if (parentIds.contains(subject.getId())) {
            throw exception(INITIAL_BALANCE_SUBJECT_NOT_LEAF);
        }
        validateProfitLossBalance(subject, reqVO.getYearOpeningAmount());
    }

    /**
     * 汇总父级科目余额：非末级科目先清零，再按“方向相同取正、相反取负”自底向上累加子级
     */
    private void aggregateParentBalances(List<FmsInitialBalanceRespVO> list,
            Map<Long, FmsInitialBalanceRespVO> balanceVOMap) {
        // 1. 非末级科目的余额由子级汇总，先清零
        Set<Long> parentIds = convertSet(list, FmsInitialBalanceRespVO::getParentId);
        list.forEach(balanceVO -> {
            if (parentIds.contains(balanceVO.getSubjectId())) {
                setZeroAmounts(balanceVO);
            }
        });
        // 2. 平铺列表父级在前，倒序遍历时子级先完成汇总，再逐级累加到父级
        for (int index = list.size() - 1; index >= 0; index--) {
            FmsInitialBalanceRespVO balanceVO = list.get(index);
            FmsInitialBalanceRespVO parent = balanceVOMap.get(balanceVO.getParentId());
            if (parent == null) {
                continue;
            }
            accumulateParentBalance(parent, balanceVO);
        }
    }

    /**
     * 将子级科目余额累加到父级：期初、年初和损益发生额均按余额方向取符号，累计发生额按借贷分别累加
     */
    private void accumulateParentBalance(FmsInitialBalanceRespVO parent, FmsInitialBalanceRespVO child) {
        parent.setOpeningAmount(parent.getOpeningAmount().add(directional(child.getOpeningAmount(), child, parent)));
        parent.setOpeningQuantity(parent.getOpeningQuantity().add(directional(child.getOpeningQuantity(), child, parent)));
        parent.setYearDebitAmount(parent.getYearDebitAmount().add(child.getYearDebitAmount()));
        parent.setYearDebitQuantity(parent.getYearDebitQuantity().add(child.getYearDebitQuantity()));
        parent.setYearCreditAmount(parent.getYearCreditAmount().add(child.getYearCreditAmount()));
        parent.setYearCreditQuantity(parent.getYearCreditQuantity().add(child.getYearCreditQuantity()));
        parent.setYearOpeningAmount(parent.getYearOpeningAmount().add(directional(child.getYearOpeningAmount(), child, parent)));
        parent.setYearOpeningQuantity(parent.getYearOpeningQuantity().add(directional(child.getYearOpeningQuantity(), child, parent)));
        parent.setProfitLossAmount(parent.getProfitLossAmount().add(
                directional(child.getProfitLossAmount(), child, parent)));
        parent.setProfitLossQuantity(parent.getProfitLossQuantity().add(
                directional(child.getProfitLossQuantity(), child, parent)));
    }

    /**
     * 子级与父级余额方向相同时取正数，相反时取负数
     */
    private BigDecimal directional(BigDecimal amount, FmsInitialBalanceRespVO child, FmsInitialBalanceRespVO parent) {
        return ObjUtil.equal(child.getBalanceDirection(), parent.getBalanceDirection()) ? amount : amount.negate();
    }

    /**
     * 构建科目余额，启用辅助核算时科目金额由辅助核算明细汇总
     */
    private FmsInitialBalanceDO buildBalance(Long accountSetId, FmsSubjectDO subject,
                                             FmsInitialBalanceSaveReqVO.Balance reqVO,
                                             Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap) {
        boolean auxiliaryAccounting = CollUtil.isNotEmpty(subject.getAuxiliaryTypeIds());
        FmsInitialBalanceDO balance = new FmsInitialBalanceDO().setSubjectId(subject.getId())
                .setAccountSetId(accountSetId).setAuxiliaryAccounting(auxiliaryAccounting)
                .setAssistBalances(Collections.emptyList());
        if (auxiliaryAccounting) {
            List<FmsInitialBalanceDO.AssistBalance> assistBalances = buildAssistBalances(
                    accountSetId, subject, reqVO.getAssistBalances(), auxiliaryTypeMap);
            balance.setAssistBalances(assistBalances);
            sumAssistBalances(balance, assistBalances);
            return balance;
        }
        copyAmounts(reqVO, balance);
        return balance;
    }

    /**
     * 构建辅助核算余额明细：逐项校验项目与科目配置，并获取或创建辅助核算组合
     */
    private List<FmsInitialBalanceDO.AssistBalance> buildAssistBalances(Long accountSetId,
            FmsSubjectDO subject, List<FmsInitialBalanceSaveReqVO.AssistBalance> reqVOs,
            Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap) {
        if (CollUtil.isEmpty(reqVOs)) {
            return Collections.emptyList();
        }
        List<FmsInitialBalanceDO.AssistBalance> result = new ArrayList<>();
        Set<Long> combinationIds = new HashSet<>();
        for (FmsInitialBalanceSaveReqVO.AssistBalance reqVO : reqVOs) {
            // 1. 校验辅助核算项目与科目启用的类别一致
            List<Long> itemIds = new ArrayList<>(new LinkedHashSet<>(reqVO.getAuxiliaryItemIds()));
            List<FmsAuxiliaryItemDO> items = auxiliaryItemService.validateAuxiliaryItemList(accountSetId, itemIds);
            Map<Long, FmsAuxiliaryItemDO> itemMap = convertMap(items, FmsAuxiliaryItemDO::getAuxiliaryTypeId);
            if (ObjUtil.notEqual(itemMap.size(), subject.getAuxiliaryTypeIds().size())
                    || subject.getAuxiliaryTypeIds().stream().anyMatch(
                    auxiliaryTypeId -> !itemMap.containsKey(auxiliaryTypeId))) {
                throw exception(INITIAL_BALANCE_AUXILIARY_INVALID);
            }
            // 2. 按科目启用的类别顺序，构建辅助核算组合明细
            List<FmsAuxiliaryCombinationDO.AuxiliaryItem> combinationItems = new ArrayList<>();
            List<FmsInitialBalanceDO.AuxiliaryItem> auxiliaryItems = new ArrayList<>();
            subject.getAuxiliaryTypeIds().forEach(auxiliaryTypeId -> {
                FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeMap.get(auxiliaryTypeId);
                FmsAuxiliaryItemDO item = itemMap.get(auxiliaryTypeId);
                combinationItems.add(FmsAuxiliaryCombinationDO.AuxiliaryItem.builder()
                        .type(auxiliaryType.getType()).typeId(auxiliaryTypeId)
                        .itemId(item.getId()).name(item.getName()).build());
                auxiliaryItems.add(FmsInitialBalanceDO.AuxiliaryItem.builder()
                        .type(auxiliaryType.getType()).typeId(auxiliaryTypeId)
                        .itemId(item.getId()).name(item.getName()).build());
            });
            // 3. 保存辅助核算组合，同一科目下同一组合只允许一条明细
            FmsAuxiliaryCombinationDO combination = auxiliaryCombinationService.saveAuxiliaryCombination(
                    accountSetId, subject.getId(), combinationItems);
            if (!combinationIds.add(combination.getId())) {
                throw exception(INITIAL_BALANCE_AUXILIARY_INVALID);
            }
            FmsInitialBalanceDO.AssistBalance assist = new FmsInitialBalanceDO.AssistBalance()
                    .setAssistCombinationId(combination.getId()).setAuxiliaries(auxiliaryItems);
            copyAmounts(reqVO, assist);
            validateProfitLossBalance(subject, assist.getYearOpeningAmount());
            result.add(assist);
        }
        return result;
    }

    /**
     * 构建科目初始余额响应，无余额记录时金额按 0 返回
     */
    private FmsInitialBalanceRespVO buildInitialBalanceRespVO(FmsSubjectDO subject,
            FmsInitialBalanceDO balance, Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeMap) {
        FmsInitialBalanceRespVO respVO = new FmsInitialBalanceRespVO().setSubjectId(subject.getId())
                .setSubjectCode(subject.getCode()).setSubjectName(subject.getName())
                .setParentId(subject.getParentId()).setType(subject.getType())
                .setBalanceDirection(subject.getBalanceDirection())
                .setQuantityAccounting(subject.getQuantityAccounting()).setQuantityUnit(subject.getQuantityUnit())
                .setAuxiliaryAccounting(CollUtil.isNotEmpty(subject.getAuxiliaryTypeIds()))
                .setAuxiliaryConfigs(convertList(subject.getAuxiliaryTypeIds(), auxiliaryTypeId -> {
                    FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeMap.get(auxiliaryTypeId);
                    return new FmsInitialBalanceRespVO.AuxiliaryConfig().setAuxiliaryTypeId(auxiliaryTypeId)
                            .setType(auxiliaryType.getType()).setName(auxiliaryType.getName());
                }));
        if (balance == null) {
            setZeroAmounts(respVO);
            respVO.setAssistBalances(Collections.emptyList());
            return respVO;
        }
        respVO.setId(balance.getId());
        copyAmounts(balance, respVO);
        if (CollUtil.isEmpty(balance.getAssistBalances())) {
            respVO.setAssistBalances(Collections.emptyList());
        } else {
            respVO.setAssistBalances(convertList(balance.getAssistBalances(), assist -> {
                FmsInitialBalanceRespVO.AssistBalance assistVO =
                        BeanUtils.toBean(assist, FmsInitialBalanceRespVO.AssistBalance.class);
                assistVO.setAuxiliaries(convertList(assist.getAuxiliaries(),
                        item -> BeanUtils.toBean(item, FmsInitialBalanceRespVO.AuxiliaryItem.class)));
                return assistVO;
            }));
        }
        return respVO;
    }

    private void copyAmounts(FmsInitialBalanceDO source, FmsInitialBalanceRespVO target) {
        target.setOpeningAmount(NumberUtils.zeroIfNull(source.getOpeningAmount()))
                .setOpeningQuantity(NumberUtils.zeroIfNull(source.getOpeningQuantity()))
                .setYearDebitAmount(NumberUtils.zeroIfNull(source.getYearDebitAmount()))
                .setYearDebitQuantity(NumberUtils.zeroIfNull(source.getYearDebitQuantity()))
                .setYearCreditAmount(NumberUtils.zeroIfNull(source.getYearCreditAmount()))
                .setYearCreditQuantity(NumberUtils.zeroIfNull(source.getYearCreditQuantity()))
                .setYearOpeningAmount(NumberUtils.zeroIfNull(source.getYearOpeningAmount()))
                .setYearOpeningQuantity(NumberUtils.zeroIfNull(source.getYearOpeningQuantity()))
                .setProfitLossAmount(NumberUtils.zeroIfNull(source.getProfitLossAmount()))
                .setProfitLossQuantity(NumberUtils.zeroIfNull(source.getProfitLossQuantity()));
    }

    /**
     * 汇总指定余额方向的金额
     */
    private BigDecimal sumByDirection(List<FmsInitialBalanceDO> balances,
            Map<Long, FmsSubjectDO> subjectMap, Integer direction,
            Function<FmsInitialBalanceDO, BigDecimal> getter) {
        List<FmsInitialBalanceDO> directionBalances = filterList(balances, balance -> ObjUtil.equal(
                subjectMap.get(balance.getSubjectId()).getBalanceDirection(), direction));
        return sumBigDecimal(directionBalances, getter);
    }

    private void copyAmounts(FmsInitialBalanceSaveReqVO.Amounts source, FmsInitialBalanceDO target) {
        target.setOpeningAmount(NumberUtils.zeroIfNull(source.getOpeningAmount()))
                .setOpeningQuantity(NumberUtils.zeroIfNull(source.getOpeningQuantity()))
                .setYearDebitAmount(NumberUtils.zeroIfNull(source.getYearDebitAmount()))
                .setYearDebitQuantity(NumberUtils.zeroIfNull(source.getYearDebitQuantity()))
                .setYearCreditAmount(NumberUtils.zeroIfNull(source.getYearCreditAmount()))
                .setYearCreditQuantity(NumberUtils.zeroIfNull(source.getYearCreditQuantity()))
                .setYearOpeningAmount(NumberUtils.zeroIfNull(source.getYearOpeningAmount()))
                .setYearOpeningQuantity(NumberUtils.zeroIfNull(source.getYearOpeningQuantity()))
                .setProfitLossAmount(NumberUtils.zeroIfNull(source.getProfitLossAmount()))
                .setProfitLossQuantity(NumberUtils.zeroIfNull(source.getProfitLossQuantity()));
    }

    private void copyAmounts(FmsInitialBalanceSaveReqVO.Amounts source,
            FmsInitialBalanceDO.AssistBalance target) {
        target.setOpeningAmount(NumberUtils.zeroIfNull(source.getOpeningAmount()))
                .setOpeningQuantity(NumberUtils.zeroIfNull(source.getOpeningQuantity()))
                .setYearDebitAmount(NumberUtils.zeroIfNull(source.getYearDebitAmount()))
                .setYearDebitQuantity(NumberUtils.zeroIfNull(source.getYearDebitQuantity()))
                .setYearCreditAmount(NumberUtils.zeroIfNull(source.getYearCreditAmount()))
                .setYearCreditQuantity(NumberUtils.zeroIfNull(source.getYearCreditQuantity()))
                .setYearOpeningAmount(NumberUtils.zeroIfNull(source.getYearOpeningAmount()))
                .setYearOpeningQuantity(NumberUtils.zeroIfNull(source.getYearOpeningQuantity()))
                .setProfitLossAmount(NumberUtils.zeroIfNull(source.getProfitLossAmount()))
                .setProfitLossQuantity(NumberUtils.zeroIfNull(source.getProfitLossQuantity()));
    }

    private void sumAssistBalances(FmsInitialBalanceDO balance,
            List<FmsInitialBalanceDO.AssistBalance> assists) {
        balance.setOpeningAmount(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getOpeningAmount))
                .setOpeningQuantity(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getOpeningQuantity))
                .setYearDebitAmount(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getYearDebitAmount))
                .setYearDebitQuantity(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getYearDebitQuantity))
                .setYearCreditAmount(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getYearCreditAmount))
                .setYearCreditQuantity(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getYearCreditQuantity))
                .setYearOpeningAmount(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getYearOpeningAmount))
                .setYearOpeningQuantity(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getYearOpeningQuantity))
                .setProfitLossAmount(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getProfitLossAmount))
                .setProfitLossQuantity(sumBigDecimal(assists, FmsInitialBalanceDO.AssistBalance::getProfitLossQuantity));
    }

    private void validateImportSubject(FmsInitialBalanceExcelVO row, FmsSubjectDO subject,
            Set<Long> parentIds) {
        if (StrUtil.isBlank(row.getSubjectCode())) {
            throw importRowException(row, "科目编码不能为空");
        }
        if (subject == null) {
            throw importRowException(row, "科目编码不存在");
        }
        if (parentIds.contains(subject.getId())) {
            throw importRowException(row, "只能导入末级科目");
        }
        FmsDebitCreditDirectionEnum direction = FmsDebitCreditDirectionEnum
                .valueOf(subject.getBalanceDirection());
        if (StrUtil.isNotBlank(row.getDirectionName())
                && !StrUtil.equals(direction.getName(), row.getDirectionName())) {
            throw importRowException(row, "余额方向与科目不一致");
        }
        validateAmounts(row);
    }

    private void validateAmounts(FmsInitialBalanceExcelVO row) {
        List<BigDecimal> amounts = Arrays.asList(row.getOpeningQuantity(), row.getOpeningAmount(),
                row.getYearDebitQuantity(), row.getYearDebitAmount(), row.getYearCreditQuantity(),
                row.getYearCreditAmount(), row.getProfitLossQuantity(), row.getProfitLossAmount());
        if (amounts.stream().filter(ObjUtil::isNotNull).anyMatch(amount -> amount.signum() < 0)) {
            throw importRowException(row, "数量和金额不能小于 0");
        }
    }

    /**
     * 解析导入行的辅助核算项目文本，按科目启用的类别顺序返回项目编号
     */
    private List<Long> parseAuxiliaryItemIds(FmsInitialBalanceExcelVO row,
                                             FmsSubjectDO subject, Map<String, FmsAuxiliaryTypeDO> auxiliaryTypeMap,
                                             Map<Long, FmsAuxiliaryTypeDO> auxiliaryTypeIdMap,
                                             Map<Long, List<FmsAuxiliaryItemDO>> auxiliaryItemMap) {
        // 1. 按“类别:名称/类别:名称”解析文本，类别不允许重复
        if (StrUtil.isBlank(row.getAuxiliaryItems())) {
            throw importRowException(row, "辅助核算项目不能为空");
        }
        Map<Long, String> values = new LinkedHashMap<>();
        for (String segment : StrUtil.split(row.getAuxiliaryItems(), '/')) {
            List<String> parts = StrUtil.split(segment, ':');
            if (parts.size() != 2 || StrUtil.isBlank(parts.get(0)) || StrUtil.isBlank(parts.get(1))) {
                throw importRowException(row, "辅助核算项目格式不正确");
            }
            FmsAuxiliaryTypeDO type = auxiliaryTypeMap.get(parts.get(0));
            if (type == null || values.put(type.getId(), parts.get(1)) != null) {
                throw importRowException(row, "辅助核算类别不存在或重复");
            }
        }
        // 2. 按科目启用的类别匹配辅助核算项目，名称或编码均可
        List<Long> itemIds = new ArrayList<>();
        for (Long auxiliaryTypeId : subject.getAuxiliaryTypeIds()) {
            String itemValue = values.get(auxiliaryTypeId);
            if (StrUtil.isBlank(itemValue)) {
                throw importRowException(row, "辅助核算项目与科目配置不一致");
            }
            FmsAuxiliaryItemDO item = CollUtil.findOne(
                    auxiliaryItemMap.getOrDefault(auxiliaryTypeId, Collections.emptyList()),
                    candidate -> StrUtil.equals(candidate.getName(), itemValue)
                            || StrUtil.equals(candidate.getCode(), itemValue));
            if (item == null) {
                throw importRowException(row, auxiliaryTypeIdMap.get(auxiliaryTypeId).getName() + "项目不存在");
            }
            itemIds.add(item.getId());
        }
        // 3. 校验类别数量与科目配置一致
        if (values.size() != subject.getAuxiliaryTypeIds().size()) {
            throw importRowException(row, "辅助核算项目与科目配置不一致");
        }
        return itemIds;
    }

    private Map<Long, FmsAuxiliaryTypeDO> getAuxiliaryTypeMap(
            Long accountSetId, List<FmsSubjectDO> subjects) {
        List<FmsAuxiliaryTypeDO> list = auxiliaryTypeService.validateAuxiliaryTypeList(accountSetId,
                convertSetByFlatMap(subjects, FmsSubjectDO::getAuxiliaryTypeIds, List::stream));
        return convertMap(list, FmsAuxiliaryTypeDO::getId);
    }

    private void copyAmounts(FmsInitialBalanceExcelVO source,
            FmsInitialBalanceSaveReqVO.Amounts target, FmsSubjectDO subject, boolean january) {
        BigDecimal openingAmount = NumberUtils.zeroIfNull(source.getOpeningAmount());
        BigDecimal openingQuantity = NumberUtils.zeroIfNull(source.getOpeningQuantity());
        BigDecimal yearDebitAmount = january ? ZERO : NumberUtils.zeroIfNull(source.getYearDebitAmount());
        BigDecimal yearDebitQuantity = january ? ZERO : NumberUtils.zeroIfNull(source.getYearDebitQuantity());
        BigDecimal yearCreditAmount = january ? ZERO : NumberUtils.zeroIfNull(source.getYearCreditAmount());
        BigDecimal yearCreditQuantity = january ? ZERO : NumberUtils.zeroIfNull(source.getYearCreditQuantity());
        boolean debit = ObjUtil.equal(subject.getBalanceDirection(),
                FmsDebitCreditDirectionEnum.DEBIT.getType());
        BigDecimal yearOpeningAmount = january ? ZERO : debit
                ? openingAmount.subtract(yearDebitAmount).add(yearCreditAmount)
                : openingAmount.add(yearDebitAmount).subtract(yearCreditAmount);
        BigDecimal yearOpeningQuantity = january ? ZERO : debit
                ? openingQuantity.subtract(yearDebitQuantity).add(yearCreditQuantity)
                : openingQuantity.add(yearDebitQuantity).subtract(yearCreditQuantity);
        if (yearOpeningAmount.signum() < 0 || yearOpeningQuantity.signum() < 0) {
            throw importRowException(source, "年初余额不能小于 0");
        }
        target.setOpeningAmount(openingAmount).setOpeningQuantity(openingQuantity)
                .setYearDebitAmount(yearDebitAmount).setYearDebitQuantity(yearDebitQuantity)
                .setYearCreditAmount(yearCreditAmount).setYearCreditQuantity(yearCreditQuantity)
                .setYearOpeningAmount(yearOpeningAmount).setYearOpeningQuantity(yearOpeningQuantity)
                .setProfitLossAmount(january ? ZERO : NumberUtils.zeroIfNull(source.getProfitLossAmount()))
                .setProfitLossQuantity(january ? ZERO : NumberUtils.zeroIfNull(source.getProfitLossQuantity()));
    }

    private boolean isZeroRow(FmsInitialBalanceExcelVO row) {
        List<BigDecimal> amounts = Arrays.asList(row.getOpeningQuantity(), row.getOpeningAmount(),
                row.getYearDebitQuantity(), row.getYearDebitAmount(), row.getYearCreditQuantity(),
                row.getYearCreditAmount(), row.getProfitLossQuantity(), row.getProfitLossAmount());
        return amounts.stream().map(NumberUtils::zeroIfNull).allMatch(amount -> amount.signum() == 0);
    }

    private RuntimeException importRowException(FmsInitialBalanceExcelVO row, String message) {
        return exception(INITIAL_BALANCE_IMPORT_ROW_INVALID, row.getRowNumber(), message);
    }

    private void setZeroAmounts(FmsInitialBalanceRespVO respVO) {
        respVO.setOpeningAmount(ZERO).setOpeningQuantity(ZERO).setYearDebitAmount(ZERO)
                .setYearDebitQuantity(ZERO).setYearCreditAmount(ZERO).setYearCreditQuantity(ZERO)
                .setYearOpeningAmount(ZERO).setYearOpeningQuantity(ZERO)
                .setProfitLossAmount(ZERO).setProfitLossQuantity(ZERO);
    }

    private void validateProfitLossBalance(FmsSubjectDO subject, BigDecimal yearOpeningAmount) {
        if (ObjUtil.equal(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                && NumberUtils.zeroIfNull(yearOpeningAmount).signum() != 0) {
            throw exception(INITIAL_BALANCE_PROFIT_LOSS_YEAR_OPENING_INVALID);
        }
    }

}

package cn.iocoder.yudao.module.fms.service.ledger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerAuxiliaryListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerEntryVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarybalance.FmsLedgerAuxiliaryBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarydetail.FmsLedgerAuxiliaryDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.general.FmsLedgerGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn.FmsLedgerMultiColumnRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitydetail.FmsLedgerQuantityDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitygeneral.FmsLedgerQuantityGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsInitialBalanceDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsInitialBalanceMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.ledger.FmsLedgerQueryMapper;
import cn.iocoder.yudao.module.fms.enums.common.FmsBalanceDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryItemService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.sumBigDecimal;
import static cn.iocoder.yudao.framework.common.util.number.NumberUtils.zeroIfNull;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.AUXILIARY_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.LEDGER_PERIOD_BEFORE_ACCOUNT_START;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.LEDGER_PERIOD_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_AUXILIARY_REQUIRED;

/**
 * FMS 账簿 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsLedgerServiceImpl implements FmsLedgerService {

    @Resource
    private FmsLedgerQueryMapper ledgerQueryMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsFinanceParameterService financeParameterService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsAuxiliaryItemService auxiliaryItemService;
    @Resource
    private FmsInitialBalanceMapper initialBalanceMapper;

    // ==================== 公共查询 ====================

    @Override
    public List<FmsLedgerEntryVO> getEntryList(Long accountSetId, LocalDateTime beginTime,
            LocalDateTime endTime, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询指定期间的凭证分录
        return filterList(ledgerQueryMapper.selectEntryListBeforeTime(accountSetId, endTime),
                entry -> !entry.getVoucherTime().isBefore(beginTime));
    }

    @Override
    public BigDecimal getAuxiliaryCombinationBalance(Long accountSetId, String month, Long subjectId,
            Collection<Long> auxiliaryItemIds, Long userId) {
        // 1. 加载账簿上下文
        LedgerContext context = buildContext(new FmsLedgerListReqVO().setAccountSetId(accountSetId)
                .setStartMonth(month).setEndMonth(month), userId);

        // 2. 校验科目和辅助核算项目
        FmsSubjectDO subject = context.subjectMap.get(subjectId);
        if (subject == null) {
            throw exception(SUBJECT_NOT_EXISTS);
        }
        List<FmsAuxiliaryItemDO> auxiliaryItems = auxiliaryItemService
                .validateAuxiliaryItemList(accountSetId, auxiliaryItemIds);
        Set<Long> auxiliaryItemIdSet = new LinkedHashSet<>(auxiliaryItemIds);
        if (CollUtil.size(subject.getAuxiliaryTypeIds()) != auxiliaryItemIdSet.size()
                || !new LinkedHashSet<>(subject.getAuxiliaryTypeIds()).equals(
                convertSet(auxiliaryItems, FmsAuxiliaryItemDO::getAuxiliaryTypeId))) {
            throw exception(VOUCHER_AUXILIARY_REQUIRED);
        }

        // 3. 计算辅助核算组合的初始余额
        BigDecimal signedBalance = BigDecimal.ZERO;
        List<FmsInitialBalanceDO> initialBalances = filterList(context.initialBalances,
                initial -> ObjUtil.equal(initial.getSubjectId(), subjectId));
        for (FmsInitialBalanceDO initial : initialBalances) {
            if (CollUtil.isEmpty(initial.getAssistBalances())) {
                continue;
            }
            List<FmsInitialBalanceDO.AssistBalance> assistBalances = filterList(initial.getAssistBalances(),
                    assist -> CollUtil.isNotEmpty(assist.getAuxiliaries())
                            && auxiliaryItemIdSet.equals(convertSet(assist.getAuxiliaries(),
                            FmsInitialBalanceDO.AuxiliaryItem::getItemId)));
            BigDecimal initialAmount = sumBigDecimal(assistBalances,
                    FmsInitialBalanceDO.AssistBalance::getOpeningAmount);
            signedBalance = signedBalance.add(ObjUtil.equal(subject.getBalanceDirection(),
                    FmsDebitCreditDirectionEnum.DEBIT.getType()) ? initialAmount : initialAmount.negate());
        }

        // 4. 累加会计期间结束前的凭证发生额
        List<FmsLedgerEntryVO> entries = filterList(context.entries,
                entry -> ObjUtil.equal(entry.getSubjectId(), subjectId)
                        && CollUtil.isNotEmpty(entry.getAuxiliaries())
                        && auxiliaryItemIdSet.equals(convertSet(entry.getAuxiliaries(),
                        FmsVoucherEntryDO.AuxiliaryItem::getItemId)));
        return signedBalance.add(calculateSignedAmount(entries, null,
                context.endMonth.plusMonths(1).atDay(1).atStartOfDay()));
    }

    // ==================== 明细账 ====================

    @Override
    public List<FmsLedgerDetailRespVO> getDetailList(FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载账簿上下文
        LedgerContext context = buildContext(listReqVO, userId);
        // 2. 校验科目
        FmsSubjectDO subject = context.subjectMap.get(listReqVO.getSubjectId());
        if (subject == null) {
            throw exception(SUBJECT_NOT_EXISTS);
        }

        // 3. 计算明细账
        return buildLedgerRows(context, subject, true);
    }

    @Override
    public List<FmsSubjectDO> getDetailSubjectList(FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载指定期间的账簿上下文
        LedgerContext context = buildContext(listReqVO, userId);
        LocalDateTime beginTime = context.startMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = context.endMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 2. 收集期间有凭证发生额的科目，并补齐其父级节点
        Set<Long> subjectIds = filterEntriesByTime(context.entries, beginTime, endTime).stream()
                .map(FmsLedgerEntryVO::getSubjectId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> visibleSubjectIds = new LinkedHashSet<>(subjectIds);
        for (FmsSubjectDO subject : context.subjects) {
            if (!subjectIds.contains(subject.getId())) {
                continue;
            }
            // TODO @AI：尽量不要 while true 死循环，而是有一个 for i 《 short 的上限》
            Long parentId = subject.getParentId();
            while (parentId != null && visibleSubjectIds.add(parentId)) {
                FmsSubjectDO parent = context.subjectMap.get(parentId);
                parentId = parent == null ? null : parent.getParentId();
            }
        }
        return filterList(context.subjects, subject -> visibleSubjectIds.contains(subject.getId()));
    }

    // ==================== 总账 ====================

    @Override
    public List<FmsLedgerGeneralRespVO> getGeneralList(FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载账簿上下文
        LedgerContext context = buildContext(listReqVO, userId);

        // 2. 筛选总账科目
        List<FmsSubjectDO> subjects = filterSubjects(context, listReqVO);

        // 3. 计算总账
        List<FmsLedgerDetailRespVO> detailRows = new ArrayList<>();
        for (FmsSubjectDO subject : subjects) {
            List<FmsLedgerDetailRespVO> rows = buildLedgerRows(context, subject, false);
            if (CollUtil.isNotEmpty(rows)
                    && rows.stream().anyMatch(row -> row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_VOUCHER)
                    || zeroIfNull(row.getDebitAmount()).signum() != 0
                    || zeroIfNull(row.getCreditAmount()).signum() != 0
                    || zeroIfNull(row.getBalance()).signum() != 0)) {
                detailRows.addAll(rows);
            }
        }
        return BeanUtils.toBean(detailRows, FmsLedgerGeneralRespVO.class);
    }

    // ==================== 科目余额表 ====================

    @Override
    public List<FmsLedgerSubjectBalanceRespVO> getSubjectBalanceList(
            FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载账簿上下文
        LedgerContext context = buildContext(listReqVO, userId);

        // 2. 计算辅助核算组合余额
        Map<Long, FmsAuxiliaryItemDO> auxiliaryItemMap = convertMap(
                auxiliaryItemService.getAuxiliaryItemListByAccountSetId(listReqVO.getAccountSetId(), userId),
                FmsAuxiliaryItemDO::getId);
        Map<Long, List<FmsLedgerSubjectBalanceRespVO>> auxiliaryBalanceMap =
                buildAuxiliaryCombinationBalanceMap(context, auxiliaryItemMap);

        // 3. 计算科目余额并过滤八项金额全零且没有辅助组合余额的科目
        List<FmsLedgerSubjectBalanceRespVO> result = buildSubjectBalanceList(context, listReqVO,
                balance -> hasSubjectBalanceAmount(balance)
                        || auxiliaryBalanceMap.containsKey(balance.getSubjectId()));
        appendAuxiliaryCombinationBalances(result, auxiliaryBalanceMap);
        return result;
    }

    // ==================== 多栏账 ====================

    @Override
    public FmsLedgerMultiColumnRespVO getMultiColumn(FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载账簿上下文
        LedgerContext context = buildContext(listReqVO, userId);
        // 2. 校验科目
        FmsSubjectDO subject = context.subjectMap.get(listReqVO.getSubjectId());
        if (subject == null) {
            throw exception(SUBJECT_NOT_EXISTS);
        }

        // 3. 筛选有数据的动态科目列
        Set<Long> subjectIds = collectSubjectIds(context, subject.getId());
        List<FmsSubjectDO> columns = filterList(context.subjects, column -> subjectIds.contains(column.getId())
                && (context.entries.stream().anyMatch(entry -> ObjUtil.equal(entry.getSubjectId(), column.getId()))
                || context.initialBalances.stream().anyMatch(initial -> ObjUtil.equal(
                        initial.getSubjectId(), column.getId())
                        && (zeroIfNull(initial.getOpeningAmount()).signum() != 0
                        || zeroIfNull(initial.getYearDebitAmount()).signum() != 0
                        || zeroIfNull(initial.getYearCreditAmount()).signum() != 0))));

        // 4. 计算多栏账
        List<FmsLedgerDetailRespVO> rows = appendMultiColumnEndingRows(
                buildLedgerRows(context, subject, true));
        rows.forEach(row -> {
            Map<Long, BigDecimal> columnAmounts = buildColumnAmounts(context, columns, row);
            row.setColumnAmounts(columnAmounts);
        });
        return new FmsLedgerMultiColumnRespVO()
                .setColumns(convertList(columns, column -> new FmsLedgerMultiColumnRespVO.Column()
                        .setSubjectId(column.getId()).setSubjectCode(column.getCode())
                        .setSubjectName(column.getName())
                        .setBalanceDirection(column.getBalanceDirection())))
                .setRows(rows);
    }

    // ==================== 核算项目明细账 ====================

    @Override
    public List<FmsLedgerAuxiliaryDetailRespVO> getAuxiliaryDetailList(
            FmsLedgerAuxiliaryListReqVO listReqVO, Long userId) {
        // 1. 加载辅助核算账簿上下文
        AuxiliaryLedgerContext context = buildAuxiliaryContext(listReqVO.getAccountSetId(),
                listReqVO.getStartMonth(), listReqVO.getEndMonth(), listReqVO.getAuxiliaryTypeId(),
                listReqVO.getSubjectId(), userId);
        // 2. 校验辅助核算项目
        validateAuxiliaryItem(context, listReqVO.getAuxiliaryItemId());

        // 3. 计算核算项目明细账
        return buildAuxiliaryDetailRows(context, listReqVO.getAuxiliaryItemId());
    }

    // ==================== 核算项目余额表 ====================

    @Override
    public List<FmsLedgerAuxiliaryBalanceRespVO> getAuxiliaryBalanceList(
            FmsLedgerAuxiliaryListReqVO listReqVO, Long userId) {
        // 1. 加载辅助核算账簿上下文
        AuxiliaryLedgerContext context = buildAuxiliaryContext(listReqVO.getAccountSetId(),
                listReqVO.getStartMonth(), listReqVO.getEndMonth(), listReqVO.getAuxiliaryTypeId(),
                listReqVO.getSubjectId(), userId);
        // 2. 校验辅助核算项目
        if (listReqVO.getAuxiliaryItemId() != null) {
            validateAuxiliaryItem(context, listReqVO.getAuxiliaryItemId());
        }

        // 3. 计算核算项目余额并过滤八项金额全零的项目
        List<FmsLedgerAuxiliaryBalanceRespVO> balances = convertList(context.auxiliaryItems,
                item -> buildAuxiliaryBalance(context, item),
                item -> (listReqVO.getAuxiliaryItemId() == null
                        || ObjUtil.equal(item.getId(), listReqVO.getAuxiliaryItemId()))
                        && (CollUtil.isNotEmpty(filterAuxiliaryEntries(context, item.getId()))
                        || CollUtil.isNotEmpty(filterInitialAssists(context, item.getId()))));
        return filterList(balances, this::hasAuxiliaryBalanceAmount);
    }

    // ==================== 数量金额明细账 ====================

    @Override
    public List<FmsLedgerQuantityDetailRespVO> getQuantityDetailList(
            FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载账簿上下文并校验科目
        LedgerContext context = buildContext(listReqVO, userId);
        FmsSubjectDO subject = context.subjectMap.get(listReqVO.getSubjectId());
        if (subject == null) {
            throw exception(SUBJECT_NOT_EXISTS);
        }

        // 2. 仅汇总所选子树中启用数量核算的科目
        Set<Long> subjectIds = collectSubjectIds(context, subject.getId());
        Set<Long> quantitySubjectIds = convertSet(filterList(context.subjects,
                item -> subjectIds.contains(item.getId()) && Boolean.TRUE.equals(item.getQuantityAccounting())),
                FmsSubjectDO::getId);
        List<FmsLedgerQuantityDetailRespVO> result = BeanUtils.toBean(
                buildLedgerRows(context, subject, true, quantitySubjectIds), FmsLedgerQuantityDetailRespVO.class);
        result.forEach(this::fillQuantityDetailUnitPrices);
        return result;
    }

    /**
     * 补充数量明细账的期初、本期、累计和期末单价
     *
     * @param row 数量明细账行
     */
    private void fillQuantityDetailUnitPrices(FmsLedgerQuantityDetailRespVO row) {
        // 1. 汇总当前行的数量和金额
        BigDecimal periodQuantity = zeroIfNull(row.getDebitQuantity()).add(zeroIfNull(row.getCreditQuantity()));
        BigDecimal periodAmount = zeroIfNull(row.getDebitAmount()).add(zeroIfNull(row.getCreditAmount()));
        BigDecimal balanceQuantity = zeroIfNull(row.getBalanceQuantity());
        BigDecimal balanceAmount = zeroIfNull(row.getBalance());
        BigDecimal price = row.getUnitPrice();
        // 2. 按行类型计算对应期间单价
        if (row.getRowType().equals(FmsLedgerQuantityDetailRespVO.ROW_TYPE_OPENING)) {
            row.setOpeningUnitPrice(unitPrice(balanceAmount, balanceQuantity));
        } else if (row.getRowType().equals(FmsLedgerQuantityDetailRespVO.ROW_TYPE_PERIOD_TOTAL)) {
            row.setPeriodUnitPrice(unitPrice(periodAmount, periodQuantity));
        } else if (row.getRowType().equals(FmsLedgerQuantityDetailRespVO.ROW_TYPE_YEAR_TOTAL)) {
            row.setYearUnitPrice(unitPrice(periodAmount, periodQuantity));
        } else if (row.getRowType().equals(FmsLedgerQuantityDetailRespVO.ROW_TYPE_VOUCHER)) {
            row.setPeriodUnitPrice(price);
        }
        // 3. 统一计算期末单价
        row.setEndingUnitPrice(unitPrice(balanceAmount, balanceQuantity));
    }

    private BigDecimal unitPrice(BigDecimal amount, BigDecimal quantity) {
        return quantity.signum() == 0 ? BigDecimal.ZERO : amount.divide(quantity, 6, RoundingMode.HALF_UP);
    }

    // ==================== 数量金额总账 ====================

    @Override
    public List<FmsLedgerQuantityGeneralRespVO> getQuantityGeneralList(FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 加载账簿上下文
        LedgerContext context = buildContext(listReqVO, userId);

        // 2. 计算科目余额；保留有数量业务节点及其非数量父级汇总节点
        List<FmsLedgerSubjectBalanceRespVO> balances = buildSubjectBalanceList(context, listReqVO,
                balance -> true, subject -> buildSubjectBalance(context, subject, true));
        pruneQuantityGeneralTree(balances);
        return buildQuantityGeneralRespVOList(balances);
    }

    /**
     * 构建普通账簿计算上下文
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 普通账簿计算上下文
     */
    private LedgerContext buildContext(FmsLedgerListReqVO listReqVO, Long userId) {
        // 1. 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(
                listReqVO.getAccountSetId(), userId);

        // 2. 校验会计期间
        YearMonth startMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getStartMonth());
        YearMonth endMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth());
        validateLedgerPeriod(accountSet, startMonth, endMonth);

        // 3. 加载财务参数和科目
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(
                listReqVO.getAccountSetId(), userId);
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(
                listReqVO.getAccountSetId(), null, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        Map<Long, List<Long>> childMap = new HashMap<>();
        subjects.forEach(subject -> childMap.computeIfAbsent(subject.getParentId(), key -> new ArrayList<>())
                .add(subject.getId()));

        // 4. 加载初始余额和查询期间结束前的凭证分录
        LocalDateTime endTime = endMonth.plusMonths(1).atDay(1).atStartOfDay();
        return new LedgerContext(accountSet, financeParameter, startMonth, endMonth, subjects, subjectMap, childMap,
                initialBalanceMapper.selectListByAccountSetId(listReqVO.getAccountSetId()),
                ledgerQueryMapper.selectEntryListBeforeTime(listReqVO.getAccountSetId(), endTime));
    }

    /**
     * 根据科目范围和级次筛选科目
     *
     * @param context 普通账簿计算上下文
     * @param listReqVO 列表查询参数
     * @return 科目列表
     */
    private List<FmsSubjectDO> filterSubjects(LedgerContext context, FmsLedgerListReqVO listReqVO) {
        FmsSubjectDO startSubject = getOptionalSubject(context, listReqVO.getStartSubjectId());
        FmsSubjectDO endSubject = getOptionalSubject(context, listReqVO.getEndSubjectId());
        String startCode = startSubject == null ? null : startSubject.getCode();
        String endCode = endSubject == null ? null : endSubject.getCode();
        Set<Long> endSubjectIds = endSubject == null ? Collections.emptySet()
                : collectSubjectIds(context, endSubject.getId());
        return filterList(context.subjects, subject -> (listReqVO.getMinLevel() == null
                || subject.getLevel() >= listReqVO.getMinLevel())
                && (listReqVO.getMaxLevel() == null || subject.getLevel() <= listReqVO.getMaxLevel())
                && (startCode == null || subject.getCode().compareTo(startCode) >= 0)
                && (endCode == null || subject.getCode().compareTo(endCode) <= 0
                || endSubjectIds.contains(subject.getId())));
    }

    /**
     * 构建科目余额树
     *
     * @param context 普通账簿计算上下文
     * @param listReqVO 列表查询参数
     * @param balancePredicate 余额行保留条件
     * @return 科目余额树
     */
    private List<FmsLedgerSubjectBalanceRespVO> buildSubjectBalanceList(LedgerContext context,
            FmsLedgerListReqVO listReqVO, Predicate<FmsLedgerSubjectBalanceRespVO> balancePredicate) {
        return buildSubjectBalanceList(context, listReqVO, balancePredicate,
                subject -> buildSubjectBalance(context, subject, false));
    }

    /**
     * 构建科目余额树
     *
     * @param context 普通账簿计算上下文
     * @param listReqVO 列表查询参数
     * @param balancePredicate 余额行保留条件
     * @param balanceBuilder 科目余额构建函数
     * @return 科目余额树
     */
    private List<FmsLedgerSubjectBalanceRespVO> buildSubjectBalanceList(LedgerContext context,
            FmsLedgerListReqVO listReqVO, Predicate<FmsLedgerSubjectBalanceRespVO> balancePredicate,
            Function<FmsSubjectDO, FmsLedgerSubjectBalanceRespVO> balanceBuilder) {
        // 1. 计算科目余额并过滤空行
        List<FmsSubjectDO> subjects = filterSubjects(context, listReqVO);
        Map<Long, FmsLedgerSubjectBalanceRespVO> resultMap = convertMap(subjects, FmsSubjectDO::getId,
                balanceBuilder, (oldValue, newValue) -> oldValue,
                LinkedHashMap::new);
        resultMap.values().removeIf(balancePredicate.negate());

        // 2. 组装科目树
        List<FmsLedgerSubjectBalanceRespVO> roots = new ArrayList<>();
        subjects.forEach(subject -> {
            FmsLedgerSubjectBalanceRespVO current = resultMap.get(subject.getId());
            if (current == null) {
                return;
            }
            FmsLedgerSubjectBalanceRespVO parent = resultMap.get(subject.getParentId());
            if (parent == null) {
                roots.add(current);
            } else {
                parent.getChildren().add(current);
            }
        });
        return roots;
    }

    /**
     * 获得可选科目，不存在时抛出业务异常
     *
     * @param context 普通账簿计算上下文
     * @param subjectId 科目编号
     * @return 科目
     */
    private FmsSubjectDO getOptionalSubject(LedgerContext context, Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        FmsSubjectDO subject = context.subjectMap.get(subjectId);
        if (subject == null) {
            throw exception(SUBJECT_NOT_EXISTS);
        }
        return subject;
    }

    /**
     * 构建指定科目的账簿行
     *
     * @param context 普通账簿计算上下文
     * @param subject 科目
     * @param includeVoucherRows 是否包含凭证分录行
     * @return 账簿行列表
     */
    private List<FmsLedgerDetailRespVO> buildLedgerRows(LedgerContext context, FmsSubjectDO subject, boolean includeVoucherRows) {
        return buildLedgerRows(context, subject, includeVoucherRows, collectSubjectIds(context, subject.getId()));
    }

    /**
     * 构建指定科目范围的账簿行
     *
     * @param context 普通账簿计算上下文
     * @param subject 查询科目
     * @param includeVoucherRows 是否包含凭证分录行
     * @param subjectIds 参与汇总的科目编号集合
     * @return 账簿行列表
     */
    private List<FmsLedgerDetailRespVO> buildLedgerRows(LedgerContext context, FmsSubjectDO subject,
            boolean includeVoucherRows, Set<Long> subjectIds) {
        // 1. 筛选科目及下级科目的凭证分录
        List<FmsLedgerEntryVO> entries = filterList(context.entries,
                entry -> subjectIds.contains(entry.getSubjectId()));

        // 2. 计算并添加期初余额行
        BigDecimal openingSignedAmount = calculateInitialSignedAmount(context, subjectIds)
                .add(calculateSignedAmount(entries, null, context.startMonth.atDay(1).atStartOfDay()));
        BigDecimal openingSignedQuantity = calculateInitialSignedQuantity(context, subjectIds)
                .add(calculateSignedQuantity(entries, null, context.startMonth.atDay(1).atStartOfDay()));
        List<FmsLedgerDetailRespVO> result = new ArrayList<>();
        result.add(buildSummaryRow(context, subject, FmsLedgerDetailRespVO.ROW_TYPE_OPENING, context.startMonth,
                context.startMonth.atEndOfMonth(), "期初余额", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, openingSignedAmount, openingSignedQuantity));

        // 3. 逐月计算凭证分录、本期合计和本年累计行
        BigDecimal runningSignedAmount = openingSignedAmount;
        BigDecimal runningSignedQuantity = openingSignedQuantity;
        YearMonth currentMonth = context.startMonth;
        while (!currentMonth.isAfter(context.endMonth)) {
            LocalDateTime monthBeginTime = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime nextMonthBeginTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
            List<FmsLedgerEntryVO> monthEntries = entries.stream()
                    .filter(entry -> !entry.getVoucherTime().isBefore(monthBeginTime)
                            && entry.getVoucherTime().isBefore(nextMonthBeginTime))
                    .collect(Collectors.toList());
            if (includeVoucherRows) {
                for (FmsLedgerEntryVO entry : monthEntries) {
                    runningSignedAmount = runningSignedAmount.add(zeroIfNull(entry.getDebitAmount()))
                            .subtract(zeroIfNull(entry.getCreditAmount()));
                    BigDecimal signedQuantity = signedQuantity(entry);
                    runningSignedQuantity = runningSignedQuantity.add(signedQuantity);
                    result.add(buildVoucherRow(context, subject, entry,
                            runningSignedAmount, runningSignedQuantity));
                }
            } else {
                runningSignedAmount = runningSignedAmount.add(calculateSignedAmount(
                        monthEntries, null, null));
                runningSignedQuantity = runningSignedQuantity.add(calculateSignedQuantity(
                        monthEntries, null, null));
            }
            BigDecimal periodDebitAmount = sumDebitAmount(monthEntries);
            BigDecimal periodCreditAmount = sumCreditAmount(monthEntries);
            BigDecimal periodDebitQuantity = sumQuantity(monthEntries, true);
            BigDecimal periodCreditQuantity = sumQuantity(monthEntries, false);
            result.add(buildSummaryRow(context, subject, FmsLedgerDetailRespVO.ROW_TYPE_PERIOD_TOTAL,
                    currentMonth, currentMonth.atEndOfMonth(), "本期合计", periodDebitAmount,
                    periodCreditAmount, periodDebitQuantity, periodCreditQuantity,
                    runningSignedAmount, runningSignedQuantity));

            LocalDateTime yearBeginTime = currentMonth.atDay(1).withDayOfYear(1).atStartOfDay();
            List<FmsLedgerEntryVO> yearEntries = entries.stream()
                    .filter(entry -> !entry.getVoucherTime().isBefore(yearBeginTime)
                            && entry.getVoucherTime().isBefore(nextMonthBeginTime))
                    .collect(Collectors.toList());
            BigDecimal yearDebitAmount = sumDebitAmount(yearEntries)
                    .add(calculateInitialYearAmount(context, subjectIds, true, currentMonth.getYear()));
            BigDecimal yearCreditAmount = sumCreditAmount(yearEntries)
                    .add(calculateInitialYearAmount(context, subjectIds, false, currentMonth.getYear()));
            BigDecimal yearDebitQuantity = sumQuantity(yearEntries, true)
                    .add(calculateInitialYearQuantity(context, subjectIds, true, currentMonth.getYear()));
            BigDecimal yearCreditQuantity = sumQuantity(yearEntries, false)
                    .add(calculateInitialYearQuantity(context, subjectIds, false, currentMonth.getYear()));
            result.add(buildSummaryRow(context, subject, FmsLedgerDetailRespVO.ROW_TYPE_YEAR_TOTAL,
                    currentMonth, currentMonth.atEndOfMonth(), "本年累计", yearDebitAmount,
                    yearCreditAmount, yearDebitQuantity, yearCreditQuantity,
                    runningSignedAmount, runningSignedQuantity));
            currentMonth = currentMonth.plusMonths(1);
        }
        return result;
    }

    /**
     * 构建凭证分录账簿行
     *
     * @param context 普通账簿计算上下文
     * @param subject 查询科目
     * @param entry 凭证分录
     * @param signedBalance 余额方向金额
     * @param signedQuantity 余额方向数量
     * @return 凭证分录账簿行
     */
    private FmsLedgerDetailRespVO buildVoucherRow(LedgerContext context, FmsSubjectDO subject,
            FmsLedgerEntryVO entry, BigDecimal signedBalance, BigDecimal signedQuantity) {
        return fillBalance(context, subject, new FmsLedgerDetailRespVO()
                .setRowType(FmsLedgerDetailRespVO.ROW_TYPE_VOUCHER)
                .setEntryId(entry.getEntryId()).setEntrySubjectId(entry.getSubjectId())
                .setSubjectId(subject.getId()).setSubjectCode(subject.getCode())
                .setSubjectName(subject.getName()).setPeriod(YearMonth.from(entry.getVoucherTime()).toString())
                .setAccountDate(entry.getVoucherTime().toLocalDate()).setVoucherId(entry.getVoucherId())
                .setVoucherNumber(entry.getVoucherWordName() + "-" + entry.getVoucherNumber())
                .setDigest(entry.getDigest()).setDebitAmount(zeroIfNull(entry.getDebitAmount()))
                .setCreditAmount(zeroIfNull(entry.getCreditAmount()))
                .setDebitQuantity(isDebit(entry) ? zeroIfNull(entry.getQuantity()) : BigDecimal.ZERO)
                .setCreditQuantity(isDebit(entry) ? BigDecimal.ZERO : zeroIfNull(entry.getQuantity()))
                .setUnitPrice(zeroIfNull(entry.getUnitPrice())).setQuantityUnit(subject.getQuantityUnit()),
                signedBalance, signedQuantity);
    }

    /**
     * 构建多栏账各科目列金额
     *
     * @param context 普通账簿计算上下文
     * @param columns 多栏账科目列
     * @param row 账簿行
     * @return 科目编号与金额 Map
     */
    private Map<Long, BigDecimal> buildColumnAmounts(LedgerContext context,
            List<FmsSubjectDO> columns, FmsLedgerDetailRespVO row) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (FmsSubjectDO column : columns) {
            BigDecimal amount;
            if (row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_VOUCHER)) {
                amount = ObjUtil.equal(row.getEntrySubjectId(), column.getId())
                        ? nominalAmount(column, row.getDebitAmount(), row.getCreditAmount()) : BigDecimal.ZERO;
            } else {
                amount = calculateColumnSummaryAmount(context, column, row);
            }
            result.put(column.getId(), amount);
        }
        return result;
    }

    /**
     * 为多栏账每月追加期末余额行
     *
     * @param rows 账簿行
     * @return 包含每月期末余额的账簿行
     */
    private List<FmsLedgerDetailRespVO> appendMultiColumnEndingRows(List<FmsLedgerDetailRespVO> rows) {
        List<FmsLedgerDetailRespVO> result = new ArrayList<>();
        for (FmsLedgerDetailRespVO row : rows) {
            result.add(row);
            if (!ObjUtil.equal(row.getRowType(), FmsLedgerDetailRespVO.ROW_TYPE_YEAR_TOTAL)) {
                continue;
            }
            YearMonth period = LocalDateTimeUtils.parseYearMonth(row.getPeriod());
            result.add(BeanUtils.toBean(row, FmsLedgerDetailRespVO.class)
                    .setRowType(FmsLedgerDetailRespVO.ROW_TYPE_ENDING)
                    .setAccountDate(period.atEndOfMonth()).setDigest("期末余额")
                    .setDebitAmount(BigDecimal.ZERO).setCreditAmount(BigDecimal.ZERO)
                    .setColumnAmounts(null));
        }
        return result;
    }

    /**
     * 计算多栏账科目汇总金额
     *
     * @param context 普通账簿计算上下文
     * @param column 多栏账科目列
     * @param row 汇总账簿行
     * @return 科目汇总金额
     */
    private BigDecimal calculateColumnSummaryAmount(LedgerContext context,
            FmsSubjectDO column, FmsLedgerDetailRespVO row) {
        List<FmsLedgerEntryVO> entries = filterList(context.entries,
                entry -> ObjUtil.equal(entry.getSubjectId(), column.getId()));
        YearMonth period = LocalDateTimeUtils.parseYearMonth(row.getPeriod());
        if (row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_OPENING)) {
            BigDecimal initialAmount = sumBigDecimal(filterList(context.initialBalances,
                    initial -> ObjUtil.equal(initial.getSubjectId(), column.getId())),
                    FmsInitialBalanceDO::getOpeningAmount);
            return initialAmount.add(sumBigDecimal(entries, entry -> entry.getVoucherTime()
                    .isBefore(period.atDay(1).atStartOfDay())
                    ? nominalAmount(column, entry.getDebitAmount(), entry.getCreditAmount()) : BigDecimal.ZERO));
        }
        LocalDateTime nextMonthBeginTime = period.plusMonths(1).atDay(1).atStartOfDay();
        if (row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_ENDING)) {
            BigDecimal initialAmount = sumBigDecimal(filterList(context.initialBalances,
                    initial -> ObjUtil.equal(initial.getSubjectId(), column.getId())),
                    FmsInitialBalanceDO::getOpeningAmount);
            return initialAmount.add(sumBigDecimal(entries, entry -> entry.getVoucherTime()
                    .isBefore(nextMonthBeginTime)
                    ? nominalAmount(column, entry.getDebitAmount(), entry.getCreditAmount()) : BigDecimal.ZERO));
        }
        LocalDateTime beginTime = row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_YEAR_TOTAL)
                ? LocalDate.of(period.getYear(), 1, 1).atStartOfDay() : period.atDay(1).atStartOfDay();
        BigDecimal amount = sumBigDecimal(filterEntriesByTime(entries, beginTime, nextMonthBeginTime),
                entry -> nominalAmount(column, entry.getDebitAmount(), entry.getCreditAmount()));
        if (row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_YEAR_TOTAL)
                && context.accountSet.getStartTime().getYear() == period.getYear()) {
            amount = amount.add(sumBigDecimal(filterList(context.initialBalances,
                    initial -> ObjUtil.equal(initial.getSubjectId(), column.getId())),
                    initial -> ObjUtil.equal(column.getBalanceDirection(),
                            FmsDebitCreditDirectionEnum.DEBIT.getType())
                            ? zeroIfNull(initial.getYearDebitAmount()).subtract(
                                    zeroIfNull(initial.getYearCreditAmount()))
                            : zeroIfNull(initial.getYearCreditAmount()).subtract(
                                    zeroIfNull(initial.getYearDebitAmount()))));
        }
        return amount;
    }

    /**
     * 按科目余额方向计算名义金额
     *
     * @param subject 科目
     * @param debitAmount 借方金额
     * @param creditAmount 贷方金额
     * @return 名义金额
     */
    private BigDecimal nominalAmount(FmsSubjectDO subject, BigDecimal debitAmount,
            BigDecimal creditAmount) {
        return ObjUtil.equal(subject.getBalanceDirection(), FmsDebitCreditDirectionEnum.DEBIT.getType())
                ? zeroIfNull(debitAmount).subtract(zeroIfNull(creditAmount))
                : zeroIfNull(creditAmount).subtract(zeroIfNull(debitAmount));
    }

    /**
     * 构建账簿汇总行
     *
     * @param context 普通账簿计算上下文
     * @param subject 科目
     * @param rowType 行类型
     * @param period 会计期间
     * @param accountDate 记账日期
     * @param digest 摘要
     * @param debitAmount 借方金额
     * @param creditAmount 贷方金额
     * @param debitQuantity 借方数量
     * @param creditQuantity 贷方数量
     * @param signedBalance 余额方向金额
     * @param signedQuantity 余额方向数量
     * @return 账簿汇总行
     */
    private FmsLedgerDetailRespVO buildSummaryRow(LedgerContext context, FmsSubjectDO subject,
                                                  Integer rowType, YearMonth period, LocalDate accountDate, String digest,
                                                  BigDecimal debitAmount, BigDecimal creditAmount, BigDecimal debitQuantity,
                                                  BigDecimal creditQuantity, BigDecimal signedBalance, BigDecimal signedQuantity) {
        return fillBalance(context, subject, new FmsLedgerDetailRespVO().setRowType(rowType)
                .setSubjectId(subject.getId()).setSubjectCode(subject.getCode())
                .setSubjectName(subject.getName()).setPeriod(period.toString()).setAccountDate(accountDate)
                .setDigest(digest).setDebitAmount(debitAmount).setCreditAmount(creditAmount)
                .setDebitQuantity(debitQuantity).setCreditQuantity(creditQuantity)
                .setQuantityUnit(subject.getQuantityUnit()), signedBalance, signedQuantity);
    }

    /**
     * 按账簿余额方向模式填充余额
     *
     * @param context 普通账簿计算上下文
     * @param subject 科目
     * @param result 账簿行
     * @param signedBalance 余额方向金额
     * @param signedQuantity 余额方向数量
     * @return 账簿行
     */
    private FmsLedgerDetailRespVO fillBalance(LedgerContext context, FmsSubjectDO subject, FmsLedgerDetailRespVO result,
                                              BigDecimal signedBalance, BigDecimal signedQuantity) {
        Integer balanceMode = context.financeParameter.getLedgerBalanceMode();
        if (ObjUtil.equal(balanceMode, FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode())) {
            boolean debitSubject = ObjUtil.equal(subject.getBalanceDirection(),
                    FmsDebitCreditDirectionEnum.DEBIT.getType());
            result.setBalanceDirection(signedBalance.signum() == 0 ? FmsBalanceDirectionEnum.FLAT.getName()
                    : FmsDebitCreditDirectionEnum.valueOf(subject.getBalanceDirection()).getName())
                    .setBalance(debitSubject ? signedBalance : signedBalance.negate())
                    .setBalanceQuantity(debitSubject ? signedQuantity : signedQuantity.negate());
        } else {
            result.setBalanceDirection(balanceDirection(signedBalance))
                    .setBalance(signedBalance.abs()).setBalanceQuantity(signedQuantity.abs());
        }
        return result;
    }

    /**
     * 构建科目余额
     *
     * @param context 普通账簿计算上下文
     * @param subject 科目
     * @return 科目余额
     */
    private FmsLedgerSubjectBalanceRespVO buildSubjectBalance(
            LedgerContext context, FmsSubjectDO subject, boolean quantityGeneral) {
        // 1. 筛选科目及下级科目的凭证分录
        Set<Long> subjectIds = collectSubjectIds(context, subject.getId());
        List<FmsLedgerEntryVO> entries = filterList(context.entries,
                entry -> subjectIds.contains(entry.getSubjectId()));

        // 2. 计算期初余额和查询期间发生额
        LocalDateTime startTime = context.startMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = context.endMonth.plusMonths(1).atDay(1).atStartOfDay();
        BigDecimal openingSignedAmount = calculateInitialSignedAmount(context, subjectIds)
                .add(calculateSignedAmount(entries, null, startTime));
        BigDecimal openingSignedQuantity = calculateInitialSignedQuantity(context, subjectIds)
                .add(calculateSignedQuantity(entries, null, startTime));
        List<FmsLedgerEntryVO> periodEntries = filterEntriesByTime(entries, startTime, endTime);
        LocalDateTime yearBeginTime = LocalDate.of(context.endMonth.getYear(), 1, 1).atStartOfDay();
        List<FmsLedgerEntryVO> yearEntries = filterEntriesByTime(entries, yearBeginTime, endTime);
        BigDecimal endingSignedAmount = openingSignedAmount.add(calculateSignedAmount(
                periodEntries, null, null));
        BigDecimal endingSignedQuantity = openingSignedQuantity.add(calculateSignedQuantity(
                periodEntries, null, null));
        BigDecimal yearDebitQuantity = sumQuantity(yearEntries, true).add(calculateInitialYearQuantity(
                context, subjectIds, true, context.endMonth.getYear()));
        BigDecimal yearCreditQuantity = sumQuantity(yearEntries, false).add(calculateInitialYearQuantity(
                context, subjectIds, false, context.endMonth.getYear()));

        // 3. 组装科目余额响应
        FmsLedgerSubjectBalanceRespVO result = new FmsLedgerSubjectBalanceRespVO()
                .setNodeKey("S:" + subject.getId())
                .setNodeType(FmsLedgerSubjectBalanceRespVO.NODE_TYPE_SUBJECT)
                .setSubjectId(subject.getId())
                .setSubjectCode(subject.getCode()).setSubjectName(subject.getName())
                .setLevel(subject.getLevel()).setQuantityAccounting(subject.getQuantityAccounting())
                .setQuantityUnit(subject.getQuantityUnit())
                .setOpeningDebitAmount(debitPart(openingSignedAmount))
                .setOpeningCreditAmount(creditPart(openingSignedAmount))
                .setOpeningBalanceDirection(balanceDirection(openingSignedAmount))
                .setOpeningQuantity(openingSignedQuantity.abs())
                .setOpeningUnitPrice(calculateUnitPrice(openingSignedAmount, openingSignedQuantity))
                .setPeriodDebitAmount(sumDebitAmount(periodEntries))
                .setPeriodCreditAmount(sumCreditAmount(periodEntries))
                .setPeriodDebitQuantity(sumQuantity(periodEntries, true))
                .setPeriodCreditQuantity(sumQuantity(periodEntries, false))
                .setYearDebitAmount(sumDebitAmount(yearEntries).add(calculateInitialYearAmount(
                        context, subjectIds, true, context.endMonth.getYear())))
                .setYearCreditAmount(sumCreditAmount(yearEntries).add(calculateInitialYearAmount(
                        context, subjectIds, false, context.endMonth.getYear())))
                .setYearDebitQuantity(yearDebitQuantity).setYearCreditQuantity(yearCreditQuantity)
                .setEndingDebitAmount(debitPart(endingSignedAmount))
                .setEndingCreditAmount(creditPart(endingSignedAmount))
                .setEndingBalanceDirection(balanceDirection(endingSignedAmount))
                .setEndingQuantity(endingSignedQuantity.abs())
                .setEndingUnitPrice(calculateUnitPrice(endingSignedAmount, endingSignedQuantity));
        if (quantityGeneral && ObjUtil.equal(context.financeParameter.getLedgerBalanceMode(),
                FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode())) {
            fillQuantityGeneralBalance(subject, result, openingSignedAmount, openingSignedQuantity,
                    endingSignedAmount, endingSignedQuantity);
        }
        return result;
    }

    /**
     * 构建科目对应的辅助核算组合余额 Map
     *
     * @param context 普通账簿计算上下文
     * @param auxiliaryItemMap 辅助核算项目 Map
     * @return 科目编号与辅助核算组合余额 Map
     */
    private Map<Long, List<FmsLedgerSubjectBalanceRespVO>> buildAuxiliaryCombinationBalanceMap(
            LedgerContext context, Map<Long, FmsAuxiliaryItemDO> auxiliaryItemMap) {
        // 1. 收集期初余额和凭证分录中实际使用的辅助核算组合
        Map<Long, AuxiliaryCombinationRecord> combinationMap = new LinkedHashMap<>();
        for (FmsInitialBalanceDO initial : context.initialBalances) {
            for (FmsInitialBalanceDO.AssistBalance assist : CollUtil.emptyIfNull(initial.getAssistBalances())) {
                if (assist.getAssistCombinationId() == null) {
                    continue;
                }
                AuxiliaryCombinationRecord record = combinationMap.computeIfAbsent(
                        assist.getAssistCombinationId(), id -> new AuxiliaryCombinationRecord(
                                id, initial.getSubjectId(), buildAuxiliarySnapshots(assist.getAuxiliaries()),
                                new ArrayList<>()));
                record.initialAssists.add(assist);
            }
        }
        for (FmsLedgerEntryVO entry : context.entries) {
            if (entry.getAssistCombinationId() == null || CollUtil.isEmpty(entry.getAuxiliaries())) {
                continue;
            }
            combinationMap.computeIfAbsent(entry.getAssistCombinationId(), id -> new AuxiliaryCombinationRecord(
                    id, entry.getSubjectId(), buildAuxiliarySnapshotsFromEntries(entry.getAuxiliaries()),
                    new ArrayList<>()));
        }

        // 2. 计算非零组合余额并按科目分组
        Map<Long, List<FmsLedgerSubjectBalanceRespVO>> result = new LinkedHashMap<>();
        for (AuxiliaryCombinationRecord record : combinationMap.values()) {
            FmsSubjectDO subject = context.subjectMap.get(record.subjectId);
            if (subject == null) {
                continue;
            }
            FmsLedgerSubjectBalanceRespVO balance = buildAuxiliaryCombinationBalance(
                    context, subject, record, auxiliaryItemMap);
            if (hasSubjectBalanceAmount(balance)) {
                result.computeIfAbsent(record.subjectId, key -> new ArrayList<>()).add(balance);
            }
        }
        return result;
    }

    /**
     * 构建辅助核算组合余额
     *
     * @param context 普通账簿计算上下文
     * @param subject 科目
     * @param record 辅助核算组合记录
     * @param auxiliaryItemMap 辅助核算项目 Map
     * @return 辅助核算组合余额
     */
    private FmsLedgerSubjectBalanceRespVO buildAuxiliaryCombinationBalance(LedgerContext context,
            FmsSubjectDO subject, AuxiliaryCombinationRecord record,
            Map<Long, FmsAuxiliaryItemDO> auxiliaryItemMap) {
        // 1. 筛选组合分录并计算期初、期末余额
        List<FmsLedgerEntryVO> entries = filterList(context.entries,
                entry -> ObjUtil.equal(entry.getSubjectId(), subject.getId())
                        && ObjUtil.equal(entry.getAssistCombinationId(), record.id));
        LocalDateTime startTime = context.startMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = context.endMonth.plusMonths(1).atDay(1).atStartOfDay();
        boolean debitSubject = ObjUtil.equal(subject.getBalanceDirection(),
                FmsDebitCreditDirectionEnum.DEBIT.getType());
        BigDecimal initialAmount = sumBigDecimal(record.initialAssists,
                assist -> zeroIfNull(assist.getOpeningAmount()));
        BigDecimal initialQuantity = sumBigDecimal(record.initialAssists,
                assist -> zeroIfNull(assist.getOpeningQuantity()));
        BigDecimal openingSignedAmount = (debitSubject ? initialAmount : initialAmount.negate())
                .add(calculateSignedAmount(entries, null, startTime));
        BigDecimal openingSignedQuantity = (debitSubject ? initialQuantity : initialQuantity.negate())
                .add(calculateSignedQuantity(entries, null, startTime));
        List<FmsLedgerEntryVO> periodEntries = filterEntriesByTime(entries, startTime, endTime);
        BigDecimal endingSignedAmount = openingSignedAmount.add(calculateSignedAmount(
                periodEntries, null, null));
        BigDecimal endingSignedQuantity = openingSignedQuantity.add(calculateSignedQuantity(
                periodEntries, null, null));

        // 2. 计算本年累计发生额
        LocalDateTime yearBeginTime = LocalDate.of(context.endMonth.getYear(), 1, 1).atStartOfDay();
        List<FmsLedgerEntryVO> yearEntries = filterEntriesByTime(entries, yearBeginTime, endTime);
        BigDecimal initialYearDebitAmount = BigDecimal.ZERO;
        BigDecimal initialYearCreditAmount = BigDecimal.ZERO;
        BigDecimal initialYearDebitQuantity = BigDecimal.ZERO;
        BigDecimal initialYearCreditQuantity = BigDecimal.ZERO;
        if (context.accountSet.getStartTime().getYear() == context.endMonth.getYear()) {
            initialYearDebitAmount = sumBigDecimal(record.initialAssists,
                    assist -> zeroIfNull(assist.getYearDebitAmount()));
            initialYearCreditAmount = sumBigDecimal(record.initialAssists,
                    assist -> zeroIfNull(assist.getYearCreditAmount()));
            initialYearDebitQuantity = sumBigDecimal(record.initialAssists,
                    assist -> zeroIfNull(assist.getYearDebitQuantity()));
            initialYearCreditQuantity = sumBigDecimal(record.initialAssists,
                    assist -> zeroIfNull(assist.getYearCreditQuantity()));
        }

        // 3. 组装辅助核算组合节点
        String codeSuffix = record.items.stream().map(item -> {
            FmsAuxiliaryItemDO auxiliaryItem = auxiliaryItemMap.get(item.itemId);
            return auxiliaryItem == null ? String.valueOf(item.itemId) : auxiliaryItem.getCode();
        }).collect(Collectors.joining("_"));
        String nameSuffix = record.items.stream().map(item -> {
            FmsAuxiliaryItemDO auxiliaryItem = auxiliaryItemMap.get(item.itemId);
            return item.name != null ? item.name
                    : auxiliaryItem == null ? String.valueOf(item.itemId) : auxiliaryItem.getName();
        }).collect(Collectors.joining("_"));
        return new FmsLedgerSubjectBalanceRespVO().setNodeKey("A:" + record.id)
                .setNodeType(FmsLedgerSubjectBalanceRespVO.NODE_TYPE_AUXILIARY_COMBINATION)
                .setSubjectId(subject.getId()).setAssistCombinationId(record.id)
                .setSubjectCode(subject.getCode() + "_" + codeSuffix)
                .setSubjectName(subject.getName() + "_" + nameSuffix).setLevel(subject.getLevel() + 1)
                .setQuantityAccounting(subject.getQuantityAccounting()).setQuantityUnit(subject.getQuantityUnit())
                .setOpeningDebitAmount(debitPart(openingSignedAmount))
                .setOpeningCreditAmount(creditPart(openingSignedAmount))
                .setOpeningBalanceDirection(balanceDirection(openingSignedAmount))
                .setOpeningQuantity(openingSignedQuantity.abs())
                .setOpeningUnitPrice(calculateUnitPrice(openingSignedAmount, openingSignedQuantity))
                .setPeriodDebitAmount(sumDebitAmount(periodEntries))
                .setPeriodCreditAmount(sumCreditAmount(periodEntries))
                .setPeriodDebitQuantity(sumQuantity(periodEntries, true))
                .setPeriodCreditQuantity(sumQuantity(periodEntries, false))
                .setYearDebitAmount(sumDebitAmount(yearEntries).add(initialYearDebitAmount))
                .setYearCreditAmount(sumCreditAmount(yearEntries).add(initialYearCreditAmount))
                .setYearDebitQuantity(sumQuantity(yearEntries, true).add(initialYearDebitQuantity))
                .setYearCreditQuantity(sumQuantity(yearEntries, false).add(initialYearCreditQuantity))
                .setEndingDebitAmount(debitPart(endingSignedAmount))
                .setEndingCreditAmount(creditPart(endingSignedAmount))
                .setEndingBalanceDirection(balanceDirection(endingSignedAmount))
                .setEndingQuantity(endingSignedQuantity.abs())
                .setEndingUnitPrice(calculateUnitPrice(endingSignedAmount, endingSignedQuantity));
    }

    /**
     * 将辅助核算组合余额挂到对应科目节点
     *
     * @param balances 科目余额树
     * @param auxiliaryBalanceMap 科目编号与辅助核算组合余额 Map
     */
    private void appendAuxiliaryCombinationBalances(List<FmsLedgerSubjectBalanceRespVO> balances,
            Map<Long, List<FmsLedgerSubjectBalanceRespVO>> auxiliaryBalanceMap) {
        for (FmsLedgerSubjectBalanceRespVO balance : balances) {
            appendAuxiliaryCombinationBalances(balance.getChildren(), auxiliaryBalanceMap);
            balance.getChildren().addAll(auxiliaryBalanceMap.getOrDefault(
                    balance.getSubjectId(), Collections.emptyList()));
        }
    }

    private List<AuxiliarySnapshot> buildAuxiliarySnapshots(
            List<FmsInitialBalanceDO.AuxiliaryItem> auxiliaries) {
        return convertList(CollUtil.emptyIfNull(auxiliaries),
                item -> new AuxiliarySnapshot(item.getItemId(), item.getName()));
    }

    private List<AuxiliarySnapshot> buildAuxiliarySnapshotsFromEntries(
            List<FmsVoucherEntryDO.AuxiliaryItem> auxiliaries) {
        return convertList(CollUtil.emptyIfNull(auxiliaries),
                item -> new AuxiliarySnapshot(item.getItemId(), item.getName()));
    }

    /**
     * 按科目方向填充数量金额总账余额
     *
     * @param subject 科目
     * @param result 科目余额
     * @param openingSignedAmount 期初借贷净额
     * @param openingSignedQuantity 期初借贷净数量
     * @param endingSignedAmount 期末借贷净额
     * @param endingSignedQuantity 期末借贷净数量
     */
    private void fillQuantityGeneralBalance(FmsSubjectDO subject, FmsLedgerSubjectBalanceRespVO result,
            BigDecimal openingSignedAmount, BigDecimal openingSignedQuantity,
            BigDecimal endingSignedAmount, BigDecimal endingSignedQuantity) {
        boolean debitSubject = ObjUtil.equal(subject.getBalanceDirection(), FmsDebitCreditDirectionEnum.DEBIT.getType());
        String subjectDirection = FmsDebitCreditDirectionEnum.valueOf(subject.getBalanceDirection()).getName();
        BigDecimal openingBalance = debitSubject ? openingSignedAmount : openingSignedAmount.negate();
        BigDecimal endingBalance = debitSubject ? endingSignedAmount : endingSignedAmount.negate();
        result.setOpeningBalanceDirection(openingBalance.signum() == 0 ? FmsBalanceDirectionEnum.FLAT.getName() : subjectDirection)
                .setOpeningDebitAmount(debitSubject ? openingBalance : BigDecimal.ZERO)
                .setOpeningCreditAmount(debitSubject ? BigDecimal.ZERO : openingBalance)
                .setOpeningQuantity(debitSubject ? openingSignedQuantity : openingSignedQuantity.negate())
                .setEndingBalanceDirection(endingBalance.signum() == 0 ? FmsBalanceDirectionEnum.FLAT.getName() : subjectDirection)
                .setEndingDebitAmount(debitSubject ? endingBalance : BigDecimal.ZERO)
                .setEndingCreditAmount(debitSubject ? BigDecimal.ZERO : endingBalance)
                .setEndingQuantity(debitSubject ? endingSignedQuantity : endingSignedQuantity.negate());
    }

    /**
     * 判断科目余额表是否存在非零金额
     *
     * @param balance 科目余额
     * @return 是否存在非零金额
     */
    private boolean hasSubjectBalanceAmount(FmsLedgerSubjectBalanceRespVO balance) {
        return hasAnyNonZero(balance.getOpeningDebitAmount(), balance.getOpeningCreditAmount(),
                balance.getPeriodDebitAmount(), balance.getPeriodCreditAmount(),
                balance.getYearDebitAmount(), balance.getYearCreditAmount(),
                balance.getEndingDebitAmount(), balance.getEndingCreditAmount());
    }

    /**
     * 判断数量金额总账期初或本期是否存在非零金额、数量
     *
     * @param balance 科目余额
     * @return 期初或本期是否存在非零金额、数量
     */
    private boolean hasQuantityGeneralBalance(FmsLedgerSubjectBalanceRespVO balance) {
        return hasAnyNonZero(balance.getOpeningDebitAmount(), balance.getOpeningCreditAmount(),
                balance.getPeriodDebitAmount(), balance.getPeriodCreditAmount(), balance.getOpeningQuantity(),
                balance.getPeriodDebitQuantity(), balance.getPeriodCreditQuantity());
    }

    /**
     * 裁剪数量金额总账树：保留数量核算节点及其必要的父级汇总节点。
     *
     * @param balances 科目余额树
     */
    private void pruneQuantityGeneralTree(List<FmsLedgerSubjectBalanceRespVO> balances) {
        for (int index = balances.size() - 1; index >= 0; index--) {
            FmsLedgerSubjectBalanceRespVO balance = balances.get(index);
            pruneQuantityGeneralTree(balance.getChildren());
            if (CollUtil.isEmpty(balance.getChildren()) && !hasQuantityGeneralBalance(balance)) {
                balances.remove(index);
            }
        }
    }

    /**
     * 判断核算项目余额表是否存在非零金额
     *
     * @param balance 核算项目余额
     * @return 是否存在非零金额
     */
    private boolean hasAuxiliaryBalanceAmount(FmsLedgerAuxiliaryBalanceRespVO balance) {
        return hasAnyNonZero(balance.getOpeningDebitAmount(), balance.getOpeningCreditAmount(),
                balance.getPeriodDebitAmount(), balance.getPeriodCreditAmount(),
                balance.getYearDebitAmount(), balance.getYearCreditAmount(),
                balance.getEndingDebitAmount(), balance.getEndingCreditAmount());
    }

    /**
     * 判断数值数组中是否存在非零值
     *
     * @param values 数值数组
     * @return 是否存在非零值
     */
    private boolean hasAnyNonZero(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (zeroIfNull(value).signum() != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 收集科目及其全部下级科目编号
     *
     * @param context 普通账簿计算上下文
     * @param subjectId 科目编号
     * @return 科目编号集合
     */
    private Set<Long> collectSubjectIds(LedgerContext context, Long subjectId) {
        Set<Long> result = new LinkedHashSet<>();
        collectSubjectIds(context.childMap, subjectId, result);
        return result;
    }

    /**
     * 递归收集科目及其全部下级科目编号
     *
     * @param childMap 父科目编号与子科目编号 Map
     * @param subjectId 科目编号
     * @param result 科目编号集合
     */
    private void collectSubjectIds(Map<Long, List<Long>> childMap, Long subjectId, Set<Long> result) {
        result.add(subjectId);
        for (Long childId : childMap.getOrDefault(subjectId, Collections.emptyList())) {
            collectSubjectIds(childMap, childId, result);
        }
    }

    /**
     * 计算科目初始余额方向金额
     *
     * @param context 普通账簿计算上下文
     * @param subjectIds 科目编号集合
     * @return 初始余额方向金额
     */
    private BigDecimal calculateInitialSignedAmount(LedgerContext context, Set<Long> subjectIds) {
        BigDecimal result = BigDecimal.ZERO;
        for (FmsInitialBalanceDO initial : context.initialBalances) {
            if (subjectIds.contains(initial.getSubjectId())) {
                FmsSubjectDO subject = context.subjectMap.get(initial.getSubjectId());
                result = result.add(ObjUtil.equal(subject.getBalanceDirection(),
                        FmsDebitCreditDirectionEnum.DEBIT.getType())
                        ? zeroIfNull(initial.getOpeningAmount()) : zeroIfNull(initial.getOpeningAmount()).negate());
            }
        }
        return result;
    }

    /**
     * 计算科目初始余额方向数量
     *
     * @param context 普通账簿计算上下文
     * @param subjectIds 科目编号集合
     * @return 初始余额方向数量
     */
    private BigDecimal calculateInitialSignedQuantity(LedgerContext context, Set<Long> subjectIds) {
        BigDecimal result = BigDecimal.ZERO;
        for (FmsInitialBalanceDO initial : context.initialBalances) {
            if (subjectIds.contains(initial.getSubjectId())) {
                FmsSubjectDO subject = context.subjectMap.get(initial.getSubjectId());
                result = result.add(ObjUtil.equal(subject.getBalanceDirection(),
                        FmsDebitCreditDirectionEnum.DEBIT.getType())
                        ? zeroIfNull(initial.getOpeningQuantity())
                        : zeroIfNull(initial.getOpeningQuantity()).negate());
            }
        }
        return result;
    }

    /**
     * 计算账套启用年度的年初累计发生额
     *
     * @param context 普通账簿计算上下文
     * @param subjectIds 科目编号集合
     * @param debit 是否借方
     * @param year 年度
     * @return 年初累计发生额
     */
    private BigDecimal calculateInitialYearAmount(LedgerContext context, Set<Long> subjectIds, boolean debit, int year) {
        if (context.accountSet.getStartTime().getYear() != year) {
            return BigDecimal.ZERO;
        }
        return sumBigDecimal(filterList(context.initialBalances,
                initial -> subjectIds.contains(initial.getSubjectId())),
                initial -> debit ? initial.getYearDebitAmount() : initial.getYearCreditAmount());
    }

    /**
     * 计算账套启用年度的年初累计数量
     *
     * @param context 普通账簿计算上下文
     * @param subjectIds 科目编号集合
     * @param debit 是否借方
     * @param year 年度
     * @return 年初累计数量
     */
    private BigDecimal calculateInitialYearQuantity(LedgerContext context, Set<Long> subjectIds, boolean debit, int year) {
        if (context.accountSet.getStartTime().getYear() != year) {
            return BigDecimal.ZERO;
        }
        return sumBigDecimal(filterList(context.initialBalances,
                initial -> subjectIds.contains(initial.getSubjectId())),
                initial -> debit ? initial.getYearDebitQuantity() : initial.getYearCreditQuantity());
    }

    /**
     * 计算指定时间范围的借贷方向净额
     *
     * @param entries 凭证分录列表
     * @param beginTime 开始时间，为空时不限制
     * @param endTime 结束时间，为空时不限制
     * @return 借贷方向净额
     */
    private BigDecimal calculateSignedAmount(List<FmsLedgerEntryVO> entries,
                                             LocalDateTime beginTime, LocalDateTime endTime) {
        return sumBigDecimal(filterEntriesByTime(entries, beginTime, endTime),
                entry -> zeroIfNull(entry.getDebitAmount()).subtract(zeroIfNull(entry.getCreditAmount())));
    }

    /**
     * 计算指定时间范围的借贷方向净数量
     *
     * @param entries 凭证分录列表
     * @param beginTime 开始时间，为空时不限制
     * @param endTime 结束时间，为空时不限制
     * @return 借贷方向净数量
     */
    private BigDecimal calculateSignedQuantity(List<FmsLedgerEntryVO> entries,
                                               LocalDateTime beginTime, LocalDateTime endTime) {
        return sumBigDecimal(filterEntriesByTime(entries, beginTime, endTime), this::signedQuantity);
    }

    /**
     * 计算凭证分录的借贷方向数量
     *
     * @param entry 凭证分录
     * @return 借贷方向数量
     */
    private BigDecimal signedQuantity(FmsLedgerEntryVO entry) {
        return isDebit(entry) ? zeroIfNull(entry.getQuantity()) : zeroIfNull(entry.getQuantity()).negate();
    }

    /**
     * 汇总借方金额
     *
     * @param entries 凭证分录列表
     * @return 借方金额
     */
    private BigDecimal sumDebitAmount(List<FmsLedgerEntryVO> entries) {
        return sumBigDecimal(entries, FmsLedgerEntryVO::getDebitAmount);
    }

    /**
     * 汇总贷方金额
     *
     * @param entries 凭证分录列表
     * @return 贷方金额
     */
    private BigDecimal sumCreditAmount(List<FmsLedgerEntryVO> entries) {
        return sumBigDecimal(entries, FmsLedgerEntryVO::getCreditAmount);
    }

    /**
     * 汇总指定借贷方向的数量
     *
     * @param entries 凭证分录列表
     * @param debit 是否借方
     * @return 数量
     */
    private BigDecimal sumQuantity(List<FmsLedgerEntryVO> entries, boolean debit) {
        return sumBigDecimal(filterList(entries, entry -> debit == isDebit(entry)),
                FmsLedgerEntryVO::getQuantity);
    }

    /**
     * 判断凭证分录是否为借方分录
     *
     * @param entry 凭证分录
     * @return 是否为借方分录
     */
    private boolean isDebit(FmsLedgerEntryVO entry) {
        return zeroIfNull(entry.getDebitAmount()).signum() != 0;
    }

    /**
     * 提取借方余额
     *
     * @param signedAmount 余额方向金额
     * @return 借方余额
     */
    private BigDecimal debitPart(BigDecimal signedAmount) {
        return signedAmount.signum() > 0 ? signedAmount : BigDecimal.ZERO;
    }

    /**
     * 提取贷方余额
     *
     * @param signedAmount 余额方向金额
     * @return 贷方余额
     */
    private BigDecimal creditPart(BigDecimal signedAmount) {
        return signedAmount.signum() < 0 ? signedAmount.abs() : BigDecimal.ZERO;
    }

    /**
     * 获得余额方向
     *
     * @param signedAmount 余额方向金额
     * @return 余额方向
     */
    private String balanceDirection(BigDecimal signedAmount) {
        return FmsBalanceDirectionEnum.valueOf(signedAmount).getName();
    }

    /**
     * 根据结存金额和数量计算单价
     *
     * @param signedAmount 余额方向金额
     * @param signedQuantity 余额方向数量
     * @return 单价
     */
    private BigDecimal calculateUnitPrice(BigDecimal signedAmount, BigDecimal signedQuantity) {
        if (signedQuantity.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return signedAmount.abs().divide(signedQuantity.abs(), 6, RoundingMode.HALF_UP);
    }

    /**
     * 构建辅助核算账簿计算上下文
     *
     * @param accountSetId 账套编号
     * @param startMonthValue 开始会计期间
     * @param endMonthValue 结束会计期间
     * @param auxiliaryTypeId 辅助核算类别编号
     * @param subjectId 科目编号
     * @param userId 用户编号
     * @return 辅助核算账簿计算上下文
     */
    private AuxiliaryLedgerContext buildAuxiliaryContext(Long accountSetId, String startMonthValue,
                                                         String endMonthValue, Long auxiliaryTypeId, Long subjectId, Long userId) {
        // 1. 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 校验会计期间
        YearMonth startMonth = LocalDateTimeUtils.parseYearMonth(startMonthValue);
        YearMonth endMonth = LocalDateTimeUtils.parseYearMonth(endMonthValue);
        validateLedgerPeriod(accountSet, startMonth, endMonth);

        // 3. 加载财务参数、辅助核算项目和科目
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(accountSetId, userId);
        List<FmsAuxiliaryItemDO> auxiliaryItems = auxiliaryItemService.getAuxiliaryItemList(
                accountSetId, auxiliaryTypeId, userId);
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        Map<Long, List<Long>> childMap = new HashMap<>();
        subjects.forEach(subject -> childMap.computeIfAbsent(subject.getParentId(), key -> new ArrayList<>())
                .add(subject.getId()));

        // 4. 确定科目查询范围
        Set<Long> subjectIds;
        if (subjectId == null) {
            subjectIds = subjects.stream().map(FmsSubjectDO::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            if (!subjectMap.containsKey(subjectId)) {
                throw exception(SUBJECT_NOT_EXISTS);
            }
            subjectIds = collectSubjectIds(childMap, subjectId);
        }

        // 5. 加载初始余额和查询期间结束前的凭证分录
        List<FmsInitialBalanceDO> initialBalances = initialBalanceMapper.selectListByAccountSetId(accountSetId);
        List<FmsLedgerEntryVO> entries = ledgerQueryMapper.selectEntryListBeforeTime(
                accountSetId, endMonth.plusMonths(1).atDay(1).atStartOfDay());
        return new AuxiliaryLedgerContext(accountSet, financeParameter, startMonth, endMonth,
                auxiliaryTypeId, subjectId, auxiliaryItems, subjectMap, subjectIds, initialBalances, entries);
    }

    /**
     * 校验账簿查询会计期间
     *
     * @param accountSet 账套
     * @param startMonth 开始会计期间
     * @param endMonth 结束会计期间
     */
    private void validateLedgerPeriod(FmsAccountSetDO accountSet, YearMonth startMonth, YearMonth endMonth) {
        if (startMonth.isAfter(endMonth)) {
            throw exception(LEDGER_PERIOD_INVALID);
        }
        if (startMonth.isBefore(YearMonth.from(accountSet.getStartTime()))) {
            throw exception(LEDGER_PERIOD_BEFORE_ACCOUNT_START);
        }
    }

    /**
     * 构建核算项目明细账行
     *
     * @param context 辅助核算账簿计算上下文
     * @param auxiliaryItemId 辅助核算项目编号
     * @return 核算项目明细账行列表
     */
    private List<FmsLedgerAuxiliaryDetailRespVO> buildAuxiliaryDetailRows(AuxiliaryLedgerContext context, Long auxiliaryItemId) {
        // 1. 筛选核算项目凭证分录并计算期初余额
        List<FmsLedgerEntryVO> entries = filterAuxiliaryEntries(context, auxiliaryItemId);
        LocalDateTime startTime = context.startMonth.atDay(1).atStartOfDay();
        BigDecimal runningSignedAmount = calculateAuxiliaryInitialSignedAmount(context, auxiliaryItemId)
                .add(calculateSignedAmount(entries, null, startTime));

        // 2. 添加期初余额行
        List<FmsLedgerAuxiliaryDetailRespVO> result = new ArrayList<>();
        result.add(buildAuxiliarySummaryRow(context, FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_OPENING,
                context.startMonth, "期初余额", BigDecimal.ZERO, BigDecimal.ZERO, runningSignedAmount));

        // 3. 逐月计算凭证分录、本期合计和本年累计行
        YearMonth currentMonth = context.startMonth;
        while (!currentMonth.isAfter(context.endMonth)) {
            LocalDateTime monthBeginTime = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime nextMonthBeginTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
            List<FmsLedgerEntryVO> monthEntries = filterEntriesByTime(
                    entries, monthBeginTime, nextMonthBeginTime);
            for (FmsLedgerEntryVO entry : monthEntries) {
                runningSignedAmount = runningSignedAmount.add(zeroIfNull(entry.getDebitAmount()))
                        .subtract(zeroIfNull(entry.getCreditAmount()));
                result.add(buildAuxiliaryVoucherRow(context, entry, runningSignedAmount));
            }
            result.add(buildAuxiliarySummaryRow(context,
                    FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_PERIOD_TOTAL, currentMonth, "本期合计",
                    sumDebitAmount(monthEntries), sumCreditAmount(monthEntries), runningSignedAmount));

            LocalDateTime yearBeginTime = LocalDate.of(currentMonth.getYear(), 1, 1).atStartOfDay();
            List<FmsLedgerEntryVO> yearEntries = filterEntriesByTime(
                    entries, yearBeginTime, nextMonthBeginTime);
            result.add(buildAuxiliarySummaryRow(context,
                    FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_YEAR_TOTAL, currentMonth, "本年累计",
                    sumDebitAmount(yearEntries).add(calculateAuxiliaryInitialYearAmount(
                            context, auxiliaryItemId, true, currentMonth.getYear())),
                    sumCreditAmount(yearEntries).add(calculateAuxiliaryInitialYearAmount(
                            context, auxiliaryItemId, false, currentMonth.getYear())),
                    runningSignedAmount));
            currentMonth = currentMonth.plusMonths(1);
        }
        return result;
    }

    /**
     * 构建核算项目凭证分录行
     *
     * @param context 辅助核算账簿计算上下文
     * @param entry 凭证分录
     * @param signedBalance 余额方向金额
     * @return 核算项目凭证分录行
     */
    private FmsLedgerAuxiliaryDetailRespVO buildAuxiliaryVoucherRow(AuxiliaryLedgerContext context,
            FmsLedgerEntryVO entry, BigDecimal signedBalance) {
        FmsSubjectDO subject = context.subjectMap.get(entry.getSubjectId());
        return fillAuxiliaryBalance(context, new FmsLedgerAuxiliaryDetailRespVO()
                .setRowType(FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_VOUCHER)
                .setEntryId(entry.getEntryId()).setSubjectId(entry.getSubjectId())
                .setSubjectCode(subject.getCode()).setSubjectName(subject.getName())
                .setPeriod(YearMonth.from(entry.getVoucherTime()).toString())
                .setAccountDate(entry.getVoucherTime().toLocalDate()).setVoucherId(entry.getVoucherId())
                .setVoucherNumber(entry.getVoucherWordName() + "-" + entry.getVoucherNumber())
                .setDigest(entry.getDigest()).setDebitAmount(zeroIfNull(entry.getDebitAmount()))
                .setCreditAmount(zeroIfNull(entry.getCreditAmount())), signedBalance);
    }

    /**
     * 构建核算项目明细账汇总行
     *
     * @param context 辅助核算账簿计算上下文
     * @param rowType 行类型
     * @param period 会计期间
     * @param digest 摘要
     * @param debitAmount 借方金额
     * @param creditAmount 贷方金额
     * @param signedBalance 余额方向金额
     * @return 核算项目明细账汇总行
     */
    private FmsLedgerAuxiliaryDetailRespVO buildAuxiliarySummaryRow(AuxiliaryLedgerContext context,
            Integer rowType, YearMonth period, String digest, BigDecimal debitAmount,
            BigDecimal creditAmount, BigDecimal signedBalance) {
        return fillAuxiliaryBalance(context, new FmsLedgerAuxiliaryDetailRespVO().setRowType(rowType)
                .setSubjectId(context.selectedSubjectId).setPeriod(period.toString())
                .setAccountDate(period.atEndOfMonth()).setDigest(digest)
                .setDebitAmount(debitAmount).setCreditAmount(creditAmount), signedBalance);
    }

    /**
     * 填充核算项目明细账余额
     *
     * @param context 辅助核算账簿计算上下文
     * @param result 核算项目明细账行
     * @param signedBalance 余额方向金额
     * @return 核算项目明细账行
     */
    private FmsLedgerAuxiliaryDetailRespVO fillAuxiliaryBalance(
            AuxiliaryLedgerContext context, FmsLedgerAuxiliaryDetailRespVO result, BigDecimal signedBalance) {
        // 1. 按科目方向展示时，余额方向固定为科目方向，反向净额保留负数
        FmsSubjectDO subject = context.subjectMap.get(context.selectedSubjectId);
        if (subject != null && ObjUtil.equal(context.financeParameter.getLedgerBalanceMode(),
                FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode())) {
            boolean debitSubject = ObjUtil.equal(subject.getBalanceDirection(),
                    FmsDebitCreditDirectionEnum.DEBIT.getType());
            return result.setBalanceDirection(signedBalance.signum() == 0
                            ? FmsBalanceDirectionEnum.FLAT.getName()
                            : FmsDebitCreditDirectionEnum.valueOf(subject.getBalanceDirection()).getName())
                    .setBalance(debitSubject ? signedBalance : signedBalance.negate());
        }
        // 2. 按实际方向展示；未指定科目时可能聚合不同余额方向的科目
        return result.setBalanceDirection(balanceDirection(signedBalance)).setBalance(signedBalance.abs());
    }

    /**
     * 构建核算项目余额
     *
     * @param context 辅助核算账簿计算上下文
     * @param item 辅助核算项目
     * @return 核算项目余额
     */
    private FmsLedgerAuxiliaryBalanceRespVO buildAuxiliaryBalance(AuxiliaryLedgerContext context, FmsAuxiliaryItemDO item) {
        // 1. 筛选核算项目凭证分录
        List<FmsLedgerEntryVO> entries = filterAuxiliaryEntries(context, item.getId());
        LocalDateTime startTime = context.startMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = context.endMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 2. 计算期初和期末余额
        BigDecimal openingSignedAmount = calculateAuxiliaryInitialSignedAmount(context, item.getId())
                .add(calculateSignedAmount(entries, null, startTime));
        List<FmsLedgerEntryVO> periodEntries = filterEntriesByTime(entries, startTime, endTime);
        BigDecimal endingSignedAmount = openingSignedAmount.add(calculateSignedAmount(periodEntries, null, null));

        // 3. 计算本年累计发生额
        LocalDateTime yearBeginTime = LocalDate.of(context.endMonth.getYear(), 1, 1).atStartOfDay();
        List<FmsLedgerEntryVO> yearEntries = filterEntriesByTime(entries, yearBeginTime, endTime);

        // 4. 组装核算项目余额响应
        return new FmsLedgerAuxiliaryBalanceRespVO().setAuxiliaryItemId(item.getId())
                .setCode(item.getCode()).setName(item.getName())
                .setOpeningDebitAmount(debitPart(openingSignedAmount))
                .setOpeningCreditAmount(creditPart(openingSignedAmount))
                .setPeriodDebitAmount(sumDebitAmount(periodEntries))
                .setPeriodCreditAmount(sumCreditAmount(periodEntries))
                .setYearDebitAmount(sumDebitAmount(yearEntries).add(calculateAuxiliaryInitialYearAmount(
                        context, item.getId(), true, context.endMonth.getYear())))
                .setYearCreditAmount(sumCreditAmount(yearEntries).add(calculateAuxiliaryInitialYearAmount(
                        context, item.getId(), false, context.endMonth.getYear())))
                .setEndingDebitAmount(debitPart(endingSignedAmount))
                .setEndingCreditAmount(creditPart(endingSignedAmount));
    }

    /**
     * 筛选指定辅助核算项目的凭证分录
     *
     * @param context 辅助核算账簿计算上下文
     * @param auxiliaryItemId 辅助核算项目编号
     * @return 凭证分录列表
     */
    private List<FmsLedgerEntryVO> filterAuxiliaryEntries(AuxiliaryLedgerContext context, Long auxiliaryItemId) {
        return filterList(context.entries, entry -> context.subjectIds.contains(entry.getSubjectId())
                && CollUtil.emptyIfNull(entry.getAuxiliaries()).stream()
                        .anyMatch(auxiliary -> ObjUtil.equal(auxiliary.getTypeId(), context.auxiliaryTypeId)
                                && ObjUtil.equal(auxiliary.getItemId(), auxiliaryItemId)));
    }

    /**
     * 筛选指定时间范围的凭证分录
     *
     * @param entries 凭证分录列表
     * @param beginTime 开始时间，为空时不限制
     * @param endTime 结束时间，为空时不限制
     * @return 凭证分录列表
     */
    private List<FmsLedgerEntryVO> filterEntriesByTime(List<FmsLedgerEntryVO> entries,
                                                       LocalDateTime beginTime, LocalDateTime endTime) {
        return filterList(entries, entry -> (beginTime == null || !entry.getVoucherTime().isBefore(beginTime))
                && (endTime == null || entry.getVoucherTime().isBefore(endTime)));
    }

    /**
     * 计算辅助核算项目初始余额方向金额
     *
     * @param context 辅助核算账簿计算上下文
     * @param auxiliaryItemId 辅助核算项目编号
     * @return 初始余额方向金额
     */
    private BigDecimal calculateAuxiliaryInitialSignedAmount(AuxiliaryLedgerContext context, Long auxiliaryItemId) {
        BigDecimal result = BigDecimal.ZERO;
        for (InitialAssistRecord record : filterInitialAssists(context, auxiliaryItemId)) {
            FmsSubjectDO subject = context.subjectMap.get(record.initial.getSubjectId());
            if (subject != null) {
                result = result.add(ObjUtil.equal(subject.getBalanceDirection(),
                        FmsDebitCreditDirectionEnum.DEBIT.getType())
                        ? zeroIfNull(record.assist.getOpeningAmount())
                        : zeroIfNull(record.assist.getOpeningAmount()).negate());
            }
        }
        return result;
    }

    /**
     * 计算辅助核算项目在账套启用年度的年初累计发生额
     *
     * @param context 辅助核算账簿计算上下文
     * @param auxiliaryItemId 辅助核算项目编号
     * @param debit 是否借方
     * @param year 年度
     * @return 年初累计发生额
     */
    private BigDecimal calculateAuxiliaryInitialYearAmount(AuxiliaryLedgerContext context,
            Long auxiliaryItemId, boolean debit, int year) {
        if (context.accountSet.getStartTime().getYear() != year) {
            return BigDecimal.ZERO;
        }
        return sumBigDecimal(filterInitialAssists(context, auxiliaryItemId),
                record -> debit ? record.assist.getYearDebitAmount() : record.assist.getYearCreditAmount());
    }

    /**
     * 筛选辅助核算项目的初始余额记录
     *
     * @param context 辅助核算账簿计算上下文
     * @param auxiliaryItemId 辅助核算项目编号
     * @return 初始余额记录列表
     */
    private List<InitialAssistRecord> filterInitialAssists(AuxiliaryLedgerContext context, Long auxiliaryItemId) {
        return convertListByFlatMap(filterList(context.initialBalances,
                initial -> context.subjectIds.contains(initial.getSubjectId())
                        && CollUtil.isNotEmpty(initial.getAssistBalances())), initial ->
                initial.getAssistBalances().stream()
                        .filter(assist -> CollUtil.isNotEmpty(assist.getAuxiliaries())
                                && assist.getAuxiliaries().stream().anyMatch(auxiliary ->
                                ObjUtil.equal(auxiliary.getTypeId(), context.auxiliaryTypeId)
                                        && ObjUtil.equal(auxiliary.getItemId(), auxiliaryItemId)))
                        .map(assist -> new InitialAssistRecord(initial, assist)));
    }

    /**
     * 校验辅助核算项目存在
     *
     * @param context 辅助核算账簿计算上下文
     * @param auxiliaryItemId 辅助核算项目编号
     */
    private void validateAuxiliaryItem(AuxiliaryLedgerContext context, Long auxiliaryItemId) {
        if (context.auxiliaryItems.stream().noneMatch(item -> ObjUtil.equal(item.getId(), auxiliaryItemId))) {
            throw exception(AUXILIARY_ITEM_NOT_EXISTS);
        }
    }

    /**
     * 收集科目及其全部下级科目编号
     *
     * @param childMap 父科目编号与子科目编号 Map
     * @param subjectId 科目编号
     * @return 科目编号集合
     */
    private Set<Long> collectSubjectIds(Map<Long, List<Long>> childMap, Long subjectId) {
        Set<Long> result = new LinkedHashSet<>();
        result.add(subjectId);
        for (Long childId : childMap.getOrDefault(subjectId, Collections.emptyList())) {
            result.addAll(collectSubjectIds(childMap, childId));
        }
        return result;
    }

    /**
     * 将科目余额树转换为数量金额总账树
     *
     * @param balances 科目余额树
     * @return 数量金额总账树
     */
    private List<FmsLedgerQuantityGeneralRespVO> buildQuantityGeneralRespVOList(List<FmsLedgerSubjectBalanceRespVO> balances) {
        List<FmsLedgerQuantityGeneralRespVO> result = new ArrayList<>(balances.size());
        balances.forEach(balance -> {
            FmsLedgerQuantityGeneralRespVO item = BeanUtils.toBean(balance, FmsLedgerQuantityGeneralRespVO.class)
                    .setOpeningAmount(firstAmount(balance.getOpeningDebitAmount(), balance.getOpeningCreditAmount()))
                    .setEndingAmount(firstAmount(balance.getEndingDebitAmount(), balance.getEndingCreditAmount()))
                    .setChildren(buildQuantityGeneralRespVOList(balance.getChildren()));
            result.add(item);
        });
        return result;
    }

    private static BigDecimal firstAmount(BigDecimal debitAmount, BigDecimal creditAmount) {
        return debitAmount != null ? debitAmount : creditAmount;
    }

    /**
     * 普通账簿计算上下文
     */
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class LedgerContext {

        /**
         * 账套
         */
        private final FmsAccountSetDO accountSet;
        /**
         * 财务参数
         */
        private final FmsFinanceParameterDO financeParameter;
        /**
         * 开始会计期间
         */
        private final YearMonth startMonth;
        /**
         * 结束会计期间
         */
        private final YearMonth endMonth;
        /**
         * 科目列表
         */
        private final List<FmsSubjectDO> subjects;
        /**
         * 科目 Map
         */
        private final Map<Long, FmsSubjectDO> subjectMap;
        /**
         * 父科目编号与子科目编号 Map
         */
        private final Map<Long, List<Long>> childMap;
        /**
         * 科目初始余额列表
         */
        private final List<FmsInitialBalanceDO> initialBalances;
        /**
         * 凭证分录列表
         */
        private final List<FmsLedgerEntryVO> entries;
    }

    /**
     * 辅助核算账簿计算上下文
     */
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class AuxiliaryLedgerContext {

        /**
         * 账套
         */
        private final FmsAccountSetDO accountSet;
        /**
         * 财务参数
         */
        private final FmsFinanceParameterDO financeParameter;
        /**
         * 开始会计期间
         */
        private final YearMonth startMonth;
        /**
         * 结束会计期间
         */
        private final YearMonth endMonth;
        /**
         * 辅助核算类别编号
         */
        private final Long auxiliaryTypeId;
        /**
         * 选中的科目编号
         */
        private final Long selectedSubjectId;
        /**
         * 辅助核算项目列表
         */
        private final List<FmsAuxiliaryItemDO> auxiliaryItems;
        /**
         * 科目 Map
         */
        private final Map<Long, FmsSubjectDO> subjectMap;
        /**
         * 查询范围内的科目编号集合
         */
        private final Set<Long> subjectIds;
        /**
         * 科目初始余额列表
         */
        private final List<FmsInitialBalanceDO> initialBalances;
        /**
         * 凭证分录列表
         */
        private final List<FmsLedgerEntryVO> entries;
    }

    /**
     * 辅助核算组合余额计算记录
     */
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class AuxiliaryCombinationRecord {

        private final Long id;
        private final Long subjectId;
        private final List<AuxiliarySnapshot> items;
        private final List<FmsInitialBalanceDO.AssistBalance> initialAssists;
    }

    /**
     * 辅助核算项目快照
     */
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class AuxiliarySnapshot {

        private final Long itemId;
        private final String name;
    }

    /**
     * 科目初始余额与辅助核算余额记录
     */
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class InitialAssistRecord {

        /**
         * 科目初始余额
         */
        private final FmsInitialBalanceDO initial;
        /**
         * 辅助核算余额
         */
        private final FmsInitialBalanceDO.AssistBalance assist;
    }

}

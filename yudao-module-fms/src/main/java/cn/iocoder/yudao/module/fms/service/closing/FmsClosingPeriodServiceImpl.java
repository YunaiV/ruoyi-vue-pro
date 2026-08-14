package cn.iocoder.yudao.module.fms.service.closing;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingOverviewRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.income.FmsIncomeStatementCheckRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingPeriodDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingPeriodMapper;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherStatusEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.report.FmsBalanceSheetService;
import cn.iocoder.yudao.module.fms.service.report.FmsBalanceSheetServiceImpl;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.count;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.*;

/**
 * FMS 结账期间 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsClosingPeriodServiceImpl implements FmsClosingPeriodService {

    @Resource
    private FmsClosingPeriodMapper closingPeriodMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsClosingSchemeService closingSchemeService;
    @Resource
    private FmsClosingVoucherService closingVoucherService;
    @Resource
    private FmsFinanceParameterService financeParameterService;
    @Resource
    private FmsInitialBalanceService initialBalanceService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsLedgerService ledgerService;
    @Resource
    private FmsVoucherService voucherService;
    @Resource
    private FmsBalanceSheetService balanceSheetService;
    @Resource
    private FmsIncomeStatementService incomeStatementService;

    @Override
    public void validatePeriodOpen(Long accountSetId, LocalDateTime businessTime) {
        if (isPeriodClosed(accountSetId, businessTime)) {
            throw exception(CLOSING_PERIOD_CLOSED);
        }
    }

    @Override
    public boolean isPeriodClosed(Long accountSetId, LocalDateTime businessTime) {
        YearMonth month = YearMonth.from(businessTime);
        LocalDateTime monthBeginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        return closingPeriodMapper.selectByPeriod(accountSetId, monthBeginTime,
                LocalDateTimeUtils.endOfMonth(monthBeginTime)) != null;
    }

    @Override
    public YearMonth getCurrentMonth(Long accountSetId, LocalDateTime startTime) {
        FmsClosingPeriodDO latestPeriod = closingPeriodMapper.selectLatestByAccountSetId(accountSetId);
        return latestPeriod != null ? YearMonth.from(latestPeriod.getClosingTime()).plusMonths(1)
                : YearMonth.from(startTime);
    }

    @Override
    public String getCurrentMonth(Long accountSetId, Long userId) {
        // 1.1 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 校验账套初始化状态
        if (ObjUtil.notEqual(accountSet.getInitialized(), true) || accountSet.getStartTime() == null) {
            throw exception(ACCOUNT_SET_NOT_INITIALIZED);
        }

        // 2. 获得当前会计期间
        return getCurrentMonth(accountSetId, accountSet.getStartTime()).toString();
    }

    @Override
    public FmsClosingOverviewRespVO getClosingOverview(
            FmsClosingQueryReqVO queryReqVO, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(queryReqVO.getAccountSetId(), userId);
        // 1.2 解析会计期间
        YearMonth month = LocalDateTimeUtils.parseYearMonth(queryReqVO.getMonth());
        LocalDateTime beginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        LocalDateTime endTime = LocalDateTimeUtils.endOfMonth(beginTime);

        // 2. 查询结账前检查项
        List<FmsVoucherDO> vouchers = voucherService.getVoucherListByPeriod(
                queryReqVO.getAccountSetId(), beginTime, endTime);
        List<FmsClosingVoucherDO> closingVouchers = closingVoucherService.getClosingVoucherListByPeriod(
                queryReqVO.getAccountSetId(), beginTime, endTime);
        long pendingVoucherCount = getPendingVoucherCount(vouchers, closingVouchers);
        long voucherCount = vouchers.size();
        BigDecimal profitLossBalance = calculateProfitLossBalance(queryReqVO.getAccountSetId(), month, userId);
        BigDecimal balanceSheetDifference = calculateBalanceSheetDifference(
                queryReqVO.getAccountSetId(), month, userId);
        FmsClosingSchemeDO profitLossClosing = closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                queryReqVO.getAccountSetId(), FmsClosingTypeEnum.PROFIT_LOSS.getType());
        FmsClosingVoucherDO profitLossVoucher = profitLossClosing == null ? null
                : CollUtil.findOne(closingVouchers, item ->
                        ObjUtil.equal(item.getClosingId(), profitLossClosing.getId()));

        // 3. 查询其余结账前检查项
        FmsTrialBalanceRespVO trialBalance = initialBalanceService.getTrialBalance(
                queryReqVO.getAccountSetId(), userId);
        boolean voucherNumberContinuous = isVoucherNumberContinuous(vouchers);
        boolean profitLossVoucherGenerated = !hasProfitLossActivity(
                queryReqVO.getAccountSetId(), month, userId) || profitLossVoucher != null;
        FmsReportListReqVO reportReqVO = new FmsReportListReqVO().setAccountSetId(queryReqVO.getAccountSetId())
                .setStartMonth(queryReqVO.getMonth()).setEndMonth(queryReqVO.getMonth());
        FmsIncomeStatementCheckRespVO incomeCheck = incomeStatementService.checkIncomeStatement(reportReqVO, userId);
        FmsBalanceSheetCheckRespVO balanceCheck = balanceSheetService.checkBalanceSheet(reportReqVO, userId);

        // 4. 组装结账概况
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(
                queryReqVO.getAccountSetId(), userId);
        boolean voucherReviewRequired = Boolean.TRUE.equals(financeParameter.getVoucherReviewRequired());
        boolean initialBalanceBalanced = trialBalance != null && Boolean.TRUE.equals(trialBalance.getBalanced());
        boolean incomeStatementBalanced = incomeCheck != null && Boolean.TRUE.equals(incomeCheck.getBalanced());
        int incomeUnmappedCount = incomeCheck == null || incomeCheck.getUnmappedSubjects() == null
                ? 0 : incomeCheck.getUnmappedSubjects().size();
        boolean balanceSheetProfitLossTransferred = balanceCheck != null
                && Boolean.TRUE.equals(balanceCheck.getProfitLossTransferred());
        boolean balanceSheetBalanced = balanceCheck != null && Boolean.TRUE.equals(balanceCheck.getBalanced());
        int balanceUnmappedCount = balanceCheck == null || balanceCheck.getUnmappedSubjects() == null
                ? 0 : balanceCheck.getUnmappedSubjects().size();
        boolean closed = closingPeriodMapper.selectByPeriod(
                queryReqVO.getAccountSetId(), beginTime, endTime) != null;
        boolean canClose = !closed
                && (!voucherReviewRequired || pendingVoucherCount == 0)
                && initialBalanceBalanced && voucherNumberContinuous && profitLossVoucherGenerated
                && profitLossBalance.compareTo(BigDecimal.ZERO) == 0
                && incomeStatementBalanced && incomeUnmappedCount == 0
                && balanceSheetProfitLossTransferred && balanceSheetBalanced && balanceUnmappedCount == 0;
        return new FmsClosingOverviewRespVO().setMonth(queryReqVO.getMonth())
                .setClosed(closed)
                .setVoucherReviewRequired(voucherReviewRequired)
                .setPendingVoucherCount(pendingVoucherCount)
                .setVoucherCount(voucherCount)
                .setProfitLossBalance(profitLossBalance)
                .setBalanceSheetDifference(balanceSheetDifference)
                .setProfitLossVoucherId(profitLossVoucher == null ? null : profitLossVoucher.getVoucherId())
                .setInitialBalanceBalanced(initialBalanceBalanced)
                .setVoucherNumberContinuous(voucherNumberContinuous)
                .setProfitLossVoucherGenerated(profitLossVoucherGenerated)
                .setIncomeStatementBalanced(incomeStatementBalanced)
                .setIncomeStatementUnmappedSubjectCount(incomeUnmappedCount)
                .setBalanceSheetProfitLossTransferred(balanceSheetProfitLossTransferred)
                .setBalanceSheetBalanced(balanceSheetBalanced)
                .setBalanceSheetUnmappedSubjectCount(balanceUnmappedCount)
                .setCanClose(canClose);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_PERIOD_CLOSE_SUB_TYPE,
            bizNo = "{{#queryReqVO.accountSetId}}", success = FMS_CLOSING_PERIOD_CLOSE_SUCCESS)
    public void closePeriod(FmsClosingQueryReqVO queryReqVO, Long userId) {
        // 1. 校验账套权限、初始化状态和结账目标期间
        YearMonth currentMonth = validateCloseTargetPeriod(queryReqVO.getAccountSetId(), queryReqVO.getMonth(), userId);
        YearMonth targetMonth = LocalDateTimeUtils.parseYearMonth(queryReqVO.getMonth());

        // 2. 逐月执行结账，保证目标期间之前的每个期间都已完成完整检查
        for (YearMonth month = currentMonth; !month.isAfter(targetMonth); month = month.plusMonths(1)) {
            closeSinglePeriod(queryReqVO.getAccountSetId(), month, userId);
        }
    }

    /**
     * 执行单个会计期间的结账检查和状态登记
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     */
    private void closeSinglePeriod(Long accountSetId, YearMonth month, Long userId) {
        // 1. 查询结账前概况
        FmsClosingQueryReqVO queryReqVO = new FmsClosingQueryReqVO()
                .setAccountSetId(accountSetId).setMonth(month.toString());
        // 1.2 校验结账概况
        FmsClosingOverviewRespVO overview = getClosingOverview(queryReqVO, userId);
        if (overview.getClosed()) {
            throw exception(CLOSING_PERIOD_CLOSED);
        }
        if (overview.getVoucherReviewRequired() && overview.getPendingVoucherCount() > 0) {
            throw exception(CLOSING_VOUCHER_PENDING_REVIEW);
        }

        // 2.1 校验初始余额试算平衡
        if (!Boolean.TRUE.equals(overview.getInitialBalanceBalanced())) {
            throw exception(CLOSING_INITIAL_BALANCE_UNBALANCED);
        }
        // 2.2 校验凭证号连续
        if (!Boolean.TRUE.equals(overview.getVoucherNumberContinuous())) {
            throw exception(CLOSING_VOUCHER_NUMBER_DISCONTINUOUS);
        }

        // 3.1 校验损益结转凭证
        if (!Boolean.TRUE.equals(overview.getProfitLossVoucherGenerated())) {
            throw exception(CLOSING_PROFIT_LOSS_VOUCHER_MISSING);
        }
        if (overview.getProfitLossBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw exception(CLOSING_PROFIT_LOSS_NOT_TRANSFERRED);
        }
        // 3.2 校验利润表勾稽和公式覆盖
        if (!Boolean.TRUE.equals(overview.getIncomeStatementBalanced())) {
            throw exception(CLOSING_INCOME_STATEMENT_UNBALANCED);
        }
        if (overview.getIncomeStatementUnmappedSubjectCount() > 0) {
            throw exception(CLOSING_REPORT_SUBJECT_UNMAPPED);
        }
        // 3.3 校验资产负债表平衡和公式覆盖
        if (!Boolean.TRUE.equals(overview.getBalanceSheetProfitLossTransferred())) {
            throw exception(CLOSING_PROFIT_LOSS_NOT_TRANSFERRED);
        }
        if (!Boolean.TRUE.equals(overview.getBalanceSheetBalanced())) {
            throw exception(CLOSING_BALANCE_SHEET_UNBALANCED);
        }
        if (overview.getBalanceSheetUnmappedSubjectCount() > 0) {
            throw exception(CLOSING_REPORT_SUBJECT_UNMAPPED);
        }

        // 4. 登记结账期间
        LocalDateTime beginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        LocalDateTime endTime = LocalDateTimeUtils.endOfMonth(beginTime);
        closingPeriodMapper.insert(new FmsClosingPeriodDO().setClosingTime(month.atEndOfMonth().atStartOfDay())
                .setAccountSetId(accountSetId));

        // 5. 更新当期结转凭证为已结账
        closingVoucherService.updateClosingVoucherClosedByPeriod(
                accountSetId, beginTime, endTime, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_PERIOD_CANCEL_SUB_TYPE,
            bizNo = "{{#queryReqVO.accountSetId}}", success = FMS_CLOSING_PERIOD_CANCEL_SUCCESS)
    public void cancelClosePeriod(FmsClosingQueryReqVO queryReqVO, Long userId) {
        // 1.1 校验账套写权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetWritePermission(
                queryReqVO.getAccountSetId(), userId);
        // 1.2 串行化同一账套的结账状态变更
        accountSetService.lockAccountSet(queryReqVO.getAccountSetId());

        // 2. 从最近期间逐月反结账到目标期间
        FmsClosingPeriodDO latest = closingPeriodMapper.selectLatestByAccountSetId(queryReqVO.getAccountSetId());
        YearMonth targetMonth = LocalDateTimeUtils.parseYearMonth(queryReqVO.getMonth());
        YearMonth startMonth = accountSet.getStartTime() == null ? null : YearMonth.from(accountSet.getStartTime());
        if (latest == null || targetMonth.isAfter(YearMonth.from(latest.getClosingTime()))
                || (startMonth != null && targetMonth.isBefore(startMonth))) {
            throw exception(CLOSING_PERIOD_NOT_CLOSED);
        }
        for (YearMonth month = YearMonth.from(latest.getClosingTime()); !month.isBefore(targetMonth);
             month = month.minusMonths(1)) {
            cancelSinglePeriod(queryReqVO.getAccountSetId(), month);
        }
    }

    /**
     * 撤销单个会计期间的结账状态
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     */
    private void cancelSinglePeriod(Long accountSetId, YearMonth month) {
        LocalDateTime beginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        LocalDateTime endTime = LocalDateTimeUtils.endOfMonth(beginTime);
        FmsClosingPeriodDO period = closingPeriodMapper.selectByPeriod(accountSetId, beginTime, endTime);
        if (period == null) {
            throw exception(CLOSING_PERIOD_NOT_CLOSED);
        }
        closingPeriodMapper.deleteById(period.getId());
        closingVoucherService.updateClosingVoucherClosedByPeriod(accountSetId, beginTime, endTime, false);
    }

    @Override
    public boolean isPeriodClosed(Long accountSetId, String monthValue) {
        YearMonth month = LocalDateTimeUtils.parseYearMonth(monthValue);
        LocalDateTime beginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        FmsClosingPeriodDO period = closingPeriodMapper.selectByPeriod(accountSetId, beginTime, LocalDateTimeUtils.endOfMonth(beginTime));
        return period != null;
    }

    /**
     * 获得待审核凭证数量
     *
     * 结转凭证由结账流程生成且不提供人工审核入口，不计入结账前的待审核凭证数量
     *
     * @param vouchers 会计期间凭证列表
     * @param closingVouchers 会计期间结转凭证列表
     * @return 待审核凭证数量
     */
    private long getPendingVoucherCount(List<FmsVoucherDO> vouchers,
            List<FmsClosingVoucherDO> closingVouchers) {
        Set<Long> closingVoucherIds = convertSet(closingVouchers, FmsClosingVoucherDO::getVoucherId);
        return count(vouchers, voucher ->
                ObjUtil.equal(voucher.getStatus(), FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                        && !closingVoucherIds.contains(voucher.getId()));
    }

    /**
     * 计算末级损益科目的本期借贷差额绝对值合计
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     * @return 损益余额
     */
    private BigDecimal calculateProfitLossBalance(Long accountSetId, YearMonth month, Long userId) {
        // 1. 查询科目和当期科目余额
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = getSubjectBalanceMap(accountSetId, month, userId);

        // 2. 汇总末级损益科目的本期借贷差额
        BigDecimal result = BigDecimal.ZERO;
        for (FmsSubjectDO subject : subjects) {
            if (ObjUtil.notEqual(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                    || parentIds.contains(subject.getId())) {
                continue;
            }
            FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(subject.getId());
            if (balance != null) {
                result = result.add(NumberUtils.zeroIfNull(balance.getPeriodDebitAmount())
                        .subtract(NumberUtils.zeroIfNull(balance.getPeriodCreditAmount())).abs());
            }
        }
        return result;
    }

    /**
     * 判断当期损益类末级科目是否存在发生额
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     * @return 是否存在发生额
     */
    private boolean hasProfitLossActivity(Long accountSetId, YearMonth month, Long userId) {
        // 1. 查询末级损益科目和当期余额
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = getSubjectBalanceMap(accountSetId, month, userId);

        // 2. 任一末级损益科目存在借方或贷方发生额即视为当期有损益业务
        for (FmsSubjectDO subject : subjects) {
            if (ObjUtil.notEqual(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                    || parentIds.contains(subject.getId())) {
                continue;
            }
            FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(subject.getId());
            if (balance != null && (NumberUtils.zeroIfNull(balance.getPeriodDebitAmount()).signum() != 0
                    || NumberUtils.zeroIfNull(balance.getPeriodCreditAmount()).signum() != 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验每个凭证字的当期凭证号从 1 开始连续编号
     *
     * @param vouchers 当期凭证列表
     * @return 是否连续编号
     */
    private boolean isVoucherNumberContinuous(List<FmsVoucherDO> vouchers) {
        // 1. 按凭证字分组
        Map<Long, List<FmsVoucherDO>> voucherMap = vouchers.stream()
                .collect(Collectors.groupingBy(FmsVoucherDO::getVoucherWordId, LinkedHashMap::new,
                        Collectors.toList()));

        // 2. 校验各凭证字的号码从 1 开始连续递增
        for (List<FmsVoucherDO> wordVouchers : voucherMap.values()) {
            for (int index = 0; index < wordVouchers.size(); index++) {
                if (ObjUtil.notEqual(wordVouchers.get(index).getVoucherNumber(), index + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private BigDecimal calculateBalanceSheetDifference(Long accountSetId, YearMonth month, Long userId) {
        FmsReportListReqVO listReqVO = new FmsReportListReqVO().setAccountSetId(accountSetId)
                .setStartMonth(month.toString()).setEndMonth(month.toString());
        BigDecimal assetAmount = BigDecimal.ZERO;
        BigDecimal liabilityAmount = BigDecimal.ZERO;
        for (FmsBalanceSheetRowRespVO row : balanceSheetService.getBalanceSheet(listReqVO, userId)) {
            if (ObjUtil.equal(row.getAssetRowNo(), FmsBalanceSheetServiceImpl.ASSET_TOTAL_ROW_NO)) {
                assetAmount = NumberUtils.zeroIfNull(row.getAssetClosingAmount());
            }
            if (ObjUtil.equal(row.getLiabilityRowNo(), FmsBalanceSheetServiceImpl.LIABILITY_TOTAL_ROW_NO)) {
                liabilityAmount = NumberUtils.zeroIfNull(row.getLiabilityClosingAmount());
            }
        }
        return assetAmount.subtract(liabilityAmount).abs();
    }

    /**
     * 获得单个会计期间的科目余额 Map
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     * @return 科目编号到余额的 Map
     */
    private Map<Long, FmsLedgerSubjectBalanceRespVO> getSubjectBalanceMap(Long accountSetId, YearMonth month, Long userId) {
        return getSubjectBalanceMap(accountSetId, month, month, userId);
    }

    /**
     * 获得月份区间的科目余额 Map，树形余额扁平化后按科目编号存放
     *
     * @param accountSetId 账套编号
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @param userId 用户编号
     * @return 科目编号到余额的 Map
     */
    private Map<Long, FmsLedgerSubjectBalanceRespVO> getSubjectBalanceMap(Long accountSetId, YearMonth startMonth, YearMonth endMonth, Long userId) {
        FmsLedgerListReqVO listReqVO = new FmsLedgerListReqVO().setAccountSetId(accountSetId)
                .setStartMonth(startMonth.toString()).setEndMonth(endMonth.toString());
        Map<Long, FmsLedgerSubjectBalanceRespVO> result = new LinkedHashMap<>();
        flattenBalances(ledgerService.getSubjectBalanceList(listReqVO, userId), result);
        return result;
    }

    /**
     * 递归扁平化树形科目余额到 Map
     *
     * @param balances 树形科目余额
     * @param result 科目编号到余额的 Map
     */
    private void flattenBalances(List<FmsLedgerSubjectBalanceRespVO> balances,
            Map<Long, FmsLedgerSubjectBalanceRespVO> result) {
        for (FmsLedgerSubjectBalanceRespVO balance : balances) {
            result.put(balance.getSubjectId(), balance);
            flattenBalances(balance.getChildren(), result);
        }
    }

    /**
     * 校验账套写权限、初始化状态和结账目标期间
     *
     * @param accountSetId 账套编号
     * @param month 结账目标期间
     * @param userId 用户编号
     * @return 当前会计期间
     */
    private YearMonth validateCloseTargetPeriod(Long accountSetId, String month, Long userId) {
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        accountSetService.lockAccountSet(accountSetId);
        if (ObjUtil.notEqual(accountSet.getInitialized(), true) || accountSet.getStartTime() == null) {
            throw exception(ACCOUNT_SET_NOT_INITIALIZED);
        }
        YearMonth currentMonth = getCurrentMonth(accountSetId, accountSet.getStartTime());
        if (LocalDateTimeUtils.parseYearMonth(month).isBefore(currentMonth)) {
            throw exception(CLOSING_NOT_CURRENT_PERIOD);
        }
        return currentMonth;
    }

}

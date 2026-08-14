package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSubjectAmountVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportPeriodTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.LEDGER_PERIOD_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EDITABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_FORMULA_OPERATOR_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_FORMULA_RULE_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_FORMULA_SUBJECT_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.SUBJECT_NOT_EXISTS;

/**
 * FMS 报表共用 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsReportCommonServiceImpl implements FmsReportCommonService {

    private static final DateTimeFormatter MONTH_FORMATTER = DatePattern.NORM_MONTH_FORMATTER;
    private static final Pattern LINE_FORMULA_PATTERN = Pattern.compile("([+-]?)(L\\d+)");

    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsLedgerService ledgerService;
    @Resource
    @Lazy // 避免循环依赖
    private FmsClosingSchemeService closingSchemeService;
    @Resource
    @Lazy // 避免循环依赖
    private FmsClosingVoucherService closingVoucherService;
    @Resource
    @Lazy // 避免循环依赖
    private FmsVoucherService voucherService;

    @Override
    public Map<Long, FmsLedgerSubjectBalanceRespVO> getSubjectBalanceMap(Long accountSetId,
            YearMonth startMonth, YearMonth endMonth, Long userId) {
        // 1. 查询普通账簿科目余额
        FmsLedgerListReqVO listReqVO = new FmsLedgerListReqVO().setAccountSetId(accountSetId)
                .setStartMonth(startMonth.format(MONTH_FORMATTER))
                .setEndMonth(endMonth.format(MONTH_FORMATTER));
        List<FmsLedgerSubjectBalanceRespVO> balances = flattenBalances(ledgerService.getSubjectBalanceList(listReqVO, userId));
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = convertMap(
                balances, FmsLedgerSubjectBalanceRespVO::getSubjectId, Function.identity(),
                (first, second) -> first, LinkedHashMap::new);

        // 2. 查询结转损益凭证在期初、本期和本年的科目发生额
        LocalDateTime startTime = LocalDateTimeUtils.getMonthBeginTime(startMonth);
        LocalDateTime endTime = LocalDateTimeUtils.getNextMonthBeginTime(endMonth);
        LocalDateTime yearBeginTime = LocalDateTimeUtils.getMonthBeginTime(endMonth.getYear(), 1);
        FmsClosingSchemeDO profitLossClosing = closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                accountSetId, FmsClosingTypeEnum.PROFIT_LOSS.getType());
        if (profitLossClosing == null) {
            return balanceMap;
        }
        List<FmsClosingVoucherDO> closingVouchers = closingVoucherService
                .getClosingVoucherListByClosingIdAndPeriod(profitLossClosing.getId(), null, endTime);
        List<FmsVoucherSubjectAmountVO> openingAmounts = getProfitLossClosingSubjectAmountList(
                closingVouchers, null, startTime);
        List<FmsVoucherSubjectAmountVO> periodAmounts = getProfitLossClosingSubjectAmountList(
                closingVouchers, startTime, endTime);
        List<FmsVoucherSubjectAmountVO> yearAmounts = getProfitLossClosingSubjectAmountList(
                closingVouchers, yearBeginTime, endTime);

        // 3. 从损益类科目及其父级余额中扣除结转损益凭证发生额
        Map<Long, FmsSubjectDO> subjectMap = convertMap(
                subjectService.getSubjectList(accountSetId, null, userId), FmsSubjectDO::getId);
        adjustProfitLossClosingAmounts(balanceMap, subjectMap, openingAmounts, periodAmounts, yearAmounts);
        return balanceMap;
    }

    /**
     * 获得指定期间内损益结转凭证的科目发生额
     *
     * @param closingVouchers 损益结转凭证关联列表
     * @param beginTime 开始时间，为空时不限制
     * @param endTime 结束时间
     * @return 科目发生额列表
     */
    private List<FmsVoucherSubjectAmountVO> getProfitLossClosingSubjectAmountList(
            List<FmsClosingVoucherDO> closingVouchers, LocalDateTime beginTime, LocalDateTime endTime) {
        // 1. 按期间筛选损益结转凭证编号
        List<FmsClosingVoucherDO> periodClosingVouchers = filterList(closingVouchers,
                item -> (beginTime == null || !item.getVoucherTime().isBefore(beginTime))
                        && item.getVoucherTime().isBefore(endTime));
        // 2. 按关联凭证编号聚合科目发生额
        return voucherService.getVoucherSubjectAmountList(
                convertList(periodClosingVouchers, FmsClosingVoucherDO::getVoucherId));
    }

    /**
     * 从报表余额中扣除结转损益凭证发生额
     *
     * 只处理损益类科目，并同步调整其父级汇总；普通结转凭证和非损益结转科目保持原余额
     *
     * @param balanceMap 科目余额 Map
     * @param subjectMap 科目 Map
     * @param openingAmounts 期初前发生额
     * @param periodAmounts 本期发生额
     * @param yearAmounts 本年发生额
     */
    private void adjustProfitLossClosingAmounts(Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap, Map<Long, FmsSubjectDO> subjectMap,
                                                List<FmsVoucherSubjectAmountVO> openingAmounts, List<FmsVoucherSubjectAmountVO> periodAmounts,
                                                List<FmsVoucherSubjectAmountVO> yearAmounts) {
        // 1. 按科目编号建立各期间结转损益发生额索引
        Map<Long, FmsVoucherSubjectAmountVO> openingAmountMap = convertMap(openingAmounts, FmsVoucherSubjectAmountVO::getSubjectId);
        Map<Long, FmsVoucherSubjectAmountVO> periodAmountMap = convertMap(periodAmounts, FmsVoucherSubjectAmountVO::getSubjectId);
        Map<Long, FmsVoucherSubjectAmountVO> yearAmountMap = convertMap(yearAmounts, FmsVoucherSubjectAmountVO::getSubjectId);
        // 2. 从损益科目及其父级汇总余额中逐级扣除结转发生额
        for (FmsSubjectDO subject : subjectMap.values()) {
            if (ObjUtil.notEqual(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())) {
                continue;
            }
            FmsVoucherSubjectAmountVO openingAmount = openingAmountMap.get(subject.getId());
            FmsVoucherSubjectAmountVO periodAmount = periodAmountMap.get(subject.getId());
            FmsVoucherSubjectAmountVO yearAmount = yearAmountMap.get(subject.getId());
            if (openingAmount == null && periodAmount == null && yearAmount == null) {
                continue;
            }
            FmsSubjectDO currentSubject = subject;
            for (int level = 0; currentSubject != null && level < subjectMap.size(); level++) {
                adjustSubjectBalance(balanceMap.get(currentSubject.getId()), openingAmount, periodAmount, yearAmount);
                currentSubject = subjectMap.get(currentSubject.getParentId());
            }
        }
    }

    /**
     * 扣除单个损益科目发生额
     *
     * @param balance 科目余额
     * @param openingAmount 期初前发生额
     * @param periodAmount 本期发生额
     * @param yearAmount 本年发生额
     */
    private void adjustSubjectBalance(FmsLedgerSubjectBalanceRespVO balance,
                                      FmsVoucherSubjectAmountVO openingAmount, FmsVoucherSubjectAmountVO periodAmount,
                                      FmsVoucherSubjectAmountVO yearAmount) {
        // 1. 账簿中不存在的科目无需调整
        if (balance == null) {
            return;
        }
        // 2. 扣除结转损益凭证，重新计算期初和期末净余额
        BigDecimal openingDebitAmount = getDebitAmount(openingAmount);
        BigDecimal openingCreditAmount = getCreditAmount(openingAmount);
        BigDecimal periodDebitAmount = getDebitAmount(periodAmount);
        BigDecimal periodCreditAmount = getCreditAmount(periodAmount);
        BigDecimal openingBalance = NumberUtils.zeroIfNull(balance.getOpeningDebitAmount())
                .subtract(NumberUtils.zeroIfNull(balance.getOpeningCreditAmount()))
                .subtract(openingDebitAmount).add(openingCreditAmount);
        BigDecimal endingBalance = NumberUtils.zeroIfNull(balance.getEndingDebitAmount())
                .subtract(NumberUtils.zeroIfNull(balance.getEndingCreditAmount()))
                .subtract(openingDebitAmount).add(openingCreditAmount)
                .subtract(periodDebitAmount).add(periodCreditAmount);
        // 3. 回写期初、本期、本年和期末借贷金额
        balance.setOpeningDebitAmount(openingBalance.max(BigDecimal.ZERO))
                .setOpeningCreditAmount(openingBalance.min(BigDecimal.ZERO).abs())
                .setPeriodDebitAmount(NumberUtils.zeroIfNull(balance.getPeriodDebitAmount()).subtract(periodDebitAmount))
                .setPeriodCreditAmount(NumberUtils.zeroIfNull(balance.getPeriodCreditAmount()).subtract(periodCreditAmount))
                .setYearDebitAmount(NumberUtils.zeroIfNull(balance.getYearDebitAmount()).subtract(getDebitAmount(yearAmount)))
                .setYearCreditAmount(NumberUtils.zeroIfNull(balance.getYearCreditAmount()).subtract(getCreditAmount(yearAmount)))
                .setEndingDebitAmount(endingBalance.max(BigDecimal.ZERO))
                .setEndingCreditAmount(endingBalance.min(BigDecimal.ZERO).abs());
    }

    private BigDecimal getDebitAmount(FmsVoucherSubjectAmountVO amount) {
        return amount == null ? BigDecimal.ZERO : NumberUtils.zeroIfNull(amount.getDebitAmount());
    }

    private BigDecimal getCreditAmount(FmsVoucherSubjectAmountVO amount) {
        return amount == null ? BigDecimal.ZERO : NumberUtils.zeroIfNull(amount.getCreditAmount());
    }

    @Override
    public List<FmsReportFormulaRespVO> buildReportFormulaList(FmsReportFormulaUpdateReqVO updateReqVO,
            Integer minimumRule, Integer maximumRule, Long userId) {
        // 1. 校验科目不重复
        List<FmsReportFormulaUpdateReqVO.Formula> formulaReqVOs = updateReqVO.getFormulas();
        List<Long> subjectIds = convertList(formulaReqVOs, FmsReportFormulaUpdateReqVO.Formula::getSubjectId);
        if (new LinkedHashSet<>(subjectIds).size() != subjectIds.size()) {
            throw exception(REPORT_FORMULA_SUBJECT_DUPLICATE);
        }
        Map<Long, FmsSubjectDO> subjectMap = convertMap(
                subjectService.getSubjectList(updateReqVO.getAccountSetId(), null, userId), FmsSubjectDO::getId);
        // 2. 校验运算符、取数规则和科目存在性，构造公式项
        List<FmsReportFormulaRespVO> formulas = new ArrayList<>();
        for (FmsReportFormulaUpdateReqVO.Formula formulaReqVO : formulaReqVOs) {
            if (!StrUtil.equalsAny(formulaReqVO.getOperator(), "+", "-")) {
                throw exception(REPORT_FORMULA_OPERATOR_INVALID);
            }
            if (formulaReqVO.getRules() == null || formulaReqVO.getRules() < minimumRule
                    || formulaReqVO.getRules() > maximumRule) {
                throw exception(REPORT_FORMULA_RULE_INVALID);
            }
            FmsSubjectDO subject = subjectMap.get(formulaReqVO.getSubjectId());
            if (subject == null) {
                throw exception(SUBJECT_NOT_EXISTS);
            }
            formulas.add(new FmsReportFormulaRespVO().setSubjectId(subject.getId())
                    .setSubjectName(subject.getName()).setSubjectNumber(subject.getCode())
                    .setOperator(formulaReqVO.getOperator()).setRules(formulaReqVO.getRules()));
        }
        return formulas;
    }

    @Override
    public void validateReportItemEditable(Long accountSetId, Long itemAccountSetId, Boolean editable) {
        if (itemAccountSetId == null || ObjUtil.notEqual(accountSetId, itemAccountSetId)) {
            throw exception(REPORT_CONFIG_NOT_EXISTS);
        }
        if (Boolean.FALSE.equals(editable)) {
            throw exception(REPORT_CONFIG_NOT_EDITABLE);
        }
    }

    @Override
    public String bindSubjectFormula(String formula, Map<String, FmsSubjectDO> subjectMap) {
        // 空公式和行次公式不含科目编码，直接返回原公式
        if (StrUtil.isEmpty(formula) || isLineFormula(formula)) {
            return formula;
        }
        // 按科目编码绑定账套下的科目编号和名称，编码不存在时科目编号置空，由前端提示科目已失效
        List<FmsReportFormulaRespVO> formulas = parseSubjectFormula(formula);
        formulas.forEach(item -> {
            FmsSubjectDO subject = subjectMap.get(item.getSubjectNumber());
            if (subject != null) {
                item.setSubjectId(subject.getId()).setSubjectName(subject.getName()).setSubjectNumber(subject.getCode());
            } else {
                item.setSubjectId(null);
            }
        });
        return JsonUtils.toJsonString(formulas);
    }

    @Override
    public List<FmsReportFormulaRespVO> parseSubjectFormula(String formula) {
        if (StrUtil.isEmpty(formula)) {
            return new ArrayList<>();
        }
        return JsonUtils.parseArray(formula, FmsReportFormulaRespVO.class);
    }

    @Override
    public Set<Long> parseFormulaSubjectIds(String formula) {
        Set<Long> subjectIds = new HashSet<>();
        if (StrUtil.isEmpty(formula) || !formula.contains("{")) {
            return subjectIds;
        }
        for (FmsReportFormulaRespVO formulaItem : parseSubjectFormula(formula)) {
            if (formulaItem.getSubjectId() != null) {
                subjectIds.add(formulaItem.getSubjectId());
            }
        }
        return subjectIds;
    }

    @Override
    public boolean isSubjectTreeMapped(FmsSubjectDO rootSubject, List<FmsSubjectDO> subjects, Set<Long> formulaSubjectIds) {
        // 一级科目自身已纳入公式时直接命中，否则沿公式科目的父级链向上查找
        if (formulaSubjectIds.contains(rootSubject.getId())) {
            return true;
        }
        // 逐个公式科目沿父级链向上查找，任一祖先为当前一级科目即视为已纳入
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        for (Long subjectId : formulaSubjectIds) {
            FmsSubjectDO currentSubject = subjectMap.get(subjectId);
            while (currentSubject != null && currentSubject.getParentId() != null
                    && currentSubject.getParentId() > 0) {
                if (ObjUtil.equal(currentSubject.getParentId(), rootSubject.getId())) {
                    return true;
                }
                currentSubject = subjectMap.get(currentSubject.getParentId());
            }
        }
        return false;
    }

    @Override
    public boolean isRootSubject(FmsSubjectDO subject) {
        return subject.getParentId() == null || subject.getParentId() == 0;
    }

    @Override
    public boolean isLineFormula(String formula) {
        return StrUtil.isNotEmpty(formula) && formula.contains("L");
    }

    @Override
    public BigDecimal applyOperator(BigDecimal result, BigDecimal amount, String operator) {
        return StrUtil.equals(operator, "-") ? result.subtract(amount) : result.add(amount);
    }

    @Override
    public void normalizeSubjectFormula(FmsReportFormulaRespVO formula, FmsSubjectDO subject) {
        if (subject != null) {
            formula.setSubjectName(subject.getName()).setSubjectNumber(subject.getCode());
        }
    }

    @Override
    public BigDecimal calculateBalanceAmount(Integer rule, FmsSubjectDO subject,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap,
            Map<Long, FmsSubjectDO> subjectMap, boolean opening) {
        // 科目缺失时按零处理
        if (subject == null) {
            return BigDecimal.ZERO;
        }
        // 科目借/贷方余额按一级子科目分别判断后求和，避免父科目先净额抵销
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.SUBJECT_DEBIT_BALANCE.getRule())
                || ObjUtil.equal(rule, FmsFormulaRuleEnum.SUBJECT_CREDIT_BALANCE.getRule())) {
            List<Long> childSubjectIds = convertList(filterList(subjectMap.values(),
                    item -> ObjUtil.equal(item.getParentId(), subject.getId())), FmsSubjectDO::getId);
            if (CollUtil.isEmpty(childSubjectIds)) {
                childSubjectIds = Collections.singletonList(subject.getId());
            }
            boolean debitRule = ObjUtil.equal(rule, FmsFormulaRuleEnum.SUBJECT_DEBIT_BALANCE.getRule());
            BigDecimal amount = BigDecimal.ZERO;
            for (Long childSubjectId : childSubjectIds) {
                FmsLedgerSubjectBalanceRespVO childBalance = balanceMap.get(childSubjectId);
                if (childBalance == null) {
                    continue;
                }
                BigDecimal childAmount = opening
                        ? (debitRule ? childBalance.getOpeningDebitAmount() : childBalance.getOpeningCreditAmount())
                        : (debitRule ? childBalance.getEndingDebitAmount() : childBalance.getEndingCreditAmount());
                amount = amount.add(NumberUtils.zeroIfNull(childAmount));
            }
            return amount;
        }
        FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(subject.getId());
        if (balance == null) {
            return BigDecimal.ZERO;
        }
        // 余额规则按科目余额方向决定借贷相减方向，借方、贷方余额规则直接取对应方向金额
        BigDecimal debitAmount = opening ? NumberUtils.zeroIfNull(balance.getOpeningDebitAmount())
                : NumberUtils.zeroIfNull(balance.getEndingDebitAmount());
        BigDecimal creditAmount = opening ? NumberUtils.zeroIfNull(balance.getOpeningCreditAmount())
                : NumberUtils.zeroIfNull(balance.getEndingCreditAmount());
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.BALANCE.getRule())) {
            return ObjUtil.equal(subject.getBalanceDirection(), 1)
                    ? debitAmount.subtract(creditAmount) : creditAmount.subtract(debitAmount);
        }
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())) {
            return debitAmount;
        }
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.CREDIT_BALANCE.getRule())) {
            return creditAmount;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateOccurrenceAmount(Integer rule, FmsSubjectDO subject,
            FmsLedgerSubjectBalanceRespVO balance, boolean current) {
        // 余额或科目缺失时按零处理；按取数规则选择本期或本年累计的借贷方向金额
        if (balance == null || subject == null) {
            return BigDecimal.ZERO;
        }
        // 损益发生额按科目余额方向决定借贷相减方向，借方、贷方发生额规则直接取对应方向金额
        BigDecimal debitAmount = current ? NumberUtils.zeroIfNull(balance.getPeriodDebitAmount())
                : NumberUtils.zeroIfNull(balance.getYearDebitAmount());
        BigDecimal creditAmount = current ? NumberUtils.zeroIfNull(balance.getPeriodCreditAmount())
                : NumberUtils.zeroIfNull(balance.getYearCreditAmount());
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.DEBIT_AMOUNT.getRule())) {
            return debitAmount;
        }
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.CREDIT_AMOUNT.getRule())) {
            return creditAmount;
        }
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.PROFIT_LOSS_AMOUNT.getRule())) {
            return ObjUtil.equal(subject.getBalanceDirection(), 1)
                    ? debitAmount.subtract(creditAmount) : creditAmount.subtract(debitAmount);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateIncomeOccurrenceAmount(Integer rule, FmsSubjectDO subject,
            FmsLedgerSubjectBalanceRespVO balance, boolean current, String operator) {
        BigDecimal amount = calculateOccurrenceAmount(rule, subject, balance, current);
        if (subject == null || balance == null || !StrUtil.equals(operator, "+")) {
            return amount;
        }
        BigDecimal debitAmount = current ? NumberUtils.zeroIfNull(balance.getPeriodDebitAmount())
                : NumberUtils.zeroIfNull(balance.getYearDebitAmount());
        BigDecimal creditAmount = current ? NumberUtils.zeroIfNull(balance.getPeriodCreditAmount())
                : NumberUtils.zeroIfNull(balance.getYearCreditAmount());
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.DEBIT_AMOUNT.getRule())) {
            return debitAmount.subtract(creditAmount);
        }
        if (ObjUtil.equal(rule, FmsFormulaRuleEnum.CREDIT_AMOUNT.getRule())) {
            return creditAmount.subtract(debitAmount);
        }
        return amount;
    }

    @Override
    public BigDecimal calculateItemLineFormula(String formula, Map<Integer, FmsReportItemRespVO> lineMap,
            boolean opening, boolean current) {
        // 逐个匹配 L 行次引用，按运算符累加对应行次金额
        String expression = CollUtil.getFirst(parseLineFormula(formula));
        if (StrUtil.isEmpty(expression)) {
            return BigDecimal.ZERO;
        }
        // 按 opening、current 选择行次的期初、本期、本年累计或期末金额，逐个累加
        BigDecimal result = BigDecimal.ZERO;
        Matcher matcher = LINE_FORMULA_PATTERN.matcher(expression);
        while (matcher.find()) {
            Integer rowNo = Integer.valueOf(matcher.group(2).substring(1));
            FmsReportItemRespVO item = lineMap.get(rowNo);
            BigDecimal amount = item == null ? BigDecimal.ZERO : opening
                    ? NumberUtils.zeroIfNull(item.getOpeningAmount())
                    : current ? NumberUtils.zeroIfNull(item.getCurrentAmount())
                    : item.getYearAmount() != null ? NumberUtils.zeroIfNull(item.getYearAmount())
                    : NumberUtils.zeroIfNull(item.getClosingAmount());
            result = applyOperator(result, amount, matcher.group(1));
        }
        return result;
    }

    @Override
    public List<String> parseLineFormula(String formula) {
        if (StrUtil.isEmpty(formula)) {
            return new ArrayList<>();
        }
        return JsonUtils.parseArray(formula, String.class);
    }

    @Override
    public Integer parsePeriod(String month) {
        return Integer.valueOf(LocalDateTimeUtils.parseYearMonth(month).format(DatePattern.SIMPLE_MONTH_FORMATTER));
    }

    @Override
    public Integer getPeriodType(Integer fromPeriod, Integer toPeriod) {
        if (fromPeriod > toPeriod) {
            throw exception(LEDGER_PERIOD_INVALID);
        }
        return ObjUtil.equal(fromPeriod, toPeriod)
                ? FmsReportPeriodTypeEnum.MONTH.getType() : FmsReportPeriodTypeEnum.QUARTER.getType();
    }

    private List<FmsLedgerSubjectBalanceRespVO> flattenBalances(List<FmsLedgerSubjectBalanceRespVO> balances) {
        List<FmsLedgerSubjectBalanceRespVO> result = new ArrayList<>();
        for (FmsLedgerSubjectBalanceRespVO balance : balances) {
            result.add(balance);
            result.addAll(flattenBalances(balance.getChildren()));
        }
        return result;
    }

}

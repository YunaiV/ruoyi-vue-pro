package cn.iocoder.yudao.module.fms.service.home;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeMetricDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceIndicatorDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.enums.config.FmsFinanceIndicatorTypeEnum;
import cn.iocoder.yudao.module.fms.enums.home.FmsHomeMetricEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceIndicatorService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.report.FmsBalanceSheetService;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.report.FmsReportCommonService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_NOT_INITIALIZED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.HOME_METRIC_INVALID;

/**
 * FMS 首页 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsHomeServiceImpl implements FmsHomeService {

    private static final int TREND_MONTH_COUNT = 12;
    private static final Pattern LINE_FORMULA_PATTERN = Pattern.compile("([+-]?)L(\\d+)");

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsClosingPeriodService closingPeriodService;
    @Resource
    private FmsIncomeStatementService incomeStatementService;
    @Resource
    private FmsBalanceSheetService balanceSheetService;
    @Resource
    private FmsReportCommonService reportCommonService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsFinanceIndicatorService financeIndicatorService;

    @Override
    public FmsHomeRespVO getHome(Long accountSetId, Long userId) {
        // 1.1 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 校验账套初始化状态
        if (!Boolean.TRUE.equals(accountSet.getInitialized()) || accountSet.getStartTime() == null) {
            throw exception(ACCOUNT_SET_NOT_INITIALIZED);
        }
        // 1.3 确定当前会计期间
        YearMonth currentMonth = closingPeriodService.getCurrentMonth(accountSetId, accountSet.getStartTime());
        YearMonth startMonth = YearMonth.from(accountSet.getStartTime());
        List<FmsFinanceIndicatorDO> indicators = getEnabledIndicators(accountSetId, userId);

        // 2. 计算最近十二期财务指标
        List<FmsHomeRespVO.Trend> trends = new ArrayList<>();
        for (int index = TREND_MONTH_COUNT - 1; index >= 0; index--) {
            YearMonth month = currentMonth.minusMonths(index);
            trends.add(month.isBefore(startMonth) ? buildEmptyTrend(month)
                    : buildTrend(accountSetId, month, indicators, userId));
        }

        // 3. 返回当期指标和趋势
        FmsHomeRespVO.Trend currentTrend = CollUtil.getLast(trends);
        return new FmsHomeRespVO().setCurrentMonth(currentMonth.toString())
                .setMetrics(currentTrend.getMetrics()).setTrends(trends);
    }

    @Override
    public FmsHomeMetricDetailRespVO getMetricDetail(
            Long accountSetId, String metricKey, Long userId) {
        // 1.1 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 1.2 校验财务指标
        FmsFinanceIndicatorDO metric = CollUtil.findOne(
                getEnabledIndicators(accountSetId, userId),
                item -> StrUtil.equals(item.getCode(), metricKey));
        if (metric == null) throw exception(HOME_METRIC_INVALID);
        // 1.3 校验账套初始化状态
        if (!Boolean.TRUE.equals(accountSet.getInitialized()) || accountSet.getStartTime() == null) {
            throw exception(ACCOUNT_SET_NOT_INITIALIZED);
        }
        // 1.4 确定当前会计期间
        YearMonth currentMonth = closingPeriodService.getCurrentMonth(accountSetId, accountSet.getStartTime());
        YearMonth startMonth = YearMonth.from(accountSet.getStartTime());

        // 2. 计算最近十二期指标趋势
        List<FmsHomeMetricDetailRespVO.Trend> trends = new ArrayList<>();
        List<FmsReportItemRespVO> currentItems = new ArrayList<>();
        List<FmsHomeMetricDetailRespVO.Structure> currentStructure = new ArrayList<>();
        for (int index = TREND_MONTH_COUNT - 1; index >= 0; index--) {
            YearMonth month = currentMonth.minusMonths(index);
            BigDecimal amount = BigDecimal.ZERO;
            if (!month.isBefore(startMonth)) {
                amount = calculateIndicator(metric, accountSetId, month, userId);
                if (month.equals(currentMonth)) {
                    // TODO DONE @AI：行次公式统一复用报表公共判断能力。
                    // TODO DONE @AI：报表查询结果先取得，再转换为首页明细结构。
                    if (reportCommonService.isLineFormula(metric.getFormula())) {
                        if (FmsFinanceIndicatorTypeEnum.isIncomeStatement(metric.getType())) {
                            currentItems = getIncomeStatement(accountSetId, month, userId);
                        } else {
                            // 先查询资产负债表行次数据，再转换为首页明细项目
                            FmsReportListReqVO reqVO = new FmsReportListReqVO().setAccountSetId(accountSetId)
                                    .setStartMonth(month.toString()).setEndMonth(month.toString());
                            Map<Integer, FmsReportItemRespVO> lineMap = balanceSheetService.getBalanceSheetLineMap(
                                    reqVO, month, month, userId);
                            currentItems = new ArrayList<>(lineMap.values());
                        }
                    } else {
                        currentStructure = buildSubjectStructure(metric.getFormula(), accountSetId, month, userId,
                                FmsFinanceIndicatorTypeEnum.isIncomeStatement(metric.getType()));
                    }
                }
            }
            trends.add(new FmsHomeMetricDetailRespVO.Trend()
                    .setMonth(month.toString()).setAmount(amount));
        }

        // 3. 返回趋势和当期科目构成
        return new FmsHomeMetricDetailRespVO().setKey(metric.getCode()).setName(metric.getName())
                .setTrends(trends).setStructure(CollUtil.isNotEmpty(currentStructure)
                        ? currentStructure : buildStructure(metric.getFormula(), currentItems));
    }

    /**
     * 构建指定会计期间的首页财务指标趋势
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 当前用户编号
     * @return 首页财务指标趋势
     */
    private FmsHomeRespVO.Trend buildTrend(Long accountSetId, YearMonth month,
                                            List<FmsFinanceIndicatorDO> indicators, Long userId) {
        boolean hasIncome = CollUtil.findOne(indicators,
                item -> FmsFinanceIndicatorTypeEnum.isIncomeStatement(item.getType())) != null;
        boolean hasBalance = CollUtil.findOne(indicators,
                item -> FmsFinanceIndicatorTypeEnum.isBalanceSheet(item.getType())) != null;
        Map<Integer, FmsReportItemRespVO> incomeLineMap = hasIncome
                ? convertMap(getIncomeStatement(accountSetId, month, userId), FmsReportItemRespVO::getRowNo)
                : Collections.emptyMap();
        Map<Integer, FmsReportItemRespVO> balanceLineMap = hasBalance
                ? balanceSheetService.getBalanceSheetLineMap(new FmsReportListReqVO().setAccountSetId(accountSetId)
                        .setStartMonth(month.toString()).setEndMonth(month.toString()), month, month, userId)
                : Collections.emptyMap();
        List<FmsHomeRespVO.Metric> metrics = convertList(indicators, indicator ->
                new FmsHomeRespVO.Metric().setKey(indicator.getCode()).setName(indicator.getName())
                        .setAmount(reportCommonService.isLineFormula(indicator.getFormula())
                                ? calculateIndicator(indicator, incomeLineMap, balanceLineMap)
                                : calculateIndicator(indicator, accountSetId, month, userId)));
        return new FmsHomeRespVO.Trend().setMonth(month.toString()).setMetrics(metrics)
                .setIncome(findAmount(metrics, "income")).setOperatingCost(findAmount(metrics, "operatingCost"))
                .setProfit(findAmount(metrics, "profit")).setExpense(findAmount(metrics, "expense"))
                .setOther(findAmount(metrics, "other"));
    }

    /**
     * 获得指定会计期间的利润表
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 当前用户编号
     * @return 利润表项目数组
     */
    private List<FmsReportItemRespVO> getIncomeStatement(Long accountSetId, YearMonth month, Long userId) {
        // TODO DONE @AI：查询参数先构建，再执行查询。
        FmsReportListReqVO reqVO = new FmsReportListReqVO().setAccountSetId(accountSetId)
                .setStartMonth(month.toString()).setEndMonth(month.toString());
        return incomeStatementService.getIncomeStatement(reqVO, userId);
    }

    /**
     * 构建未启用会计期间的空趋势
     *
     * @param month 会计期间
     * @return 空趋势
     */
    private FmsHomeRespVO.Trend buildEmptyTrend(YearMonth month) {
        return new FmsHomeRespVO.Trend().setMonth(month.toString())
                .setMetrics(new ArrayList<>())
                .setIncome(BigDecimal.ZERO).setOperatingCost(BigDecimal.ZERO).setProfit(BigDecimal.ZERO)
                .setExpense(BigDecimal.ZERO).setOther(BigDecimal.ZERO);
    }

    /**
     * 根据指标公式计算首页财务指标
     *
     * @param indicator 财务指标
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 当前用户编号
     * @return 指标金额
     */
    private BigDecimal calculateIndicator(FmsFinanceIndicatorDO indicator, Long accountSetId, YearMonth month,
                                          Long userId) {
        // 1. 准备当前会计期间的报表查询条件
        FmsReportListReqVO reqVO = new FmsReportListReqVO().setAccountSetId(accountSetId)
                .setStartMonth(month.toString()).setEndMonth(month.toString());
        // 2. 查询指标所属报表的行次数据
        Map<Integer, FmsReportItemRespVO> lineMap;
        if (FmsFinanceIndicatorTypeEnum.isBalanceSheet(indicator.getType())) {
            lineMap = balanceSheetService.getBalanceSheetLineMap(reqVO, month, month, userId);
        } else {
            lineMap = convertMap(getIncomeStatement(accountSetId, month, userId), FmsReportItemRespVO::getRowNo);
        }
        // 3. 行次公式直接按报表行次汇总
        if (reportCommonService.isLineFormula(indicator.getFormula())) {
            return calculateLineFormula(indicator.getFormula(), lineMap,
                    FmsFinanceIndicatorTypeEnum.isIncomeStatement(indicator.getType()));
        }
        // 4. 科目公式查询余额和科目，并按公式规则计算
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(accountSetId, month, month, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjectService.getSubjectList(accountSetId, null, userId), FmsSubjectDO::getId);
        BigDecimal result = BigDecimal.ZERO;
        for (FmsReportFormulaRespVO formula : reportCommonService.parseSubjectFormula(indicator.getFormula())) {
            FmsSubjectDO subject = subjectMap.get(formula.getSubjectId());
            BigDecimal amount = FmsFinanceIndicatorTypeEnum.isBalanceSheet(indicator.getType())
                    ? reportCommonService.calculateBalanceAmount(formula.getRules(), subject, balanceMap,
                            subjectMap, false)
                    : reportCommonService.calculateIncomeOccurrenceAmount(formula.getRules(), subject,
                            balanceMap.get(formula.getSubjectId()), true, formula.getOperator());
            result = reportCommonService.applyOperator(result, amount, formula.getOperator());
        }
        return result;
    }

    private BigDecimal calculateIndicator(FmsFinanceIndicatorDO indicator,
                                          Map<Integer, FmsReportItemRespVO> incomeLineMap,
                                          Map<Integer, FmsReportItemRespVO> balanceLineMap) {
        Map<Integer, FmsReportItemRespVO> lineMap = FmsFinanceIndicatorTypeEnum.isBalanceSheet(indicator.getType())
                ? balanceLineMap : incomeLineMap;
        return reportCommonService.isLineFormula(indicator.getFormula())
                ? calculateLineFormula(indicator.getFormula(), lineMap,
                        FmsFinanceIndicatorTypeEnum.isIncomeStatement(indicator.getType()))
                : BigDecimal.ZERO;
    }

    private BigDecimal findAmount(List<FmsHomeRespVO.Metric> metrics, String key) {
        FmsHomeRespVO.Metric metric = CollUtil.findOne(metrics, item -> StrUtil.equals(item.getKey(), key));
        return metric == null ? BigDecimal.ZERO : NumberUtils.zeroIfNull(metric.getAmount());
    }

    /**
     * 查询账套下已启用的首页财务指标；历史账套未初始化指标时回退到原有五项指标
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 已启用的首页财务指标
     */
    private List<FmsFinanceIndicatorDO> getEnabledIndicators(Long accountSetId, Long userId) {
        List<FmsFinanceIndicatorDO> indicators = financeIndicatorService.getEnabledFinanceIndicatorList(accountSetId,
                userId);
        if (CollUtil.isNotEmpty(indicators)) {
            return indicators;
        }
        // 兼容尚未执行新初始化流程的历史账套，保留原有五项首页指标
        return convertList(Arrays.asList(FmsHomeMetricEnum.values()), metric -> new FmsFinanceIndicatorDO()
                .setCode(metric.getKey()).setName(metric.getName())
                .setType(FmsFinanceIndicatorTypeEnum.INCOME_STATEMENT.getType())
                .setFormula(JsonUtils.toJsonString(Collections.singletonList(metric.getStructureFormula())))
                .setSort(metric.ordinal()).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setAccountSetId(accountSetId));
    }

    /**
     * 计算报表行次公式
     *
     * @param formula 指标公式 JSON
     * @param lineMap 报表行次映射
     * @param current 是否取本期发生额
     * @return 公式计算结果
     */
    private BigDecimal calculateLineFormula(String formula, Map<Integer, FmsReportItemRespVO> lineMap, boolean current) {
        List<String> expressions = JsonUtils.parseArray(formula, String.class);
        String expression = CollUtil.getFirst(expressions);
        if (StrUtil.isEmpty(expression)) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        Matcher matcher = LINE_FORMULA_PATTERN.matcher(expression);
        while (matcher.find()) {
            FmsReportItemRespVO item = lineMap.get(Integer.valueOf(matcher.group(2)));
            BigDecimal amount = item == null ? BigDecimal.ZERO : current
                    ? NumberUtils.zeroIfNull(item.getCurrentAmount())
                    : NumberUtils.zeroIfNull(item.getClosingAmount());
            result = StrUtil.equals(matcher.group(1), "-") ? result.subtract(amount) : result.add(amount);
        }
        return result;
    }

    /**
     * 构建指定财务指标的当期科目构成
     *
     * @param formula 指标公式
     * @param items 当期报表项目数组
     * @return 科目构成数组
     */
    private List<FmsHomeMetricDetailRespVO.Structure> buildStructure(String formula, List<FmsReportItemRespVO> items) {
        // 1. 报表无当期项目时，不生成科目构成
        if (CollUtil.isEmpty(items)) {
            return new ArrayList<>();
        }

        // 2. 建立有效报表行次映射
        Map<Integer, FmsReportItemRespVO> itemMap = convertMap(
                filterList(items, item -> item.getRowNo() != null && item.getRowNo() > 0),
                FmsReportItemRespVO::getRowNo);

        // 3. 递归解析指标行次公式，按科目汇总当期金额
        Map<Long, FmsHomeMetricDetailRespVO.Structure> structureMap = new LinkedHashMap<>();
        collectLineFormula(formula, BigDecimal.ONE, itemMap, structureMap, new HashSet<>());

        // 4. 仅保留正数科目构成，并按金额从大到小排列
        List<FmsHomeMetricDetailRespVO.Structure> result = filterList(structureMap.values(),
                item -> item.getAmount().signum() > 0);
        result.sort(Comparator.comparing(FmsHomeMetricDetailRespVO.Structure::getAmount).reversed());
        return result;
    }

    private List<FmsHomeMetricDetailRespVO.Structure> buildSubjectStructure(String formula, Long accountSetId,
                                                                            YearMonth month, Long userId, boolean income) {
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(accountSetId, month, month, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjectService.getSubjectList(accountSetId, null, userId), FmsSubjectDO::getId);
        Map<Long, FmsHomeMetricDetailRespVO.Structure> structureMap = new LinkedHashMap<>();
        for (FmsReportFormulaRespVO item : reportCommonService.parseSubjectFormula(formula)) {
            FmsSubjectDO subject = subjectMap.get(item.getSubjectId());
            BigDecimal amount = income
                    ? reportCommonService.calculateIncomeOccurrenceAmount(item.getRules(), subject, balanceMap.get(item.getSubjectId()), true, item.getOperator())
                    : reportCommonService.calculateBalanceAmount(item.getRules(), subject, balanceMap, subjectMap, false);
            if (StrUtil.equals(item.getOperator(), "-")) {
                amount = amount.negate();
            }
            FmsHomeMetricDetailRespVO.Structure structure = structureMap.computeIfAbsent(item.getSubjectId(),
                    subjectId -> new FmsHomeMetricDetailRespVO.Structure().setSubjectId(subjectId)
                            .setSubjectCode(subject == null ? item.getSubjectNumber() : subject.getCode())
                            .setSubjectName(subject == null ? item.getSubjectName() : subject.getName())
                            .setAmount(BigDecimal.ZERO));
            structure.setAmount(structure.getAmount().add(amount));
        }
        List<FmsHomeMetricDetailRespVO.Structure> result = filterList(structureMap.values(), item -> item.getAmount().signum() > 0);
        result.sort(Comparator.comparing(FmsHomeMetricDetailRespVO.Structure::getAmount).reversed());
        return result;
    }

    /**
     * 递归解析行次公式并汇总科目构成
     *
     * @param expression 行次公式
     * @param factor 金额系数
     * @param itemMap 行次及利润表项目的映射
     * @param structureMap 科目编号及科目构成的映射
     * @param visitingRowNumbers 当前递归链路中的行次集合
     */
    private void collectLineFormula(String expression, BigDecimal factor, Map<Integer, FmsReportItemRespVO> itemMap,
                                    Map<Long, FmsHomeMetricDetailRespVO.Structure> structureMap, Set<Integer> visitingRowNumbers) {
        Matcher matcher = LINE_FORMULA_PATTERN.matcher(expression);
        while (matcher.find()) {
            BigDecimal rowFactor = StrUtil.equals(matcher.group(1), "-") ? factor.negate() : factor;
            collectRowStructure(Integer.valueOf(matcher.group(2)), rowFactor, itemMap, structureMap, visitingRowNumbers);
        }
    }

    /**
     * 解析利润表行的公式并汇总科目构成
     *
     * @param rowNo 行次
     * @param factor 金额系数
     * @param itemMap 行次及利润表项目的映射
     * @param structureMap 科目编号及科目构成的映射
     * @param visitingRowNumbers 当前递归链路中的行次集合
     */
    private void collectRowStructure(Integer rowNo, BigDecimal factor,
                                     Map<Integer, FmsReportItemRespVO> itemMap,
                                     Map<Long, FmsHomeMetricDetailRespVO.Structure> structureMap,
                                     Set<Integer> visitingRowNumbers) {
        FmsReportItemRespVO item = itemMap.get(rowNo);
        if (item == null || StrUtil.isEmpty(item.getFormula()) || !visitingRowNumbers.add(rowNo)) {
            return;
        }
        try {
            if (item.getFormula().contains("L")) {
                List<String> expressions = JsonUtils.parseArray(item.getFormula(), String.class);
                if (CollUtil.isNotEmpty(expressions)) {
                    collectLineFormula(CollUtil.getFirst(expressions), factor, itemMap, structureMap, visitingRowNumbers);
                }
                return;
            }
            for (FmsReportFormulaRespVO formula : JsonUtils.parseArray(item.getFormula(), FmsReportFormulaRespVO.class)) {
                if (formula.getSubjectId() == null) {
                    continue;
                }
                BigDecimal amount = NumberUtils.zeroIfNull(formula.getCurrentAmount()).multiply(factor);
                if (StrUtil.equals(formula.getOperator(), "-")) {
                    amount = amount.negate();
                }
                FmsHomeMetricDetailRespVO.Structure structure = structureMap.computeIfAbsent(
                        formula.getSubjectId(), subjectId -> new FmsHomeMetricDetailRespVO.Structure()
                                .setSubjectId(subjectId).setSubjectCode(formula.getSubjectNumber())
                                .setSubjectName(formula.getSubjectName()).setAmount(BigDecimal.ZERO));
                structure.setAmount(structure.getAmount().add(amount));
            }
        } finally {
            visitingRowNumbers.remove(rowNo);
        }
    }

}

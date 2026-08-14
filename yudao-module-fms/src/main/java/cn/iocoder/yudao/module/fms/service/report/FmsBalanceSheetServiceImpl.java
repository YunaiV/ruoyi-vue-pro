package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingOverviewRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.balance.FmsBalanceSheetConfigDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.balance.FmsBalanceSheetReportDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.balance.FmsBalanceSheetConfigMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.balance.FmsBalanceSheetReportMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.util.FmsPeriodUtils;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EXISTS;

/**
 * FMS 资产负债表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsBalanceSheetServiceImpl implements FmsBalanceSheetService {

    /**
     * 货币资金行次，现金流量表的现金余额取数行
     */
    public static final Integer MONETARY_CASH_ROW_NO = 1;
    /**
     * 资产总计行次，资产侧与负债侧的分界
     */
    public static final Integer ASSET_TOTAL_ROW_NO = 30;
    /**
     * 负债和所有者权益总计行次，与资产总计勾稽平衡
     */
    public static final Integer LIABILITY_TOTAL_ROW_NO = 53;
    /**
     * 表头行的行次，例如“流动资产：”“流动负债：”，正文行次均大于该值
     */
    private static final int HEADER_ROW_NO = 0;
    /**
     * 负债和所有者权益侧表头的最小显示顺序，资产侧表头和小计的显示顺序均小于该值
     */
    private static final int LIABILITY_SIDE_START_SORT = 32;
    /**
     * 资本化支出科目编码，余额不纳入资产负债表公式覆盖检查
     */
    private static final String CAPITALIZED_EXPENSE_SUBJECT_CODE = "4404";

    @Resource
    private FmsBalanceSheetConfigMapper balanceSheetConfigMapper;
    @Resource
    private FmsBalanceSheetReportMapper balanceSheetReportMapper;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsReportCommonService reportCommonService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsInitialBalanceService initialBalanceService;
    @Resource
    private FmsFinanceParameterService financeParameterService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private FmsClosingPeriodService closingPeriodService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FmsBalanceSheetRowRespVO> getBalanceSheet(FmsReportListReqVO listReqVO, Long userId) {
        // 1.1 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(
                listReqVO.getAccountSetId(), userId);
        // 1.2 加载科目并初始化报表配置
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO.getAccountSetId(), null, userId);
        List<FmsBalanceSheetConfigDO> configs = getOrInitBalanceConfigs(listReqVO.getAccountSetId(), subjects);
        List<FmsBalanceSheetReportDO> reports = getOrCreateBalanceReports(listReqVO, configs);

        // 2. 查询年初至期末的科目余额
        YearMonth endMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth());
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(
                listReqVO.getAccountSetId(), FmsPeriodUtils.getYearStartMonth(accountSet, endMonth), endMonth, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);

        // 3. 计算报表项目，并按行编号配对资产与负债两侧
        Map<Integer, FmsReportItemRespVO> lineMap = new LinkedHashMap<>();
        Map<Integer, FmsBalanceSheetRowRespVO> rowMap = new LinkedHashMap<>();
        for (FmsBalanceSheetReportDO report : reports) {
            FmsBalanceSheetConfigDO config = BeanUtils.toBean(report, FmsBalanceSheetConfigDO.class);
            FmsReportItemRespVO item = buildBalanceItem(config, subjectMap, balanceMap, lineMap);
            if (config.getRowNo() > HEADER_ROW_NO) {
                lineMap.put(config.getRowNo(), item);
            }
            FmsBalanceSheetRowRespVO row = rowMap.computeIfAbsent(config.getRowId(),
                    rowId -> new FmsBalanceSheetRowRespVO().setRowId(rowId));
            fillRowItem(row, item, isAssetItem(config));
        }
        return new ArrayList<>(rowMap.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Integer, FmsReportItemRespVO> getBalanceSheetLineMap(FmsReportListReqVO listReqVO,
                                                                    YearMonth startMonth, YearMonth endMonth, Long userId) {
        // 1. 加载科目并初始化报表配置，报表快照按查询期间只初始化一次
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO.getAccountSetId(), null, userId);
        List<FmsBalanceSheetConfigDO> configs = getOrInitBalanceConfigs(listReqVO.getAccountSetId(), subjects);
        List<FmsBalanceSheetReportDO> reports = getOrCreateBalanceReports(listReqVO, configs);
        List<FmsBalanceSheetConfigDO> reportConfigs = convertList(
                reports, report -> BeanUtils.toBean(report, FmsBalanceSheetConfigDO.class));

        // 2. 查询取数区间的科目余额
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(
                listReqVO.getAccountSetId(), startMonth, endMonth, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);

        // 3. 计算各报表行次金额
        Map<Integer, FmsReportItemRespVO> lineMap = new LinkedHashMap<>();
        for (FmsBalanceSheetConfigDO config : reportConfigs) {
            FmsReportItemRespVO item = buildBalanceItem(config, subjectMap, balanceMap, lineMap);
            if (config.getRowNo() > HEADER_ROW_NO) {
                lineMap.put(config.getRowNo(), item);
            }
        }
        return lineMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmsBalanceSheetCheckRespVO checkBalanceSheet(FmsReportListReqVO listReqVO, Long userId) {
        // 1. 检查资产负债表平衡状态
        List<FmsBalanceSheetRowRespVO> rows = getBalanceSheet(listReqVO, userId);
        FmsBalanceSheetRowRespVO assetTotalRow = CollUtil.findOne(rows,
                row -> ObjUtil.equal(row.getAssetRowNo(), ASSET_TOTAL_ROW_NO));
        FmsBalanceSheetRowRespVO liabilityTotalRow = CollUtil.findOne(rows,
                row -> ObjUtil.equal(row.getLiabilityRowNo(), LIABILITY_TOTAL_ROW_NO));
        BigDecimal openingDifferenceAmount = NumberUtils.zeroIfNull(
                assetTotalRow == null ? null : assetTotalRow.getAssetOpeningAmount())
                .subtract(NumberUtils.zeroIfNull(
                        liabilityTotalRow == null ? null : liabilityTotalRow.getLiabilityOpeningAmount()));
        BigDecimal closingDifferenceAmount = NumberUtils.zeroIfNull(
                assetTotalRow == null ? null : assetTotalRow.getAssetClosingAmount())
                .subtract(NumberUtils.zeroIfNull(
                        liabilityTotalRow == null ? null : liabilityTotalRow.getLiabilityClosingAmount()));

        // 2. 检查初始余额和损益结转状态
        boolean initialBalanceBalanced = Boolean.TRUE.equals(
                initialBalanceService.getTrialBalance(listReqVO.getAccountSetId(), userId).getBalanced());
        boolean profitLossTransferred = isProfitLossTransferred(listReqVO, userId);

        // 3. 检查未纳入报表公式的非损益类科目
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO.getAccountSetId(), null, userId);
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(listReqVO.getAccountSetId());
        String capitalizedExpenseSubjectCode = financeParameterService.convertStandardSubjectCode(
                CAPITALIZED_EXPENSE_SUBJECT_CODE, financeParameter.getSubjectCodeRule());
        Set<Long> formulaSubjectIds = collectRowFormulaSubjectIds(rows);
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(
                listReqVO.getAccountSetId(), LocalDateTimeUtils.parseYearMonth(listReqVO.getStartMonth()),
                LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth()), userId);
        List<FmsBalanceSheetCheckRespVO.UnmappedSubject> unmappedSubjects = new ArrayList<>();
        for (FmsSubjectDO subject : subjects) {
            // 一级非损益科目存在余额且未纳入公式时，需要提示补充报表公式；资本化支出科目除外
            if (!reportCommonService.isRootSubject(subject)
                    || ObjUtil.equal(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                    || StrUtil.equals(subject.getCode(), capitalizedExpenseSubjectCode)
                    || reportCommonService.isSubjectTreeMapped(subject, subjects, formulaSubjectIds)
                    || !hasBalance(balanceMap.get(subject.getId()))) {
                continue;
            }
            unmappedSubjects.add(new FmsBalanceSheetCheckRespVO.UnmappedSubject()
                    .setId(subject.getId()).setCode(subject.getCode()).setName(subject.getName()));
        }
        return new FmsBalanceSheetCheckRespVO()
                .setBalanced(openingDifferenceAmount.signum() == 0 && closingDifferenceAmount.signum() == 0)
                .setInitialBalanceBalanced(initialBalanceBalanced)
                .setProfitLossTransferred(profitLossTransferred)
                .setOpeningDifferenceAmount(openingDifferenceAmount)
                .setClosingDifferenceAmount(closingDifferenceAmount)
                .setUnmappedSubjects(unmappedSubjects);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("DataFlowIssue")
    public void updateBalanceSheetFormula(FmsReportFormulaUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验报表项目
        FmsBalanceSheetReportDO report = balanceSheetReportMapper.selectById(updateReqVO.getId());
        reportCommonService.validateReportItemEditable(updateReqVO.getAccountSetId(),
                report == null ? null : report.getAccountSetId(), report == null ? null : report.getEditable());
        List<FmsBalanceSheetConfigDO> configs = balanceSheetConfigMapper.selectListByAccountSetId(
                updateReqVO.getAccountSetId());
        FmsBalanceSheetConfigDO config = CollUtil.findOne(configs, item ->
                        ObjUtil.equal(item.getName(), report.getName())
                                && ObjUtil.equal(item.getRowNo(), report.getRowNo())
                                && ObjUtil.equal(item.getLevel(), report.getLevel())
                                && ObjUtil.equal(item.getSort(), report.getSort()));
        if (config == null) {
            throw exception(REPORT_CONFIG_NOT_EXISTS);
        }

        // 2. 校验并构造资产负债表公式
        List<FmsReportFormulaRespVO> formulas = reportCommonService.buildReportFormulaList(updateReqVO,
                FmsFormulaRuleEnum.BALANCE.getRule(), FmsFormulaRuleEnum.SUBJECT_CREDIT_BALANCE.getRule(), userId);

        // 3. 更新当前期间和未出报表期间使用的公式
        String formula = JsonUtils.toJsonString(formulas);
        balanceSheetReportMapper.updateById(new FmsBalanceSheetReportDO().setId(report.getId()).setFormula(formula));
        balanceSheetConfigMapper.updateById(new FmsBalanceSheetConfigDO().setId(config.getId()).setFormula(formula));
    }

    /**
     * 判断报表配置是否属于资产侧
     *
     * 行次大于表头行次且不超过资产总计行次的为正文资产项；行次为表头行次时，按显示顺序是否早于负债侧表头判断
     *
     * @param config 报表配置
     * @return 是否资产侧
     */
    private boolean isAssetItem(FmsBalanceSheetConfigDO config) {
        return config.getRowNo() > HEADER_ROW_NO && config.getRowNo() <= ASSET_TOTAL_ROW_NO
                || config.getRowNo() == HEADER_ROW_NO && config.getSort() < LIABILITY_SIDE_START_SORT;
    }

    /**
     * 将报表项目填充到配对行的资产侧或负债侧字段
     *
     * @param row 报表行
     * @param item 报表项目
     * @param asset 是否资产侧
     */
    private void fillRowItem(FmsBalanceSheetRowRespVO row, FmsReportItemRespVO item, boolean asset) {
        if (asset) {
            row.setAssetId(item.getId()).setAssetName(item.getName()).setAssetRowNo(item.getRowNo())
                    .setAssetClosingAmount(item.getClosingAmount()).setAssetOpeningAmount(item.getOpeningAmount())
                    .setAssetLevel(item.getLevel()).setAssetEditable(item.getEditable())
                    .setAssetFormula(item.getFormula());
        } else {
            row.setLiabilityId(item.getId()).setLiabilityName(item.getName())
                    .setLiabilityRowNo(item.getRowNo()).setLiabilityClosingAmount(item.getClosingAmount())
                    .setLiabilityOpeningAmount(item.getOpeningAmount()).setLiabilityLevel(item.getLevel())
                    .setLiabilityEditable(item.getEditable()).setLiabilityFormula(item.getFormula());
        }
    }

    /**
     * 判断查询期间内各月的损益是否均已结转，逐月检查结账概览的损益余额
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 是否已结转
     */
    private boolean isProfitLossTransferred(FmsReportListReqVO listReqVO, Long userId) {
        YearMonth endMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth());
        for (YearMonth currentMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getStartMonth());
                !currentMonth.isAfter(endMonth); currentMonth = currentMonth.plusMonths(1)) {
            FmsClosingQueryReqVO closingQueryReqVO = new FmsClosingQueryReqVO();
            closingQueryReqVO.setAccountSetId(listReqVO.getAccountSetId());
            closingQueryReqVO.setMonth(currentMonth.toString());
            FmsClosingOverviewRespVO overview = closingPeriodService.getClosingOverview(closingQueryReqVO, userId);
            // 当月损益余额不为零时，说明存在尚未结转的损益
            if (NumberUtils.zeroIfNull(overview.getProfitLossBalance()).signum() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 收集报表各行公式中引用的科目编号集合，用于公式覆盖检查
     *
     * @param rows 报表行
     * @return 公式中的科目编号集合
     */
    private Set<Long> collectRowFormulaSubjectIds(List<FmsBalanceSheetRowRespVO> rows) {
        Set<Long> subjectIds = new LinkedHashSet<>();
        for (FmsBalanceSheetRowRespVO row : rows) {
            subjectIds.addAll(reportCommonService.parseFormulaSubjectIds(row.getAssetFormula()));
            subjectIds.addAll(reportCommonService.parseFormulaSubjectIds(row.getLiabilityFormula()));
        }
        return subjectIds;
    }

    /**
     * 判断科目是否存在期初或期末余额
     *
     * @param balance 科目余额
     * @return 是否存在余额
     */
    private boolean hasBalance(FmsLedgerSubjectBalanceRespVO balance) {
        return balance != null && (NumberUtils.zeroIfNull(balance.getOpeningDebitAmount()).signum() != 0
                || NumberUtils.zeroIfNull(balance.getOpeningCreditAmount()).signum() != 0
                || NumberUtils.zeroIfNull(balance.getEndingDebitAmount()).signum() != 0
                || NumberUtils.zeroIfNull(balance.getEndingCreditAmount()).signum() != 0);
    }

    /**
     * 计算报表项目的期初和期末金额
     *
     * 行次公式直接引用已计算的行次金额；科目公式逐项取数并按运算符累加，取数结果回写公式项用于前端展示
     *
     * @param config 报表配置
     * @param subjectMap 科目编号到科目的 Map
     * @param balanceMap 科目编号到余额的 Map
     * @param lineMap 行次到已计算报表项目的 Map
     * @return 报表项目
     */
    private FmsReportItemRespVO buildBalanceItem(FmsBalanceSheetConfigDO config,
                                                 Map<Long, FmsSubjectDO> subjectMap,
                                                 Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap,
                                                 Map<Integer, FmsReportItemRespVO> lineMap) {
        FmsReportItemRespVO item = new FmsReportItemRespVO().setId(config.getId()).setName(config.getName())
                .setRowNo(config.getRowNo()).setLevel(config.getLevel()).setEditable(config.getEditable())
                .setFormula(config.getFormula()).setOpeningAmount(BigDecimal.ZERO)
                .setClosingAmount(BigDecimal.ZERO);
        // 行次公式直接引用前面已计算的行次金额，例如 ["L1-L2"]
        if (reportCommonService.isLineFormula(config.getFormula())) {
            item.setOpeningAmount(reportCommonService.calculateItemLineFormula(config.getFormula(), lineMap, true, false))
                    .setClosingAmount(reportCommonService.calculateItemLineFormula(config.getFormula(), lineMap, false, false));
            return item;
        }
        // 科目公式逐项取数，按运算符累加到期初、期末金额
        List<FmsReportFormulaRespVO> formulas = reportCommonService.parseSubjectFormula(config.getFormula());
        for (FmsReportFormulaRespVO formula : formulas) {
            FmsSubjectDO subject = subjectMap.get(formula.getSubjectId());
            reportCommonService.normalizeSubjectFormula(formula, subject);
            BigDecimal openingAmount = reportCommonService.calculateBalanceAmount(
                    formula.getRules(), subject, balanceMap, subjectMap, true);
            BigDecimal closingAmount = reportCommonService.calculateBalanceAmount(
                    formula.getRules(), subject, balanceMap, subjectMap, false);
            formula.setOpeningAmount(openingAmount).setClosingAmount(closingAmount);
            item.setOpeningAmount(reportCommonService.applyOperator(item.getOpeningAmount(), openingAmount, formula.getOperator()))
                    .setClosingAmount(reportCommonService.applyOperator(item.getClosingAmount(), closingAmount, formula.getOperator()));
        }
        // 回写含取数结果的公式，供前端公式编辑弹窗展示各项金额
        item.setFormula(JsonUtils.toJsonString(formulas));
        return item;
    }

    /**
     * 获得账套的报表配置，首次查询时按报表模板初始化
     *
     * 初始化时将模板公式中的科目编码绑定为账套下的科目编号，编码不存在的科目编号置空
     *
     * @param accountSetId 账套编号
     * @param subjects 账套下的科目列表
     * @return 报表配置列表
     */
    private List<FmsBalanceSheetConfigDO> getOrInitBalanceConfigs(Long accountSetId, List<FmsSubjectDO> subjects) {
        // 1. 已初始化时直接返回
        List<FmsBalanceSheetConfigDO> configs = balanceSheetConfigMapper.selectListByAccountSetId(accountSetId);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复初始化
        accountSetService.lockAccountSet(accountSetId);
        configs = balanceSheetConfigMapper.selectListByAccountSetId(accountSetId);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 3. 按报表模板初始化，模板为空时返回空列表
        List<FmsReportTemplateDO> templates = reportTemplateMapper.selectListByType(
                FmsReportTypeEnum.BALANCE_SHEET.getType());
        if (CollUtil.isEmpty(templates)) {
            return Collections.emptyList();
        }
        // 4. 模板公式中的科目编码绑定为账套下的科目编号后批量插入
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode,
                Function.identity(), (first, second) -> first);
        configs = convertList(templates, template -> BeanUtils.toBean(template, FmsBalanceSheetConfigDO.class)
                .setId(null).setAccountSetId(accountSetId)
                .setFormula(reportCommonService.bindSubjectFormula(template.getFormula(), subjectMap)));
        balanceSheetConfigMapper.insertBatch(configs);
        return configs;
    }

    /**
     * 获得查询期间的报表快照，首次查询该期间时按当前配置创建
     *
     * @param listReqVO 查询条件
     * @param configs 报表配置列表
     * @return 报表快照列表
     */
    private List<FmsBalanceSheetReportDO> getOrCreateBalanceReports(
            FmsReportListReqVO listReqVO, List<FmsBalanceSheetConfigDO> configs) {
        // 1. 已生成快照时直接返回；配置为空时不生成
        Integer fromPeriod = reportCommonService.parsePeriod(listReqVO.getStartMonth());
        Integer toPeriod = reportCommonService.parsePeriod(listReqVO.getEndMonth());
        Integer periodType = reportCommonService.getPeriodType(fromPeriod, toPeriod);
        List<FmsBalanceSheetReportDO> reports = balanceSheetReportMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        if (CollUtil.isNotEmpty(reports) || CollUtil.isEmpty(configs)) {
            return reports;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复生成期间快照
        accountSetService.lockAccountSet(listReqVO.getAccountSetId());
        reports = balanceSheetReportMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        if (CollUtil.isNotEmpty(reports)) {
            return reports;
        }
        // 3. 按当前配置创建期间快照，金额初始化为零、未结账
        reports = convertList(configs, config -> BeanUtils.toBean(config, FmsBalanceSheetReportDO.class)
                .setId(null).setFromPeriod(fromPeriod).setToPeriod(toPeriod).setType(periodType)
                .setOpeningAmount(BigDecimal.ZERO).setClosingAmount(BigDecimal.ZERO).setSettled(false));
        balanceSheetReportMapper.insertBatch(reports);
        return reports;
    }

}

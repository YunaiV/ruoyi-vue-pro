package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.income.FmsIncomeStatementCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.income.FmsIncomeStatementConfigDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.income.FmsIncomeStatementReportDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.income.FmsIncomeStatementConfigMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.income.FmsIncomeStatementReportMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EXISTS;

/**
 * FMS 利润表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsIncomeStatementServiceImpl implements FmsIncomeStatementService {

    /**
     * 利润表净利润行次
     */
    private static final int NET_PROFIT_ROW_NO = 32;
    /**
     * 资产负债表未分配利润行次
     */
    private static final int RETAINED_EARNINGS_ROW_NO = 51;
    /**
     * 以前年度损益调整等科目编码前缀，不纳入利润表公式覆盖检查
     */
    private static final String EXCLUDED_PROFIT_LOSS_CODE_PREFIX = "6";

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsReportCommonService reportCommonService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsBalanceSheetService balanceSheetService;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;
    @Resource
    private FmsIncomeStatementConfigMapper incomeStatementConfigMapper;
    @Resource
    private FmsIncomeStatementReportMapper incomeStatementReportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FmsReportItemRespVO> getIncomeStatement(FmsReportListReqVO listReqVO, Long userId) {
        // 1.1 校验账套读权限
        accountSetService.validateAccountSetReadPermission(listReqVO.getAccountSetId(), userId);
        // 1.2 加载科目并初始化报表配置
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO.getAccountSetId(), null, userId);
        List<FmsIncomeStatementConfigDO> configs = getOrInitIncomeConfigs(listReqVO.getAccountSetId(), subjects);
        List<FmsIncomeStatementReportDO> reports = getOrCreateIncomeReports(listReqVO, configs);

        // 2. 查询本期和本年科目发生额
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(
                listReqVO.getAccountSetId(), LocalDateTimeUtils.parseYearMonth(listReqVO.getStartMonth()),
                LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth()), userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);

        // 3. 计算报表项目
        Map<Integer, FmsReportItemRespVO> lineMap = new LinkedHashMap<>();
        List<FmsReportItemRespVO> result = new ArrayList<>();
        for (FmsIncomeStatementReportDO report : reports) {
            FmsIncomeStatementConfigDO config = BeanUtils.toBean(report, FmsIncomeStatementConfigDO.class);
            FmsReportItemRespVO item = buildIncomeItem(config, subjectMap, balanceMap, lineMap);
            if (config.getRowNo() != null && config.getRowNo() > 0) {
                lineMap.put(config.getRowNo(), item);
            }
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmsIncomeStatementCheckRespVO checkIncomeStatement(FmsReportListReqVO listReqVO, Long userId) {
        // 1. 检查利润表与资产负债表的勾稽关系：净利润本年累计应等于未分配利润变动
        List<FmsReportItemRespVO> incomeItems = getIncomeStatement(listReqVO, userId);
        FmsReportItemRespVO netProfitItem = CollUtil.findOne(incomeItems,
                item -> ObjUtil.equal(item.getRowNo(), NET_PROFIT_ROW_NO));
        List<FmsBalanceSheetRowRespVO> balanceRows = balanceSheetService.getBalanceSheet(listReqVO, userId);
        FmsBalanceSheetRowRespVO retainedEarningsRow = CollUtil.findOne(balanceRows,
                row -> ObjUtil.equal(row.getLiabilityRowNo(), RETAINED_EARNINGS_ROW_NO));
        BigDecimal retainedEarningsChange = NumberUtils.zeroIfNull(retainedEarningsRow == null
                ? null : retainedEarningsRow.getLiabilityClosingAmount()).subtract(NumberUtils.zeroIfNull(
                        retainedEarningsRow == null ? null : retainedEarningsRow.getLiabilityOpeningAmount()));
        BigDecimal differenceAmount = NumberUtils.zeroIfNull(
                netProfitItem == null ? null : netProfitItem.getYearAmount()).subtract(retainedEarningsChange);

        // 2. 检查未纳入利润表公式的一级损益类科目
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO.getAccountSetId(), null, userId);
        Set<Long> formulaSubjectIds = new LinkedHashSet<>();
        for (FmsReportItemRespVO item : incomeItems) {
            formulaSubjectIds.addAll(reportCommonService.parseFormulaSubjectIds(item.getFormula()));
        }
        List<FmsIncomeStatementCheckRespVO.UnmappedSubject> unmappedSubjects = new ArrayList<>();
        for (FmsSubjectDO subject : subjects) {
            // 一级损益类科目未纳入公式时需要提示补充；以前年度损益调整等 6 开头科目除外
            if (!reportCommonService.isRootSubject(subject)
                    || ObjUtil.notEqual(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                    || StrUtil.startWith(subject.getCode(), EXCLUDED_PROFIT_LOSS_CODE_PREFIX)
                    || reportCommonService.isSubjectTreeMapped(subject, subjects, formulaSubjectIds)) {
                continue;
            }
            unmappedSubjects.add(new FmsIncomeStatementCheckRespVO.UnmappedSubject()
                    .setId(subject.getId()).setCode(subject.getCode()).setName(subject.getName()));
        }
        return new FmsIncomeStatementCheckRespVO().setBalanced(differenceAmount.signum() == 0)
                .setDifferenceAmount(differenceAmount).setUnmappedSubjects(unmappedSubjects);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("DataFlowIssue")
    public void updateIncomeStatementFormula(FmsReportFormulaUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验报表项目
        FmsIncomeStatementReportDO report = incomeStatementReportMapper.selectById(updateReqVO.getId());
        reportCommonService.validateReportItemEditable(updateReqVO.getAccountSetId(),
                report == null ? null : report.getAccountSetId(), report == null ? null : report.getEditable());
        // 2. 校验并构造利润表公式
        List<FmsReportFormulaRespVO> formulas = reportCommonService.buildReportFormulaList(updateReqVO,
                FmsFormulaRuleEnum.DEBIT_AMOUNT.getRule(), FmsFormulaRuleEnum.PROFIT_LOSS_AMOUNT.getRule(), userId);

        // 3. 只更新当前期间报表快照，后续期间仍使用基础配置
        incomeStatementReportMapper.updateById(new FmsIncomeStatementReportDO().setId(report.getId())
                .setFormula(JsonUtils.toJsonString(formulas)));
    }

    /**
     * 计算报表项目的本期和本年累计金额
     *
     * 行次公式直接引用已计算的行次金额；科目公式逐项取数并按运算符累加，取数结果回写公式项用于前端展示
     *
     * @param config 报表配置
     * @param subjectMap 科目编号到科目的 Map
     * @param balanceMap 科目编号到余额的 Map
     * @param lineMap 行次到已计算报表项目的 Map
     * @return 报表项目
     */
    private FmsReportItemRespVO buildIncomeItem(FmsIncomeStatementConfigDO config,
            Map<Long, FmsSubjectDO> subjectMap, Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap,
            Map<Integer, FmsReportItemRespVO> lineMap) {
        FmsReportItemRespVO item = new FmsReportItemRespVO().setId(config.getId()).setName(config.getName())
                .setRowNo(config.getRowNo()).setLevel(config.getLevel()).setEditable(config.getEditable())
                .setFormula(config.getFormula()).setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO);
        // 行次公式直接引用前面已计算的行次金额，例如 ["L1-L2"]
        if (reportCommonService.isLineFormula(config.getFormula())) {
            item.setCurrentAmount(reportCommonService.calculateItemLineFormula(
                            config.getFormula(), lineMap, false, true))
                    .setYearAmount(reportCommonService.calculateItemLineFormula(
                            config.getFormula(), lineMap, false, false));
            return item;
        }
        // 科目公式逐项取数，按运算符累加到本期、本年累计金额
        List<FmsReportFormulaRespVO> formulas = reportCommonService.parseSubjectFormula(config.getFormula());
        for (FmsReportFormulaRespVO formula : formulas) {
            FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(formula.getSubjectId());
            FmsSubjectDO subject = subjectMap.get(formula.getSubjectId());
            reportCommonService.normalizeSubjectFormula(formula, subject);
            BigDecimal currentAmount = reportCommonService.calculateIncomeOccurrenceAmount(
                    formula.getRules(), subject, balance, true, formula.getOperator());
            BigDecimal yearAmount = reportCommonService.calculateIncomeOccurrenceAmount(
                    formula.getRules(), subject, balance, false, formula.getOperator());
            formula.setCurrentAmount(currentAmount).setYearAmount(yearAmount);
            item.setCurrentAmount(reportCommonService.applyOperator(
                            item.getCurrentAmount(), currentAmount, formula.getOperator()))
                    .setYearAmount(reportCommonService.applyOperator(
                            item.getYearAmount(), yearAmount, formula.getOperator()));
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
    private List<FmsIncomeStatementConfigDO> getOrInitIncomeConfigs(
            Long accountSetId, List<FmsSubjectDO> subjects) {
        // 1. 已初始化时直接返回
        List<FmsIncomeStatementConfigDO> configs = incomeStatementConfigMapper.selectListByAccountSetId(accountSetId);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复初始化
        accountSetService.lockAccountSet(accountSetId);
        configs = incomeStatementConfigMapper.selectListByAccountSetId(accountSetId);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 3. 按报表模板初始化，模板为空时返回空列表
        List<FmsReportTemplateDO> templates = reportTemplateMapper.selectListByType(
                FmsReportTypeEnum.INCOME_STATEMENT.getType());
        if (CollUtil.isEmpty(templates)) {
            return Collections.emptyList();
        }
        // 4. 模板公式中的科目编码绑定为账套下的科目编号后批量插入
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode,
                Function.identity(), (first, second) -> first);
        configs = convertList(templates, template -> BeanUtils.toBean(template, FmsIncomeStatementConfigDO.class)
                .setId(null).setAccountSetId(accountSetId)
                .setFormula(reportCommonService.bindSubjectFormula(template.getFormula(), subjectMap)));
        incomeStatementConfigMapper.insertBatch(configs);
        return configs;
    }

    /**
     * 获得查询期间的报表快照，首次查询该期间时按当前配置创建
     *
     * @param listReqVO 查询条件
     * @param configs 报表配置列表
     * @return 报表快照列表
     */
    private List<FmsIncomeStatementReportDO> getOrCreateIncomeReports(
            FmsReportListReqVO listReqVO, List<FmsIncomeStatementConfigDO> configs) {
        // 1. 已生成快照时直接返回；配置为空时不生成
        Integer fromPeriod = reportCommonService.parsePeriod(listReqVO.getStartMonth());
        Integer toPeriod = reportCommonService.parsePeriod(listReqVO.getEndMonth());
        Integer periodType = reportCommonService.getPeriodType(fromPeriod, toPeriod);
        List<FmsIncomeStatementReportDO> reports = incomeStatementReportMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        if (CollUtil.isNotEmpty(reports) || CollUtil.isEmpty(configs)) {
            return reports;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复生成期间快照
        accountSetService.lockAccountSet(listReqVO.getAccountSetId());
        reports = incomeStatementReportMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        if (CollUtil.isNotEmpty(reports)) {
            return reports;
        }
        // 3. 按当前配置创建期间快照，金额初始化为零、未结账
        reports = convertList(configs, config -> BeanUtils.toBean(config, FmsIncomeStatementReportDO.class)
                .setId(null).setFromPeriod(fromPeriod).setToPeriod(toPeriod).setType(periodType)
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO).setSettled(false));
        incomeStatementReportMapper.insertBatch(reports);
        return reports;
    }

}

package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowStatementUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowExtendConfigDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowExtendDataDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowStatementConfigDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowStatementReportDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow.FmsCashFlowExtendConfigMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow.FmsCashFlowExtendDataMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow.FmsCashFlowStatementConfigMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow.FmsCashFlowStatementReportMapper;
import cn.iocoder.yudao.module.fms.enums.report.FmsCashFlowExtendTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsCashFlowParamTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.util.FmsPeriodUtils;
import com.googlecode.aviator.AviatorEvaluator;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_ADJUSTMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_AMOUNT_NOT_ADJUSTABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EDITABLE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_ADJUSTMENT_FORMULA_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_ADJUSTMENT_FORMULA_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_ADJUSTMENT_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_ADJUSTMENT_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_STATEMENT_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_STATEMENT_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CASH_FLOW_STATEMENT_UPDATE_SUCCESS;

/**
 * FMS 现金流量表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsCashFlowStatementServiceImpl implements FmsCashFlowStatementService {

    /**
     * 现金流量表主表分类
     */
    private static final Integer CASH_FLOW_CATEGORY_MAIN = 1;
    /**
     * 其他经营活动现金行次，未明确归类的经营活动现金流归入该行
     */
    private static final int OTHER_OPERATING_ROW_NO = 6;
    /**
     * 期末现金及现金等价物余额行次
     */
    private static final int ENDING_CASH_ROW_NO = 22;

    private static final Pattern LINE_FORMULA_PATTERN = Pattern.compile("([+-]?)(L\\d+)");
    private static final Pattern CASH_FLOW_PARAM_PATTERN = Pattern.compile("BA\\[\\d+,[12]]|IN\\d+|EX\\d+|L\\d+");
    private static final Pattern CASH_FLOW_EXPRESSION_PATTERN = Pattern.compile("[A-Z0-9_+\\-*/().?:<>=\\s]+");

    @Resource
    private FmsCashFlowStatementConfigMapper cashFlowStatementConfigMapper;
    @Resource
    private FmsCashFlowStatementReportMapper cashFlowStatementReportMapper;
    @Resource
    private FmsCashFlowExtendConfigMapper cashFlowExtendConfigMapper;
    @Resource
    private FmsCashFlowExtendDataMapper cashFlowExtendDataMapper;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsReportCommonService reportCommonService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsBalanceSheetService balanceSheetService;
    @Resource
    private FmsIncomeStatementService incomeStatementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FmsReportItemRespVO> getCashFlowStatement(FmsReportListReqVO listReqVO, Long userId) {
        // 1.1 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(
                listReqVO.getAccountSetId(), userId);
        // 1.2 初始化报表配置
        List<FmsCashFlowStatementConfigDO> configs = getOrInitCashFlowConfigs(listReqVO.getAccountSetId());
        List<FmsCashFlowStatementReportDO> reports = getOrCreateCashFlowReports(listReqVO, configs);

        // 2. 查询本期和本年的资产负债、利润表和辅助数据取数
        YearMonth startMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getStartMonth());
        YearMonth endMonth = LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth());
        Map<Integer, FmsReportItemRespVO> currentBalanceMap = balanceSheetService.getBalanceSheetLineMap(
                listReqVO, startMonth, endMonth, userId);
        Map<Integer, FmsReportItemRespVO> yearBalanceMap = balanceSheetService.getBalanceSheetLineMap(
                listReqVO, FmsPeriodUtils.getYearStartMonth(accountSet, endMonth), endMonth, userId);
        List<FmsReportItemRespVO> incomeItems = incomeStatementService.getIncomeStatement(listReqVO, userId);
        Map<Integer, FmsReportItemRespVO> incomeMap = convertMap(
                filterList(incomeItems, item -> item.getRowNo() != null && item.getRowNo() > 0),
                FmsReportItemRespVO::getRowNo, Function.identity(), (first, second) -> first);
        List<FmsCashFlowAdjustmentRespVO> adjustments = getCashFlowAdjustmentListInternal(listReqVO, userId);
        Map<Integer, FmsCashFlowAdjustmentRespVO> adjustmentMap = convertMap(
                filterList(adjustments, item -> item.getRowNo() != null && item.getRowNo() > 0),
                FmsCashFlowAdjustmentRespVO::getRowNo, Function.identity(), (first, second) -> first);

        // 3. 计算现金流量表项目
        Map<Integer, FmsCashFlowStatementReportDO> reportMap = convertMap(
                filterList(reports, report -> report.getRowNo() != null && report.getRowNo() > 0),
                FmsCashFlowStatementReportDO::getRowNo, Function.identity(), (first, second) -> first);
        Map<Integer, FmsReportItemRespVO> lineMap = new LinkedHashMap<>();
        List<FmsReportItemRespVO> result = new ArrayList<>();
        for (FmsCashFlowStatementReportDO report : reports) {
            FmsCashFlowStatementConfigDO config = BeanUtils.toBean(report, FmsCashFlowStatementConfigDO.class);
            FmsReportItemRespVO item = BeanUtils.toBean(report, FmsReportItemRespVO.class);
            BigDecimal currentAmount = calculateCashFlowFormula(config.getFormula(), currentBalanceMap,
                    incomeMap, adjustmentMap, lineMap, true);
            BigDecimal yearAmount = calculateCashFlowFormula(config.getFormula(), yearBalanceMap,
                    incomeMap, adjustmentMap, lineMap, false);
            if (isAmountAdjustable(report)) {
                currentAmount = getSavedAmount(report.getCurrentAmount(), currentAmount);
                yearAmount = getSavedAmount(report.getYearAmount(), yearAmount);
            }
            item.setCurrentAmount(currentAmount).setYearAmount(yearAmount);
            if (config.getRowNo() != null && config.getRowNo() > 0) {
                lineMap.put(config.getRowNo(), item);
            }
            result.add(item);
        }

        // 4. 将未明确归类的经营活动现金流归入其他经营活动
        reconcileCashFlow(result, lineMap, currentBalanceMap, yearBalanceMap, adjustmentMap, reportMap);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CASH_FLOW_STATEMENT_TYPE,
            subType = FMS_CASH_FLOW_STATEMENT_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_CASH_FLOW_STATEMENT_UPDATE_SUCCESS)
    public void updateCashFlowStatement(FmsCashFlowStatementUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验现金流量表项目属于指定期间
        Integer fromPeriod = reportCommonService.parsePeriod(updateReqVO.getStartMonth());
        Integer toPeriod = reportCommonService.parsePeriod(updateReqVO.getEndMonth());
        Integer periodType = reportCommonService.getPeriodType(fromPeriod, toPeriod);
        List<Long> ids = convertList(updateReqVO.getItems(), FmsCashFlowStatementUpdateReqVO.Item::getId);
        List<FmsCashFlowStatementReportDO> reports = cashFlowStatementReportMapper.selectListByIdsAndPeriod(
                ids, updateReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        Map<Long, FmsCashFlowStatementReportDO> reportMap = convertMap(
                reports, FmsCashFlowStatementReportDO::getId);
        if (reportMap.size() != new LinkedHashSet<>(ids).size()) {
            throw exception(REPORT_CONFIG_NOT_EXISTS);
        }
        // 1.3 校验报表项目允许调整金额
        for (FmsCashFlowStatementUpdateReqVO.Item item : updateReqVO.getItems()) {
            if (!isAmountAdjustable(reportMap.get(item.getId()))) {
                throw exception(REPORT_AMOUNT_NOT_ADJUSTABLE);
            }
        }

        // 2. 更新现金流量表金额，金额为零时重新按公式计算
        cashFlowStatementReportMapper.updateBatch(convertList(updateReqVO.getItems(), item ->
                new FmsCashFlowStatementReportDO().setId(item.getId())
                        .setCurrentAmount(item.getCurrentAmount())
                        .setYearAmount(item.getYearAmount())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmsCashFlowCheckRespVO checkCashFlowStatement(FmsReportListReqVO listReqVO, Long userId) {
        // 现金流量表本身无独立平衡关系，直接复用资产负债表检查，并汇总是否满足取数条件
        FmsBalanceSheetCheckRespVO balanceCheck = balanceSheetService.checkBalanceSheet(listReqVO, userId);
        return BeanUtils.toBean(balanceCheck, FmsCashFlowCheckRespVO.class)
                .setBalanceSheetReady(Boolean.TRUE.equals(balanceCheck.getBalanced())
                        && Boolean.TRUE.equals(balanceCheck.getInitialBalanceBalanced())
                        && Boolean.TRUE.equals(balanceCheck.getProfitLossTransferred())
                        && CollUtil.isEmpty(balanceCheck.getUnmappedSubjects()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FmsCashFlowAdjustmentRespVO> getCashFlowAdjustmentList(
            FmsReportListReqVO listReqVO, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(listReqVO.getAccountSetId(), userId);

        // 2. 初始化并计算辅助数据
        return getCashFlowAdjustmentListInternal(listReqVO, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CASH_FLOW_STATEMENT_TYPE,
            subType = FMS_CASH_FLOW_ADJUSTMENT_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_CASH_FLOW_ADJUSTMENT_UPDATE_SUCCESS)
    public void updateCashFlowAdjustment(
            FmsCashFlowAdjustmentUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验现金流量辅助数据
        List<Long> ids = convertList(updateReqVO.getItems(), FmsCashFlowAdjustmentUpdateReqVO.Item::getId);
        List<FmsCashFlowExtendDataDO> dataList = cashFlowExtendDataMapper.selectListByIdsAndAccountSetId(
                ids, updateReqVO.getAccountSetId());
        Map<Long, FmsCashFlowExtendDataDO> dataMap = convertMap(dataList, FmsCashFlowExtendDataDO::getId);
        if (dataMap.size() != new LinkedHashSet<>(ids).size()) {
            throw exception(REPORT_ADJUSTMENT_NOT_EXISTS);
        }
        // 1.3 校验辅助数据允许编辑
        for (FmsCashFlowAdjustmentUpdateReqVO.Item item : updateReqVO.getItems()) {
            if (Boolean.FALSE.equals(dataMap.get(item.getId()).getEditable())) {
                throw exception(REPORT_CONFIG_NOT_EDITABLE);
            }
        }

        // 2. 批量更新可编辑金额
        cashFlowExtendDataMapper.updateBatch(convertList(updateReqVO.getItems(),
                item -> new FmsCashFlowExtendDataDO().setId(item.getId())
                        .setCurrentAmount(item.getCurrentAmount()).setYearAmount(item.getYearAmount())));

        // 3. 辅助资料变化后，主表人工调整失效，下次查询重新按公式计算
        FmsCashFlowExtendDataDO periodData = CollUtil.getFirst(dataList);
        cashFlowStatementReportMapper.clearAdjustedAmounts(updateReqVO.getAccountSetId(),
                periodData.getFromPeriod(), periodData.getToPeriod(), periodData.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CASH_FLOW_STATEMENT_TYPE,
            subType = FMS_CASH_FLOW_ADJUSTMENT_FORMULA_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_CASH_FLOW_ADJUSTMENT_FORMULA_UPDATE_SUCCESS)
    public void updateCashFlowAdjustmentFormula(FmsReportFormulaUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验现金流量辅助数据
        FmsCashFlowExtendDataDO data = cashFlowExtendDataMapper.selectById(updateReqVO.getId());
        if (data == null || ObjUtil.notEqual(data.getAccountSetId(), updateReqVO.getAccountSetId())) {
            throw exception(REPORT_ADJUSTMENT_NOT_EXISTS);
        }
        if (Boolean.FALSE.equals(data.getEditable())) {
            throw exception(REPORT_CONFIG_NOT_EDITABLE);
        }

        // 2. 校验并构造辅助数据公式
        List<FmsReportFormulaRespVO> formulas = reportCommonService.buildReportFormulaList(updateReqVO,
                FmsFormulaRuleEnum.DEBIT_AMOUNT.getRule(),
                FmsFormulaRuleEnum.PROFIT_LOSS_AMOUNT.getRule(), userId);

        // 3. 更新当前期间公式并清空人工调整金额
        cashFlowExtendDataMapper.updateById(new FmsCashFlowExtendDataDO().setId(data.getId())
                .setFormula(JsonUtils.toJsonString(formulas))
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO));
    }

    /**
     * 初始化并计算当前期间的现金流量辅助数据
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 现金流量辅助数据列表
     */
    private List<FmsCashFlowAdjustmentRespVO> getCashFlowAdjustmentListInternal(
            FmsReportListReqVO listReqVO, Long userId) {
        // 1. 初始化当前期间辅助数据
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(listReqVO.getAccountSetId(), null, userId);
        List<FmsCashFlowExtendConfigDO> configs = getOrInitCashFlowExtendConfigs(listReqVO.getAccountSetId(), subjects);
        Integer fromPeriod = reportCommonService.parsePeriod(listReqVO.getStartMonth());
        Integer toPeriod = reportCommonService.parsePeriod(listReqVO.getEndMonth());
        Integer periodType = reportCommonService.getPeriodType(fromPeriod, toPeriod);
        List<FmsCashFlowExtendDataDO> dataList = cashFlowExtendDataMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType, CASH_FLOW_CATEGORY_MAIN);
        if (CollUtil.isEmpty(dataList) && CollUtil.isNotEmpty(configs)) {
            accountSetService.lockAccountSet(listReqVO.getAccountSetId());
            dataList = cashFlowExtendDataMapper.selectListByPeriod(
                    listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType, CASH_FLOW_CATEGORY_MAIN);
            if (CollUtil.isEmpty(dataList)) {
                dataList = convertList(configs, config -> BeanUtils.toBean(config,
                        FmsCashFlowExtendDataDO.class).setId(null)
                        .setFromPeriod(fromPeriod).setToPeriod(toPeriod).setType(periodType)
                        .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO));
                cashFlowExtendDataMapper.insertBatch(dataList);
            }
        }

        // 2. 查询科目发生额
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = reportCommonService.getSubjectBalanceMap(
                listReqVO.getAccountSetId(), LocalDateTimeUtils.parseYearMonth(listReqVO.getStartMonth()),
                LocalDateTimeUtils.parseYearMonth(listReqVO.getEndMonth()), userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);

        // 3. 计算辅助数据项目
        Map<Integer, FmsCashFlowAdjustmentRespVO> lineMap = new LinkedHashMap<>();
        List<FmsCashFlowAdjustmentRespVO> result = new ArrayList<>();
        for (FmsCashFlowExtendDataDO data : dataList) {
            FmsCashFlowAdjustmentRespVO item = buildCashFlowAdjustmentItem(data, subjectMap, balanceMap, lineMap);
            if (data.getRowNo() != null && data.getRowNo() > 0) {
                lineMap.put(data.getRowNo(), item);
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 获得账套的现金流量辅助数据配置，首次查询时按报表模板初始化
     *
     * 行次公式的辅助数据标记为固定类型，科目公式标记为自定义类型；模板公式中的科目编码绑定为账套下的科目编号
     *
     * @param accountSetId 账套编号
     * @param subjects 账套下的科目列表
     * @return 辅助数据配置列表
     */
    private List<FmsCashFlowExtendConfigDO> getOrInitCashFlowExtendConfigs(Long accountSetId, List<FmsSubjectDO> subjects) {
        // 1. 已初始化时直接返回
        List<FmsCashFlowExtendConfigDO> configs = cashFlowExtendConfigMapper.selectListByAccountSetIdAndCategory(
                accountSetId, CASH_FLOW_CATEGORY_MAIN);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复初始化
        accountSetService.lockAccountSet(accountSetId);
        configs = cashFlowExtendConfigMapper.selectListByAccountSetIdAndCategory(
                accountSetId, CASH_FLOW_CATEGORY_MAIN);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 3. 按报表模板初始化，模板为空时返回空列表
        List<FmsReportTemplateDO> templates = reportTemplateMapper.selectListByType(FmsReportTypeEnum.CASH_FLOW_ADJUSTMENT.getType());
        if (CollUtil.isEmpty(templates)) {
            return Collections.emptyList();
        }
        // 4. 模板公式中的科目编码绑定为账套下的科目编号后批量插入
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode,
                Function.identity(), (first, second) -> first);
        configs = convertList(templates, template -> BeanUtils.toBean(template,
                        FmsCashFlowExtendConfigDO.class).setId(null).setAccountSetId(accountSetId)
                .setType(reportCommonService.isLineFormula(template.getFormula())
                        ? FmsCashFlowExtendTypeEnum.FIXED.getType()
                        : FmsCashFlowExtendTypeEnum.CUSTOM.getType())
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO)
                .setFormula(reportCommonService.bindSubjectFormula(template.getFormula(), subjectMap)));
        cashFlowExtendConfigMapper.insertBatch(configs);
        return configs;
    }

    /**
     * 计算辅助数据项目的本期和本年累计金额，人工调整金额非零时覆盖公式计算结果
     *
     * @param data 辅助数据
     * @param subjectMap 科目编号到科目的 Map
     * @param balanceMap 科目编号到余额的 Map
     * @param lineMap 行次到已计算辅助数据项目的 Map
     * @return 辅助数据项目
     */
    private FmsCashFlowAdjustmentRespVO buildCashFlowAdjustmentItem(
            FmsCashFlowExtendDataDO data, Map<Long, FmsSubjectDO> subjectMap,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap,
            Map<Integer, FmsCashFlowAdjustmentRespVO> lineMap) {
        FmsCashFlowAdjustmentRespVO item = new FmsCashFlowAdjustmentRespVO()
                .setId(data.getId()).setName(data.getName()).setRowNo(data.getRowNo())
                .setFormula(data.getFormula()).setRemark(data.getRemark())
                .setEditable(data.getEditable()).setLevel(data.getLevel())
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO);
        // 行次公式直接引用前面已计算的行次金额，例如 ["L1+L2"]
        if (reportCommonService.isLineFormula(data.getFormula())) {
            return item.setCurrentAmount(calculateAdjustmentLineFormula(data.getFormula(), lineMap, true))
                    .setYearAmount(calculateAdjustmentLineFormula(data.getFormula(), lineMap, false));
        }
        // 科目公式逐项取数，按运算符累加到本期、本年累计金额
        List<FmsReportFormulaRespVO> formulas = reportCommonService.parseSubjectFormula(data.getFormula());
        for (FmsReportFormulaRespVO formula : formulas) {
            FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(formula.getSubjectId());
            FmsSubjectDO subject = subjectMap.get(formula.getSubjectId());
            reportCommonService.normalizeSubjectFormula(formula, subject);
            BigDecimal currentAmount = reportCommonService.calculateOccurrenceAmount(formula.getRules(), subject, balance, true);
            BigDecimal yearAmount = reportCommonService.calculateOccurrenceAmount(formula.getRules(), subject, balance, false);
            formula.setCurrentAmount(currentAmount).setYearAmount(yearAmount);
            item.setCurrentAmount(reportCommonService.applyOperator(item.getCurrentAmount(), currentAmount, formula.getOperator()))
                    .setYearAmount(reportCommonService.applyOperator(item.getYearAmount(), yearAmount, formula.getOperator()));
        }
        // 人工调整金额非零时覆盖公式计算结果
        if (NumberUtils.zeroIfNull(data.getCurrentAmount()).signum() != 0) {
            item.setCurrentAmount(data.getCurrentAmount());
        }
        if (NumberUtils.zeroIfNull(data.getYearAmount()).signum() != 0) {
            item.setYearAmount(data.getYearAmount());
        }
        return item.setFormula(JsonUtils.toJsonString(formulas));
    }

    /**
     * 计算辅助数据的行次公式金额，例如 ["L1+L2"]
     *
     * @param formula 行次公式
     * @param lineMap 行次到已计算辅助数据项目的 Map
     * @param current 是否取本期金额，否则取本年累计金额
     * @return 金额
     */
    private BigDecimal calculateAdjustmentLineFormula(String formula, Map<Integer, FmsCashFlowAdjustmentRespVO> lineMap, boolean current) {
        // 逐个匹配 L 行次引用，按运算符累加对应行次金额
        String expression = CollUtil.getFirst(reportCommonService.parseLineFormula(formula));
        if (StrUtil.isEmpty(expression)) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        Matcher matcher = LINE_FORMULA_PATTERN.matcher(expression);
        while (matcher.find()) {
            FmsCashFlowAdjustmentRespVO item = lineMap.get(Integer.valueOf(matcher.group(2).substring(1)));
            BigDecimal amount = item == null ? BigDecimal.ZERO : current
                    ? NumberUtils.zeroIfNull(item.getCurrentAmount())
                    : NumberUtils.zeroIfNull(item.getYearAmount());
            result = reportCommonService.applyOperator(result, amount, matcher.group(1));
        }
        return result;
    }

    /**
     * 计算现金流量表项目的跨报表表达式金额
     *
     * 表达式中的 BA（资产负债取数）、IN（利润表取数）、EX（辅助数据取数）和 L（行次取数）参数
     * 替换为变量后交由 Aviator 计算；表达式含非法字符时按零处理
     *
     * @param formula 行次公式
     * @param balanceMap 资产负债行次到报表项目的 Map
     * @param incomeMap 利润表行次到报表项目的 Map
     * @param adjustmentMap 辅助数据行次到项目的 Map
     * @param lineMap 行次到已计算报表项目的 Map
     * @param current 是否取本期金额，否则取本年累计金额
     * @return 金额
     */
    private BigDecimal calculateCashFlowFormula(String formula, Map<Integer, FmsReportItemRespVO> balanceMap,
                                                Map<Integer, FmsReportItemRespVO> incomeMap,
                                                Map<Integer, FmsCashFlowAdjustmentRespVO> adjustmentMap,
                                                Map<Integer, FmsReportItemRespVO> lineMap, boolean current) {
        String expression = CollUtil.getFirst(reportCommonService.parseLineFormula(formula));
        if (StrUtil.isEmpty(expression)) {
            return BigDecimal.ZERO;
        }
        // 1. 将 BA、IN、EX、L 参数逐个替换为 Aviator 变量，并放入对应的取数金额
        Map<String, Object> environment = new HashMap<>();
        Matcher matcher = CASH_FLOW_PARAM_PATTERN.matcher(expression);
        StringBuffer normalizedExpression = new StringBuffer();
        while (matcher.find()) {
            String param = matcher.group();
            String variable = param.replace("[", "_").replace(",", "_").replace("]", "");
            environment.put(variable, getCashFlowParamValue(
                    param, balanceMap, incomeMap, adjustmentMap, lineMap, current));
            matcher.appendReplacement(normalizedExpression, variable);
        }
        matcher.appendTail(normalizedExpression);
        // 2. 校验替换后的表达式只含合法字符，避免公式被注入任意代码
        if (!CASH_FLOW_EXPRESSION_PATTERN.matcher(normalizedExpression).matches()) {
            return BigDecimal.ZERO;
        }
        // 3. 交由 Aviator 计算表达式结果
        Object value = AviatorEvaluator.execute(normalizedExpression.toString(), environment);
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    /**
     * 判断现金流量表项目是否允许调整金额
     *
     * 表头没有行次，汇总行使用 L 行次公式，两类项目都由系统自动计算
     *
     * @param report 现金流量表项目
     * @return 是否允许调整金额
     */
    private boolean isAmountAdjustable(FmsCashFlowStatementReportDO report) {
        return report.getRowNo() != null && report.getRowNo() > 0
                && !reportCommonService.isLineFormula(report.getFormula());
    }

    /**
     * 获得跨报表表达式单个参数的取数金额，参数缺失时按零处理
     *
     * @param param 参数，例如 BA[1,2]、IN32、EX4、L7
     * @param balanceMap 资产负债行次到报表项目的 Map
     * @param incomeMap 利润表行次到报表项目的 Map
     * @param adjustmentMap 辅助数据行次到项目的 Map
     * @param lineMap 行次到已计算报表项目的 Map
     * @param current 是否取本期金额，否则取本年累计金额
     * @return 金额
     */
    private BigDecimal getCashFlowParamValue(String param,
            Map<Integer, FmsReportItemRespVO> balanceMap,
            Map<Integer, FmsReportItemRespVO> incomeMap,
            Map<Integer, FmsCashFlowAdjustmentRespVO> adjustmentMap,
            Map<Integer, FmsReportItemRespVO> lineMap, boolean current) {
        FmsCashFlowParamTypeEnum paramType = FmsCashFlowParamTypeEnum.of(param);
        if (paramType == null) {
            return BigDecimal.ZERO;
        }
        // BA[行次,1期初|2期末]：资产负债行次金额
        if (paramType == FmsCashFlowParamTypeEnum.BALANCE) {
            String[] parts = param.substring(paramType.getPrefix().length() + 1, param.length() - 1).split(",");
            FmsReportItemRespVO item = balanceMap.get(Integer.valueOf(parts[0]));
            if (item == null) {
                return BigDecimal.ZERO;
            }
            return "1".equals(parts[1]) ? NumberUtils.zeroIfNull(item.getOpeningAmount())
                    : NumberUtils.zeroIfNull(item.getClosingAmount());
        }
        // IN行次：利润表行次金额
        if (paramType == FmsCashFlowParamTypeEnum.INCOME) {
            FmsReportItemRespVO item = incomeMap.get(
                    Integer.valueOf(param.substring(paramType.getPrefix().length())));
            return item == null ? BigDecimal.ZERO : current
                    ? NumberUtils.zeroIfNull(item.getCurrentAmount())
                    : NumberUtils.zeroIfNull(item.getYearAmount());
        }
        // EX行次：辅助数据行次金额
        if (paramType == FmsCashFlowParamTypeEnum.EXTEND) {
            FmsCashFlowAdjustmentRespVO item = adjustmentMap.get(
                    Integer.valueOf(param.substring(paramType.getPrefix().length())));
            return item == null ? BigDecimal.ZERO : current
                    ? NumberUtils.zeroIfNull(item.getCurrentAmount())
                    : NumberUtils.zeroIfNull(item.getYearAmount());
        }
        // L行次：本表已计算行次金额
        FmsReportItemRespVO item = lineMap.get(
                Integer.valueOf(param.substring(paramType.getPrefix().length())));
        return item == null ? BigDecimal.ZERO : current
                ? NumberUtils.zeroIfNull(item.getCurrentAmount())
                : NumberUtils.zeroIfNull(item.getYearAmount());
    }

    /**
     * 获得现金流量表金额
     *
     * 既有财务规则使用非零金额覆盖公式计算结果，保存零值时重新按公式计算
     *
     * @param savedAmount 已保存金额
     * @param calculatedAmount 公式计算金额
     * @return 现金流量表金额
     */
    private BigDecimal getSavedAmount(BigDecimal savedAmount, BigDecimal calculatedAmount) {
        return NumberUtils.zeroIfNull(savedAmount).signum() != 0 ? savedAmount : calculatedAmount;
    }

    /**
     * 将未明确归类的经营活动现金流归入其他经营活动，并重算受其影响的行次公式项目
     *
     * 其他经营活动现金 = 期末现金余额 - 资产负债表现金余额的倒挤差额，差额为正时覆盖原计算值
     *
     * @param result 现金流量表项目列表
     * @param lineMap 行次到已计算报表项目的 Map
     * @param currentBalanceMap 本期资产负债行次到报表项目的 Map
     * @param yearBalanceMap 本年资产负债行次到报表项目的 Map
     * @param adjustmentMap 辅助数据行次到项目的 Map
     */
    private void reconcileCashFlow(List<FmsReportItemRespVO> result,
            Map<Integer, FmsReportItemRespVO> lineMap,
            Map<Integer, FmsReportItemRespVO> currentBalanceMap,
            Map<Integer, FmsReportItemRespVO> yearBalanceMap,
            Map<Integer, FmsCashFlowAdjustmentRespVO> adjustmentMap,
            Map<Integer, FmsCashFlowStatementReportDO> reportMap) {
        FmsReportItemRespVO otherOperating = lineMap.get(OTHER_OPERATING_ROW_NO);
        FmsReportItemRespVO endingCash = lineMap.get(ENDING_CASH_ROW_NO);
        if (otherOperating == null || endingCash == null) {
            return;
        }
        // 1. 按资产负债表的货币资金期末余额，倒挤其他经营活动现金流
        FmsReportItemRespVO currentCashItem = currentBalanceMap.get(FmsBalanceSheetServiceImpl.MONETARY_CASH_ROW_NO);
        FmsReportItemRespVO yearCashItem = yearBalanceMap.get(FmsBalanceSheetServiceImpl.MONETARY_CASH_ROW_NO);
        BigDecimal currentCash = currentCashItem == null ? BigDecimal.ZERO
                : NumberUtils.zeroIfNull(currentCashItem.getClosingAmount());
        BigDecimal yearCash = yearCashItem == null ? BigDecimal.ZERO
                : NumberUtils.zeroIfNull(yearCashItem.getClosingAmount());
        BigDecimal currentAmount = NumberUtils.zeroIfNull(otherOperating.getCurrentAmount())
                .add(NumberUtils.zeroIfNull(endingCash.getCurrentAmount())).subtract(currentCash);
        BigDecimal yearAmount = NumberUtils.zeroIfNull(otherOperating.getYearAmount())
                .add(NumberUtils.zeroIfNull(endingCash.getYearAmount())).subtract(yearCash);
        // 2. 倒挤差额为正时，覆盖其他经营活动的原计算值
        FmsCashFlowStatementReportDO otherOperatingReport = reportMap.get(OTHER_OPERATING_ROW_NO);
        if (currentAmount.signum() > 0
                && NumberUtils.zeroIfNull(otherOperatingReport.getCurrentAmount()).signum() == 0) {
            otherOperating.setCurrentAmount(currentAmount);
        }
        if (yearAmount.signum() > 0
                && NumberUtils.zeroIfNull(otherOperatingReport.getYearAmount()).signum() == 0) {
            otherOperating.setYearAmount(yearAmount);
        }
        // 3. 重算其他经营活动之后的行次公式项目，使净额和余额联动更新
        for (FmsReportItemRespVO item : result) {
            if (item.getRowNo() == null || item.getRowNo() <= OTHER_OPERATING_ROW_NO
                    || !reportCommonService.isLineFormula(item.getFormula())) {
                continue;
            }
            item.setCurrentAmount(calculateCashFlowFormula(item.getFormula(), currentBalanceMap,
                            Collections.emptyMap(), adjustmentMap, lineMap, true))
                    .setYearAmount(calculateCashFlowFormula(item.getFormula(), yearBalanceMap,
                            Collections.emptyMap(), adjustmentMap, lineMap, false));
            lineMap.put(item.getRowNo(), item);
        }
    }

    /**
     * 获得账套的现金流量表配置，首次查询时按报表模板初始化
     *
     * @param accountSetId 账套编号
     * @return 报表配置列表
     */
    private List<FmsCashFlowStatementConfigDO> getOrInitCashFlowConfigs(Long accountSetId) {
        // 1. 已初始化时直接返回
        List<FmsCashFlowStatementConfigDO> configs = cashFlowStatementConfigMapper
                .selectListByAccountSetId(accountSetId);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复初始化
        accountSetService.lockAccountSet(accountSetId);
        configs = cashFlowStatementConfigMapper.selectListByAccountSetId(accountSetId);
        if (CollUtil.isNotEmpty(configs)) {
            return configs;
        }
        // 3. 按报表模板初始化，模板为空时返回空列表
        List<FmsReportTemplateDO> templates = reportTemplateMapper.selectListByType(
                FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType());
        if (CollUtil.isEmpty(templates)) {
            return Collections.emptyList();
        }
        // 4. 现金流量表公式使用跨报表表达式，不涉及科目绑定，直接复制模板配置
        configs = convertList(templates, template -> BeanUtils.toBean(template,
                        FmsCashFlowStatementConfigDO.class).setId(null).setAccountSetId(accountSetId));
        cashFlowStatementConfigMapper.insertBatch(configs);
        return configs;
    }

    /**
     * 获得查询期间的报表快照，首次查询该期间时按当前配置创建
     *
     * @param listReqVO 查询条件
     * @param configs 报表配置列表
     * @return 报表快照列表
     */
    private List<FmsCashFlowStatementReportDO> getOrCreateCashFlowReports(
            FmsReportListReqVO listReqVO, List<FmsCashFlowStatementConfigDO> configs) {
        // 1. 已生成快照时直接返回；配置为空时不生成
        Integer fromPeriod = reportCommonService.parsePeriod(listReqVO.getStartMonth());
        Integer toPeriod = reportCommonService.parsePeriod(listReqVO.getEndMonth());
        Integer periodType = reportCommonService.getPeriodType(fromPeriod, toPeriod);
        List<FmsCashFlowStatementReportDO> reports = cashFlowStatementReportMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        if (CollUtil.isNotEmpty(reports) || CollUtil.isEmpty(configs)) {
            return reports;
        }
        // 2. 锁定账套后二次查询，避免并发首次查询重复生成期间快照
        accountSetService.lockAccountSet(listReqVO.getAccountSetId());
        reports = cashFlowStatementReportMapper.selectListByPeriod(
                listReqVO.getAccountSetId(), fromPeriod, toPeriod, periodType);
        if (CollUtil.isNotEmpty(reports)) {
            return reports;
        }
        // 3. 按当前配置创建期间快照，金额初始化为零
        reports = convertList(configs, config -> BeanUtils.toBean(config, FmsCashFlowStatementReportDO.class)
                .setId(null).setFromPeriod(fromPeriod).setToPeriod(toPeriod).setType(periodType)
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO));
        cashFlowStatementReportMapper.insertBatch(reports);
        return reports;
    }

}

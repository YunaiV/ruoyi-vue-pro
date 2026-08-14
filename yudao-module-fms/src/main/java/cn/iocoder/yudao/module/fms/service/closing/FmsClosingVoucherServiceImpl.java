package cn.iocoder.yudao.module.fms.service.closing;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingVoucherGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerEntryVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherEntrySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingSchemeMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingVoucherMapper;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingPresetSubjectEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTimeTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingVoucherTypeEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import cn.iocoder.yudao.module.fms.util.FmsPeriodUtils;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.sumBigDecimal;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.*;

/**
 * FMS 结转凭证 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsClosingVoucherServiceImpl implements FmsClosingVoucherService {

    @Resource
    private FmsClosingVoucherMapper closingVoucherMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    @Lazy // 延迟加载，避免与结账期间 Service 循环依赖
    private FmsClosingPeriodService closingPeriodService;
    @Resource
    @Lazy // 延迟加载，避免与结账方案 Service 循环依赖
    private FmsClosingSchemeService closingSchemeService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsLedgerService ledgerService;
    @Resource
    @Lazy // 延迟加载，避免与凭证 Service 循环依赖
    private FmsVoucherService voucherService;
    @Resource
    private FmsVoucherWordService voucherWordService;
    @Resource
    private FmsIncomeStatementService incomeStatementService;

    @Override
    public List<FmsClosingVoucherDO> getClosingVoucherListByPeriod(Long accountSetId,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return closingVoucherMapper.selectListByPeriod(accountSetId, beginTime, endTime);
    }

    @Override
    public List<FmsClosingVoucherDO> getClosingVoucherListByClosingIdAndPeriod(Long closingId,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return closingVoucherMapper.selectListByClosingIdAndVoucherTimeBetween(closingId, beginTime, endTime);
    }

    @Override
    public void updateClosingVoucherClosedByPeriod(Long accountSetId, LocalDateTime beginTime,
            LocalDateTime endTime, Boolean closed) {
        List<FmsClosingVoucherDO> closingVouchers = getClosingVoucherListByPeriod(
                accountSetId, beginTime, endTime);
        closingVouchers.forEach(item -> closingVoucherMapper.updateById(new FmsClosingVoucherDO()
                .setId(item.getId()).setClosed(closed)));
    }

    @Override
    public Long getClosingVoucherCountByClosingId(Long closingId) {
        return closingVoucherMapper.selectCountByClosingId(closingId);
    }

    @Override
    public Set<Long> getClosingVoucherIdSet(Long accountSetId, Collection<Long> voucherIds) {
        if (CollUtil.isEmpty(voucherIds)) {
            return Collections.emptySet();
        }
        List<FmsClosingVoucherDO> closingVouchers = closingVoucherMapper.selectListByAccountSetIdAndVoucherIds(
                accountSetId, voucherIds);
        return convertSet(closingVouchers, FmsClosingVoucherDO::getVoucherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_VOUCHER_GENERATE_SUB_TYPE,
            bizNo = "{{#generateReqVO.accountSetId}}", success = FMS_CLOSING_PROFIT_LOSS_VOUCHER_GENERATE_SUCCESS)
    public Long generateProfitLossVoucher(FmsProfitLossGenerateReqVO generateReqVO, Long userId) {
        // 1.1 校验当前会计期间
        validateCurrentPeriod(generateReqVO.getAccountSetId(), generateReqVO.getMonth(), userId);
        // 1.2 校验期间未结账
        YearMonth month = LocalDateTimeUtils.parseYearMonth(generateReqVO.getMonth());
        if (closingPeriodService.isPeriodClosed(generateReqVO.getAccountSetId(), generateReqVO.getMonth())) {
            throw exception(CLOSING_PERIOD_CLOSED);
        }
        // 1.3 校验结转损益设置
        FmsClosingSchemeDO closing = closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                generateReqVO.getAccountSetId(), FmsClosingTypeEnum.PROFIT_LOSS.getType());
        if (closing == null) {
            throw exception(CLOSING_PROFIT_LOSS_SETTINGS_NOT_EXISTS);
        }
        voucherWordService.validateVoucherWordExists(generateReqVO.getAccountSetId(), closing.getVoucherWordId());
        List<FmsClosingVoucherDO> existingVouchers = closingVoucherMapper.selectListByClosingIdAndPeriod(
                closing.getId(), LocalDateTimeUtils.getMonthBeginTime(month),
                LocalDateTimeUtils.endOfMonth(LocalDateTimeUtils.getMonthBeginTime(month)));
        deleteExistingClosingVouchers(
                generateReqVO.getAccountSetId(), existingVouchers, userId);
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(
                generateReqVO.getAccountSetId(), null, userId);
        Set<Long> parentIds = subjects.stream().map(FmsSubjectDO::getParentId)
                .filter(id -> id != null).collect(Collectors.toSet());
        validateProfitLossSubjects(closing, subjects, parentIds);

        // 2. 计算末级损益科目余额并按凭证分类构造分录
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = getSubjectBalanceMap(
                generateReqVO.getAccountSetId(), month, userId);
        LocalDateTime monthBeginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        List<FmsLedgerEntryVO> ledgerEntries = ledgerService.getEntryList(
                generateReqVO.getAccountSetId(), monthBeginTime, monthBeginTime.plusMonths(1), userId);
        List<List<FmsVoucherEntrySaveReqVO>> voucherEntriesList = new ArrayList<>();
        if (ObjUtil.equal(closing.getVoucherType(),
                FmsClosingVoucherTypeEnum.SEPARATE_GAIN_AND_LOSS.getType())) {
            List<FmsVoucherEntrySaveReqVO> gainEntries = buildProfitLossEntries(
                    closing, subjects, parentIds, balanceMap, ledgerEntries, true);
            List<FmsVoucherEntrySaveReqVO> lossEntries = buildProfitLossEntries(
                    closing, subjects, parentIds, balanceMap, ledgerEntries, false);
            if (CollUtil.isNotEmpty(gainEntries)) {
                voucherEntriesList.add(gainEntries);
            }
            if (CollUtil.isNotEmpty(lossEntries)) {
                voucherEntriesList.add(lossEntries);
            }
        } else {
            List<FmsVoucherEntrySaveReqVO> entries = buildProfitLossEntries(
                    closing, subjects, parentIds, balanceMap, ledgerEntries, null);
            if (CollUtil.isNotEmpty(entries)) {
                voucherEntriesList.add(entries);
            }
        }
        if (CollUtil.isEmpty(voucherEntriesList)) {
            throw exception(CLOSING_NO_PROFIT_LOSS_BALANCE);
        }

        // 3. 创建结转凭证并登记关联
        LocalDateTime voucherTime = month.atDay(Math.min(closing.getClosingDay(), month.lengthOfMonth()))
                .atStartOfDay();
        Long firstVoucherId = null;
        for (List<FmsVoucherEntrySaveReqVO> entries : voucherEntriesList) {
            FmsVoucherSaveReqVO voucherReqVO = new FmsVoucherSaveReqVO();
            voucherReqVO.setAccountSetId(generateReqVO.getAccountSetId());
            voucherReqVO.setVoucherWordId(closing.getVoucherWordId());
            voucherReqVO.setVoucherTime(voucherTime);
            voucherReqVO.setVoucherNumber(voucherService.getNextVoucherNumber(
                    generateReqVO.getAccountSetId(), closing.getVoucherWordId(), voucherTime, userId));
            voucherReqVO.setEntries(entries);
            Long voucherId = voucherService.createVoucher(voucherReqVO, userId);
            if (firstVoucherId == null) {
                firstVoucherId = voucherId;
            }
            closingVoucherMapper.insert(new FmsClosingVoucherDO().setClosingId(closing.getId())
                    .setVoucherId(voucherId).setVoucherTime(voucherTime)
                    .setAmount(entries.stream().map(FmsVoucherEntrySaveReqVO::getDebitAmount)
                            .map(NumberUtils::zeroIfNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .setClosed(false)
                    .setAccountSetId(generateReqVO.getAccountSetId()));
        }
        return firstVoucherId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_VOUCHER_GENERATE_SUB_TYPE,
            bizNo = "{{#generateReqVO.accountSetId}}", success = FMS_CLOSING_SCHEME_VOUCHER_GENERATE_SUCCESS)
    public Long generateClosingSchemeVoucher(
            FmsClosingSchemeGenerateReqVO generateReqVO, Long userId) {
        // 1.1 校验当前会计期间
        FmsAccountSetDO accountSet = validateCurrentPeriod(
                generateReqVO.getAccountSetId(), generateReqVO.getMonth(), userId);
        // 1.2 校验期间未结账
        if (closingPeriodService.isPeriodClosed(generateReqVO.getAccountSetId(), generateReqVO.getMonth())) {
            throw exception(CLOSING_PERIOD_CLOSED);
        }
        // 1.3 校验结账方案
        FmsClosingSchemeDO closing = closingSchemeService.validateClosingSchemeExists(
                generateReqVO.getAccountSetId(), generateReqVO.getId());
        if (!ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.REGULAR.getType())
                && !isSpecialClosing(closing)) {
            throw exception(CLOSING_SCHEME_NOT_EXISTS);
        }
        validatePeriodEndClosing(closing);
        voucherWordService.validateVoucherWordExists(
                generateReqVO.getAccountSetId(), closing.getVoucherWordId());
        YearMonth month = LocalDateTimeUtils.parseYearMonth(generateReqVO.getMonth());

        // 2. 重新生成时先删除当前期间的旧凭证和关联
        LocalDateTime monthBeginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        List<FmsClosingVoucherDO> existingVouchers = closingVoucherMapper.selectListByClosingIdAndPeriod(
                closing.getId(), monthBeginTime, LocalDateTimeUtils.endOfMonth(monthBeginTime));
        deleteExistingClosingVouchers(generateReqVO.getAccountSetId(), existingVouchers, userId);

        // 3. 计算结转金额并校验非零
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = ObjUtil.equal(closing.getTimeType(), FmsClosingTimeTypeEnum.YEAR_BEGIN.getType())
                ? getSubjectBalanceMap(generateReqVO.getAccountSetId(),
                        FmsPeriodUtils.getYearStartMonth(accountSet, month), month, userId)
                : getSubjectBalanceMap(generateReqVO.getAccountSetId(), month, userId);
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(generateReqVO.getAccountSetId(), null, userId);
        BigDecimal balance = isSpecialClosing(closing)
                ? calculateSpecialClosingBalance(closing, generateReqVO.getAccountSetId(), month, subjects, balanceMap, userId)
                : calculateClosingSchemeBalance(closing, balanceMap);
        if (balance.signum() == 0) {
            throw exception(CLOSING_SCHEME_NO_BALANCE);
        }

        // 4. 按 JSON 科目规则构造凭证分录
        List<FmsVoucherEntrySaveReqVO> entries = isSpecialClosing(closing)
                ? buildSpecialClosingEntries(closing, balance)
                : buildRegularClosingEntries(closing, balance);

        // 5. 创建凭证并登记方案关联
        LocalDateTime voucherTime = month.atEndOfMonth().atStartOfDay();
        Long voucherId = voucherService.createVoucher(new FmsVoucherSaveReqVO()
                .setAccountSetId(generateReqVO.getAccountSetId())
                .setVoucherWordId(closing.getVoucherWordId())
                .setVoucherTime(voucherTime)
                .setVoucherNumber(voucherService.getNextVoucherNumber(generateReqVO.getAccountSetId(),
                        closing.getVoucherWordId(), voucherTime, userId))
                .setEntries(entries), userId);
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setClosingId(closing.getId())
                .setVoucherId(voucherId).setVoucherTime(voucherTime)
                .setAmount(balance).setClosed(false).setAccountSetId(generateReqVO.getAccountSetId()));
        return voucherId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_VOUCHER_GENERATE_SUB_TYPE,
            bizNo = "{{#generateReqVO.accountSetId}}", success = FMS_CLOSING_VOUCHER_BATCH_GENERATE_SUCCESS)
    public List<Long> generateClosingVoucherList(
            FmsClosingVoucherGenerateReqVO generateReqVO, Long userId) {
        // 1.1 校验当前会计期间
        validateCurrentPeriod(generateReqVO.getAccountSetId(), generateReqVO.getMonth(), userId);
        // 1.2 校验期间未结账
        if (closingPeriodService.isPeriodClosed(generateReqVO.getAccountSetId(), generateReqVO.getMonth())) {
            throw exception(CLOSING_PERIOD_CLOSED);
        }
        // 1.3 校验结账方案
        List<FmsClosingSchemeDO> closings = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(generateReqVO.getIds())) {
            FmsClosingSchemeDO closing = closingSchemeService.validateClosingSchemeExists(
                    generateReqVO.getAccountSetId(), id);
            if (ObjUtil.notEqual(closing.getType(), FmsClosingTypeEnum.PROFIT_LOSS.getType())
                    && ObjUtil.notEqual(closing.getType(), FmsClosingTypeEnum.REGULAR.getType())
                    && !isSpecialClosing(closing)) {
                throw exception(CLOSING_SCHEME_NOT_EXISTS);
            }
            validatePeriodEndClosing(closing);
            closings.add(closing);
        }

        // 2. 统一删除本批方案的旧结转凭证，避免逐个重生成后删除旧凭证造成凭证号断号
        YearMonth month = LocalDateTimeUtils.parseYearMonth(generateReqVO.getMonth());
        LocalDateTime monthBeginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        List<FmsClosingVoucherDO> existingVouchers = new ArrayList<>();
        for (FmsClosingSchemeDO closing : closings) {
            existingVouchers.addAll(closingVoucherMapper.selectListByClosingIdAndPeriod(
                    closing.getId(), monthBeginTime, LocalDateTimeUtils.endOfMonth(monthBeginTime)));
        }
        deleteExistingClosingVouchers(generateReqVO.getAccountSetId(), existingVouchers, userId);

        // 3. 在同一事务中生成全部结转凭证
        List<Long> voucherIds = new ArrayList<>();
        for (FmsClosingSchemeDO closing : closings) {
            boolean profitLoss = ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.PROFIT_LOSS.getType());
            Integer noBalanceErrorCode = profitLoss ? CLOSING_NO_PROFIT_LOSS_BALANCE.getCode() : CLOSING_SCHEME_NO_BALANCE.getCode();
            try {
                if (profitLoss) {
                    FmsProfitLossGenerateReqVO profitLossReqVO = BeanUtils.toBean(generateReqVO, FmsProfitLossGenerateReqVO.class);
                    voucherIds.add(generateProfitLossVoucher(profitLossReqVO, userId));
                } else {
                    FmsClosingSchemeGenerateReqVO schemeReqVO = BeanUtils.toBean(generateReqVO, FmsClosingSchemeGenerateReqVO.class);
                    schemeReqVO.setId(closing.getId());
                    voucherIds.add(generateClosingSchemeVoucher(schemeReqVO, userId));
                }
            } catch (ServiceException ex) {
                // 批量生成时，余额为零的方案无需生成凭证，继续处理其他方案
                if (ObjUtil.notEqual(ex.getCode(), noBalanceErrorCode)) {
                    throw ex;
                }
            }
        }
        return voucherIds;
    }

    /**
     * 删除方案当前期间的已生成结转凭证及其关联
     *
     * @param accountSetId 账套编号
     * @param closingVouchers 结账凭证关联列表
     * @param userId 用户编号
     */
    private void deleteExistingClosingVouchers(Long accountSetId, List<FmsClosingVoucherDO> closingVouchers, Long userId) {
        if (CollUtil.isEmpty(closingVouchers)) {
            return;
        }
        List<Long> voucherIds = convertList(closingVouchers, FmsClosingVoucherDO::getVoucherId);
        closingVoucherMapper.deleteByIds(convertList(closingVouchers, FmsClosingVoucherDO::getId));
        voucherService.deleteVoucherList(accountSetId, voucherIds, userId);
    }

    /**
     * 计算专用结转方案的结转金额
     *
     * 转出未交增值税取应交增值税期末贷方净额；计提地税按应交增值税和消费税合计乘以比例；
     * 计提所得税按利润总额减已计提所得税后乘以比例
     *
     * @param closing 结账方案
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param subjects 账套下的科目列表
     * @param balanceMap 科目编号到余额的 Map
     * @param userId 用户编号
     * @return 结转金额
     */
    private BigDecimal calculateSpecialClosingBalance(FmsClosingSchemeDO closing, Long accountSetId,
            YearMonth month, List<FmsSubjectDO> subjects,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap, Long userId) {
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode,
                Function.identity(), (first, second) -> first);
        if (ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.UNPAID_VAT.getType())) {
            return getPositiveCreditEndingBalance(subjectMap.get(FmsClosingPresetSubjectEnum.VAT_PAYABLE.getCode()), balanceMap);
        }
        BigDecimal rate = getDirectionRatio(
                closing.getSubjectRules(), FmsDebitCreditDirectionEnum.DEBIT.getType());
        if (ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.LOCAL_TAX.getType())) {
            BigDecimal taxBase = getPositiveCreditEndingBalance(
                    subjectMap.get(FmsClosingPresetSubjectEnum.VAT_PAYABLE.getCode()), balanceMap)
                    .add(getPositiveCreditEndingBalance(
                            subjectMap.get(FmsClosingPresetSubjectEnum.CONSUMPTION_TAX.getCode()), balanceMap));
            return taxBase.multiply(rate).divide(NumberUtils.ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        }
        if (ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.INCOME_TAX.getType())) {
            FmsReportListReqVO listReqVO = new FmsReportListReqVO();
            listReqVO.setAccountSetId(accountSetId);
            listReqVO.setStartMonth(month.toString());
            listReqVO.setEndMonth(month.toString());
            List<FmsReportItemRespVO> reportItems = incomeStatementService.getIncomeStatement(listReqVO, userId);
            Map<Integer, FmsReportItemRespVO> reportItemMap = convertMap(
                    filterList(reportItems, item -> item.getRowNo() != null),
                    FmsReportItemRespVO::getRowNo, Function.identity(), (first, second) -> first);
            BigDecimal totalProfit = reportItemMap.containsKey(30)
                    ? NumberUtils.zeroIfNull(reportItemMap.get(30).getYearAmount()) : BigDecimal.ZERO;
            BigDecimal accruedIncomeTax = reportItemMap.containsKey(31)
                    ? NumberUtils.zeroIfNull(reportItemMap.get(31).getYearAmount()) : BigDecimal.ZERO;
            return totalProfit.max(BigDecimal.ZERO).multiply(rate)
                    .divide(NumberUtils.ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                    .subtract(accruedIncomeTax.max(BigDecimal.ZERO)).max(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获得科目期末贷方净额，借方大于贷方时按零处理
     *
     * @param subject 科目
     * @param balanceMap 科目编号到余额的 Map
     * @return 期末贷方净额
     */
    private BigDecimal getPositiveCreditEndingBalance(FmsSubjectDO subject,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap) {
        // 预置科目不存在时直接抛错，避免按错误科目结转
        if (subject == null) {
            throw exception(CLOSING_SPECIAL_SUBJECT_NOT_EXISTS);
        }
        FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(subject.getId());
        return balance == null ? BigDecimal.ZERO : NumberUtils.zeroIfNull(balance.getEndingCreditAmount())
                .subtract(NumberUtils.zeroIfNull(balance.getEndingDebitAmount())).max(BigDecimal.ZERO);
    }

    /**
     * 校验结转损益的三个科目：调整科目必须为末级损益科目，结转科目必须为末级非损益科目
     *
     * @param closing 结账方案
     * @param subjects 账套下的科目列表
     * @param parentIds 父级科目编号集合
     */
    private void validateProfitLossSubjects(FmsClosingSchemeDO closing,
            List<FmsSubjectDO> subjects, Set<Long> parentIds) {
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        FmsSubjectDO adjustmentSubject = subjectMap.get(closing.getPriorYearAdjustmentSubjectId());
        FmsSubjectDO adjustmentClosingSubject = subjectMap.get(closing.getAdjustmentClosingSubjectId());
        FmsSubjectDO otherClosingSubject = subjectMap.get(closing.getOtherClosingSubjectId());
        if (adjustmentSubject == null
                || !ObjUtil.equal(adjustmentSubject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                || parentIds.contains(adjustmentSubject.getId())
                || adjustmentClosingSubject == null
                || parentIds.contains(adjustmentClosingSubject.getId())
                || ObjUtil.equal(adjustmentClosingSubject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                || otherClosingSubject == null
                || parentIds.contains(otherClosingSubject.getId())
                || ObjUtil.equal(otherClosingSubject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())) {
            throw exception(CLOSING_PROFIT_LOSS_SETTINGS_INVALID);
        }
    }

    /**
     * 构造结转损益凭证分录
     *
     * 逐个末级损益科目按余额方向生成结转分录，并汇总生成目标结转科目分录；
     * gain 为 true 时只生成收益科目，false 时只生成损失科目，null 时合并生成
     *
     * @param closing 结账方案
     * @param subjects 账套下的科目列表
     * @param parentIds 父级科目编号集合
     * @param balanceMap 科目编号到余额的 Map
     * @param ledgerEntries 当前期间凭证分录列表
     * @param gain 是否收益科目，null 表示全部
     * @return 凭证分录列表
     */
    private List<FmsVoucherEntrySaveReqVO> buildProfitLossEntries(FmsClosingSchemeDO closing,
            List<FmsSubjectDO> subjects, Set<Long> parentIds,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap,
            List<FmsLedgerEntryVO> ledgerEntries, Boolean gain) {
        List<FmsVoucherEntrySaveReqVO> entries = new ArrayList<>();
        Map<Long, BigDecimal> targetAmountMap = new LinkedHashMap<>();
        String digest = closing.getDigest();
        boolean reverseBalance = closing.getReverseBalance() == null || closing.getReverseBalance();
        // 1. 逐个末级损益科目生成结转分录，跳过无余额和不属于当前收益损失分类的科目
        for (FmsSubjectDO subject : subjects) {
            if (ObjUtil.notEqual(subject.getType(), FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                    || parentIds.contains(subject.getId())) {
                continue;
            }
            FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(subject.getId());
            BigDecimal amount = balance == null ? BigDecimal.ZERO
                    : NumberUtils.zeroIfNull(balance.getPeriodDebitAmount())
                    .subtract(NumberUtils.zeroIfNull(balance.getPeriodCreditAmount()));
            if (amount.signum() == 0
                    || gain != null && gain != (amount.signum() < 0)) {
                continue;
            }
            Long targetSubjectId = ObjUtil.equal(subject.getId(),
                    closing.getPriorYearAdjustmentSubjectId())
                    ? closing.getAdjustmentClosingSubjectId() : closing.getOtherClosingSubjectId();
            Map<List<FmsVoucherEntryDO.AuxiliaryItem>, BigDecimal> auxiliaryAmountMap =
                    buildProfitLossAuxiliaryAmountMap(subject, amount, ledgerEntries);
            auxiliaryAmountMap.forEach((auxiliaries, auxiliaryAmount) -> {
                if (auxiliaryAmount.signum() == 0) {
                    return;
                }
                boolean normalCredit = ObjUtil.equal(subject.getBalanceDirection(),
                        FmsDebitCreditDirectionEnum.CREDIT.getType());
                boolean abnormalBalance = normalCredit && auxiliaryAmount.signum() > 0
                        || !normalCredit && auxiliaryAmount.signum() < 0;
                boolean sourceDebit = reverseBalance || abnormalBalance
                        ? auxiliaryAmount.signum() < 0 : normalCredit;
                BigDecimal transferAmount = auxiliaryAmount.abs();
                FmsVoucherEntrySaveReqVO entry = buildEntry(digest, subject.getId(),
                        sourceDebit ? transferAmount : BigDecimal.ZERO,
                        sourceDebit ? BigDecimal.ZERO : transferAmount);
                entry.setAuxiliaries(convertList(auxiliaries, auxiliary ->
                        new FmsVoucherEntrySaveReqVO.AuxiliaryItem()
                                .setTypeId(auxiliary.getTypeId()).setItemId(auxiliary.getItemId())));
                entries.add(entry);
                targetAmountMap.merge(targetSubjectId,
                        sourceDebit ? transferAmount.negate() : transferAmount, BigDecimal::add);
            });
        }
        // 2. 按目标结转科目汇总生成分录
        targetAmountMap.forEach((subjectId, amount) -> {
            if (amount.signum() != 0) {
                entries.add(buildEntry(digest, subjectId,
                        amount.signum() > 0 ? amount : BigDecimal.ZERO,
                        amount.signum() < 0 ? amount.abs() : BigDecimal.ZERO));
            }
        });
        return entries;
    }

    /**
     * 按辅助核算组合汇总损益科目发生额
     *
     * 未启用辅助核算时直接返回科目汇总发生额；启用辅助核算时保留原分录的辅助核算组合，保证生成的结转分录满足科目辅助核算校验，并将每个辅助核算项目一并结平
     *
     * @param subject 损益科目
     * @param amount 科目发生额
     * @param ledgerEntries 当前期间凭证分录列表
     * @return 辅助核算组合和发生额 Map
     */
    private Map<List<FmsVoucherEntryDO.AuxiliaryItem>, BigDecimal> buildProfitLossAuxiliaryAmountMap(
            FmsSubjectDO subject, BigDecimal amount, List<FmsLedgerEntryVO> ledgerEntries) {
        if (CollUtil.isEmpty(subject.getAuxiliaryTypeIds())) {
            return Collections.singletonMap(Collections.emptyList(), amount);
        }
        Map<List<FmsVoucherEntryDO.AuxiliaryItem>, BigDecimal> amountMap = new LinkedHashMap<>();
        for (FmsLedgerEntryVO entry : ledgerEntries) {
            if (ObjUtil.notEqual(entry.getSubjectId(), subject.getId())) {
                continue;
            }
            amountMap.merge(entry.getAuxiliaries(), NumberUtils.zeroIfNull(entry.getDebitAmount())
                            .subtract(NumberUtils.zeroIfNull(entry.getCreditAmount())), BigDecimal::add);
        }
        return amountMap;
    }

    /**
     * 计算常规结账方案的来源科目结转金额
     *
     * 按取数时间类型选择期初、期末、本期或本年累计口径，再按取数规则返回余额或发生额
     *
     * @param closing 结账方案
     * @param balanceMap 科目编号到余额的 Map
     * @return 结转金额
     */
    private BigDecimal calculateClosingSchemeBalance(FmsClosingSchemeDO closing,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap) {
        FmsLedgerSubjectBalanceRespVO balance = balanceMap.get(closing.getSubjectId());
        if (balance == null) {
            return BigDecimal.ZERO;
        }
        // 期初、年初或期末余额口径
        boolean openingBalance = ObjectUtils.equalsAny(closing.getTimeType(),
                FmsClosingTimeTypeEnum.PERIOD_BEGIN.getType(), FmsClosingTimeTypeEnum.YEAR_BEGIN.getType());
        BigDecimal debitBalance = openingBalance
                ? NumberUtils.zeroIfNull(balance.getOpeningDebitAmount())
                : NumberUtils.zeroIfNull(balance.getEndingDebitAmount());
        BigDecimal creditBalance = openingBalance
                ? NumberUtils.zeroIfNull(balance.getOpeningCreditAmount())
                : NumberUtils.zeroIfNull(balance.getEndingCreditAmount());
        if (ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.BALANCE.getRule())) {
            return debitBalance.subtract(creditBalance).abs();
        }
        if (ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                || ObjUtil.equal(closing.getFormulaRule(),
                        FmsFormulaRuleEnum.SUBJECT_DEBIT_BALANCE.getRule())) {
            return debitBalance;
        }
        if (ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.CREDIT_BALANCE.getRule())
                || ObjUtil.equal(closing.getFormulaRule(),
                        FmsFormulaRuleEnum.SUBJECT_CREDIT_BALANCE.getRule())) {
            return creditBalance;
        }
        // 本期或本年累计发生额口径
        BigDecimal debitAmount = ObjUtil.equal(closing.getTimeType(),
                FmsClosingTimeTypeEnum.YEAR_BEGIN.getType())
                ? NumberUtils.zeroIfNull(balance.getYearDebitAmount())
                : NumberUtils.zeroIfNull(balance.getPeriodDebitAmount());
        BigDecimal creditAmount = ObjUtil.equal(closing.getTimeType(),
                FmsClosingTimeTypeEnum.YEAR_BEGIN.getType())
                ? NumberUtils.zeroIfNull(balance.getYearCreditAmount())
                : NumberUtils.zeroIfNull(balance.getPeriodCreditAmount());
        if (ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.DEBIT_AMOUNT.getRule())) {
            return debitAmount;
        }
        if (ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.CREDIT_AMOUNT.getRule())) {
            return creditAmount;
        }
        return debitAmount.subtract(creditAmount).abs();
    }

    /**
     * 按科目规则的金额比例构造常规结账凭证分录
     *
     * @param closing 结账方案
     * @param balance 结转金额
     * @return 凭证分录列表
     */
    private List<FmsVoucherEntrySaveReqVO> buildRegularClosingEntries(
            FmsClosingSchemeDO closing, BigDecimal balance) {
        // 逐条科目规则按比例分摊结转金额
        List<FmsVoucherEntrySaveReqVO> entries = new ArrayList<>();
        for (FmsClosingSchemeDO.SubjectRule subjectRule : closing.getSubjectRules()) {
            BigDecimal amount = balance.multiply(subjectRule.getAmountRatio())
                    .divide(NumberUtils.ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            entries.add(buildEntry(subjectRule.getDigest(), subjectRule.getSubjectId(),
                    ObjUtil.equal(subjectRule.getDirection(), FmsDebitCreditDirectionEnum.DEBIT.getType())
                            ? amount : BigDecimal.ZERO,
                    ObjUtil.equal(subjectRule.getDirection(), FmsDebitCreditDirectionEnum.CREDIT.getType())
                            ? amount : BigDecimal.ZERO));
        }
        return entries;
    }

    /**
     * 按科目规则的金额比例构造专用结转凭证分录
     *
     * 借贷两侧分别按比例分摊，每侧最后一条分录倒挤剩余金额，保证合计等于结转金额
     *
     * @param closing 结账方案
     * @param balance 结转金额
     * @return 凭证分录列表
     */
    private List<FmsVoucherEntrySaveReqVO> buildSpecialClosingEntries(
            FmsClosingSchemeDO closing, BigDecimal balance) {
        BigDecimal debitRatio = getDirectionRatio(
                closing.getSubjectRules(), FmsDebitCreditDirectionEnum.DEBIT.getType());
        BigDecimal creditRatio = getDirectionRatio(
                closing.getSubjectRules(), FmsDebitCreditDirectionEnum.CREDIT.getType());
        int debitRemainingCount = (int) closing.getSubjectRules().stream()
                .filter(item -> ObjUtil.equal(item.getDirection(),
                        FmsDebitCreditDirectionEnum.DEBIT.getType())).count();
        int creditRemainingCount = closing.getSubjectRules().size() - debitRemainingCount;
        BigDecimal allocatedDebitAmount = BigDecimal.ZERO;
        BigDecimal allocatedCreditAmount = BigDecimal.ZERO;
        List<FmsVoucherEntrySaveReqVO> entries = new ArrayList<>();
        for (FmsClosingSchemeDO.SubjectRule subjectRule : closing.getSubjectRules()) {
            boolean debit = ObjUtil.equal(subjectRule.getDirection(),
                    FmsDebitCreditDirectionEnum.DEBIT.getType());
            BigDecimal amount;
            if (debit) {
                debitRemainingCount--;
                amount = debitRemainingCount == 0 ? balance.subtract(allocatedDebitAmount)
                        : balance.multiply(subjectRule.getAmountRatio())
                                .divide(debitRatio, 2, RoundingMode.HALF_UP);
                allocatedDebitAmount = allocatedDebitAmount.add(amount);
            } else {
                creditRemainingCount--;
                amount = creditRemainingCount == 0 ? balance.subtract(allocatedCreditAmount)
                        : balance.multiply(subjectRule.getAmountRatio())
                                .divide(creditRatio, 2, RoundingMode.HALF_UP);
                allocatedCreditAmount = allocatedCreditAmount.add(amount);
            }
            entries.add(buildEntry(subjectRule.getDigest(), subjectRule.getSubjectId(),
                    debit ? amount : BigDecimal.ZERO, debit ? BigDecimal.ZERO : amount));
        }
        return entries;
    }

    /**
     * 汇总科目规则中指定借贷方向的金额比例
     *
     * @param subjectRules 科目规则
     * @param direction 借贷方向
     * @return 金额比例合计
     */
    private BigDecimal getDirectionRatio(List<FmsClosingSchemeDO.SubjectRule> subjectRules, Integer direction) {
        return sumBigDecimal(filterList(subjectRules,
                        item -> ObjUtil.equal(item.getDirection(), direction)),
                FmsClosingSchemeDO.SubjectRule::getAmountRatio);
    }

    /**
     * 判断是否为专用结转方案（转出未交增值税、计提地税、计提所得税）
     *
     * @param closing 结账方案
     * @return 是否专用结转方案
     */
    private boolean isSpecialClosing(FmsClosingSchemeDO closing) {
        return ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.UNPAID_VAT.getType())
                || ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.LOCAL_TAX.getType())
                || ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.INCOME_TAX.getType());
    }

    /**
     * 获得单个会计期间的科目余额 Map
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     * @return 科目编号到余额的 Map
     */
    private Map<Long, FmsLedgerSubjectBalanceRespVO> getSubjectBalanceMap(
            Long accountSetId, YearMonth month, Long userId) {
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
    private Map<Long, FmsLedgerSubjectBalanceRespVO> getSubjectBalanceMap(
            Long accountSetId, YearMonth startMonth, YearMonth endMonth, Long userId) {
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
     * 构造凭证分录
     *
     * @param digest 摘要
     * @param subjectId 科目编号
     * @param debitAmount 借方金额
     * @param creditAmount 贷方金额
     * @return 凭证分录
     */
    private FmsVoucherEntrySaveReqVO buildEntry(String digest, Long subjectId, BigDecimal debitAmount, BigDecimal creditAmount) {
        return new FmsVoucherEntrySaveReqVO().setDigest(digest).setSubjectId(subjectId)
                .setDebitAmount(debitAmount).setCreditAmount(creditAmount);
    }

    /**
     * 校验常规方案已启用期末结转
     *
     * @param closing 结账方案
     */
    private void validatePeriodEndClosing(FmsClosingSchemeDO closing) {
        if (ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.REGULAR.getType())
                && !Boolean.TRUE.equals(closing.getPeriodEnd())) {
            throw exception(CLOSING_SCHEME_NOT_PERIOD_END);
        }
    }

    /**
     * 校验账套写权限、初始化状态，且指定期间为当前会计期间
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     * @return 已校验的账套
     */
    private FmsAccountSetDO validateCurrentPeriod(Long accountSetId, String month, Long userId) {
        // 1. 校验账套写权限并锁定账套，避免同一账套并发生成结转凭证
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        accountSetService.lockAccountSet(accountSetId);

        // 2. 校验账套已完成初始化
        if (ObjUtil.notEqual(accountSet.getInitialized(), true) || accountSet.getStartTime() == null) {
            throw exception(ACCOUNT_SET_NOT_INITIALIZED);
        }

        // 3. 校验指定期间为账套当前会计期间
        String currentMonth = closingPeriodService.getCurrentMonth(accountSetId, accountSet.getStartTime()).toString();
        if (!ObjUtil.equal(currentMonth, month)) {
            throw exception(CLOSING_NOT_CURRENT_PERIOD);
        }
        return accountSet;
    }

}

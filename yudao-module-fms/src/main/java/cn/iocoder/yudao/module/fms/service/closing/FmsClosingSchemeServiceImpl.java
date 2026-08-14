package cn.iocoder.yudao.module.fms.service.closing;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossSettingsSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsSpecialClosingSettingsSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
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
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import cn.iocoder.yudao.module.fms.util.FmsPeriodUtils;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * FMS 结账方案 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsClosingSchemeServiceImpl implements FmsClosingSchemeService {

    private static final String PRESET_RESOURCE = "fms/closing/closing-scheme-presets.json";

    @Resource
    private FmsClosingSchemeMapper closingSchemeMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsClosingVoucherService closingVoucherService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsFinanceParameterService financeParameterService;
    @Resource
    private FmsLedgerService ledgerService;
    @Resource
    private FmsVoucherWordService voucherWordService;
    @Resource
    private FmsIncomeStatementService incomeStatementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeDefaultClosingSchemes(Long accountSetId, Long userId) {
        // 1.1 加载并校验预置方案
        List<SchemePreset> presets = loadSchemePresets();
        validateSchemePresets(presets);
        // 1.2 查询尚未初始化的预置结账类型
        Set<Integer> existingTypes = convertSet(
                closingSchemeMapper.selectListByAccountSetId(accountSetId), FmsClosingSchemeDO::getType);
        List<SchemePreset> pendingPresets = filterList(presets, preset -> !existingTypes.contains(
                FmsClosingTypeEnum.valueOf(preset.getClosingType()).getType()));
        if (CollUtil.isEmpty(pendingPresets)) {
            return;
        }

        // 2. 查询默认凭证字和预置科目
        List<FmsVoucherWordDO> voucherWords = voucherWordService.getVoucherWordList(accountSetId);
        FmsVoucherWordDO voucherWord = CollUtil.findOne(voucherWords,
                item -> Boolean.TRUE.equals(item.getDefaultStatus()));
        if (voucherWord == null) {
            voucherWord = CollUtil.getFirst(voucherWords);
        }
        if (voucherWord == null) {
            throw exception(CLOSING_SPECIAL_SETTINGS_INVALID);
        }
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Map<String, FmsSubjectDO> subjectMap = buildPresetSubjectMap(accountSetId, subjects);

        // 3. 创建预置结账方案
        Long voucherWordId = voucherWord.getId();
        closingSchemeMapper.insertBatch(convertList(pendingPresets,
                preset -> buildPresetClosingScheme(accountSetId, voucherWordId, preset, subjectMap)));
    }

    @Override
    public FmsClosingSchemeDO getClosingSchemeByAccountSetIdAndType(Long accountSetId, Integer type) {
        return closingSchemeMapper.selectByAccountSetIdAndType(accountSetId, type);
    }

    @Override
    public FmsClosingSchemeDO validateClosingSchemeExists(Long accountSetId, Long id) {
        FmsClosingSchemeDO closing = closingSchemeMapper.selectByIdAndAccountSetId(id, accountSetId);
        if (closing == null) {
            throw exception(CLOSING_SCHEME_NOT_EXISTS);
        }
        return closing;
    }

    @Override
    public Long getClosingSchemeCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        Set<Long> subjectIdSet = new HashSet<>(subjectIds);
        List<FmsClosingSchemeDO> closings = closingSchemeMapper.selectListByAccountSetId(accountSetId);
        // 来源科目、结转科目或科目规则中任一引用命中即计入
        return (long) CollUtil.count(closings, closing -> subjectIdSet.contains(closing.getSubjectId())
                || subjectIdSet.contains(closing.getPriorYearAdjustmentSubjectId())
                || subjectIdSet.contains(closing.getAdjustmentClosingSubjectId())
                || subjectIdSet.contains(closing.getOtherClosingSubjectId())
                || CollUtil.isNotEmpty(closing.getSubjectRules()) && closing.getSubjectRules().stream()
                        .map(FmsClosingSchemeDO.SubjectRule::getSubjectId)
                        .anyMatch(subjectIdSet::contains));
    }

    @Override
    public Long getClosingSchemeCountByVoucherWordId(Long accountSetId, Long voucherWordId) {
        return closingSchemeMapper.selectCountByAccountSetIdAndVoucherWordId(accountSetId, voucherWordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_SETTINGS_UPDATE_SUB_TYPE,
            bizNo = "{{#saveReqVO.accountSetId}}", success = FMS_CLOSING_SETTINGS_UPDATE_SUCCESS)
    public Long saveProfitLossSettings(FmsProfitLossSettingsSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(saveReqVO.getAccountSetId(), userId);
        // 1.2 校验凭证字和科目
        voucherWordService.validateVoucherWordExists(saveReqVO.getAccountSetId(), saveReqVO.getVoucherWordId());
        validateProfitLossSettings(saveReqVO, userId);

        // 2. 创建或更新结转损益设置
        FmsClosingSchemeDO closing = closingSchemeMapper.selectByAccountSetIdAndType(
                saveReqVO.getAccountSetId(), FmsClosingTypeEnum.PROFIT_LOSS.getType());
        FmsClosingSchemeDO updateObj = BeanUtils.toBean(saveReqVO, FmsClosingSchemeDO.class)
                .setName(FmsClosingTypeEnum.PROFIT_LOSS.getName()).setPeriodEnd(true)
                .setType(FmsClosingTypeEnum.PROFIT_LOSS.getType());
        if (closing == null) {
            closingSchemeMapper.insert(updateObj);
            return updateObj.getId();
        }
        closingSchemeMapper.updateById(updateObj.setId(closing.getId()));
        return closing.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FmsClosingSchemeRespVO> getClosingSchemeList(
            FmsClosingQueryReqVO queryReqVO, Long userId) {
        // 1. 校验账套读权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(
                queryReqVO.getAccountSetId(), userId);

        // 2. 查询方案、余额和已生成凭证
        YearMonth month = LocalDateTimeUtils.parseYearMonth(queryReqVO.getMonth());
        List<FmsClosingSchemeDO> closings = closingSchemeMapper.selectPeriodEndListByAccountSetId(
                queryReqVO.getAccountSetId());
        if (CollUtil.isEmpty(closings)) {
            return Collections.emptyList();
        }
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = getSubjectBalanceMap(
                queryReqVO.getAccountSetId(), month, userId);
        Map<Long, FmsLedgerSubjectBalanceRespVO> yearBalanceMap = closings.stream()
                .anyMatch(item -> ObjUtil.equal(item.getTimeType(),
                        FmsClosingTimeTypeEnum.YEAR_BEGIN.getType()))
                ? getSubjectBalanceMap(queryReqVO.getAccountSetId(),
                        FmsPeriodUtils.getYearStartMonth(accountSet, month), month, userId) : balanceMap;
        Map<Long, List<FmsClosingVoucherDO>> closingVoucherMap = new HashMap<>();
        LocalDateTime monthBeginTime = LocalDateTimeUtils.getMonthBeginTime(month);
        closingVoucherService.getClosingVoucherListByPeriod(queryReqVO.getAccountSetId(),
                        monthBeginTime, LocalDateTimeUtils.endOfMonth(monthBeginTime))
                .forEach(item -> closingVoucherMap.computeIfAbsent(
                        item.getClosingId(), key -> new ArrayList<>()).add(item));

        // 查询账套科目，供专用结转方案按预置科目编码取数
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(
                queryReqVO.getAccountSetId(), null, userId);

        // 3. 组装方案结转金额和凭证编号
        List<FmsClosingSchemeRespVO> results = new ArrayList<>();
        for (FmsClosingSchemeDO closing : closings) {
            BigDecimal balance;
            if (ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.PROFIT_LOSS.getType())) {
                balance = calculateProfitLossBalance(queryReqVO.getAccountSetId(), month, userId);
            } else if (ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.REGULAR.getType())) {
                balance = calculateClosingSchemeBalance(closing,
                        ObjUtil.equal(closing.getTimeType(), FmsClosingTimeTypeEnum.YEAR_BEGIN.getType())
                                ? yearBalanceMap : balanceMap);
            } else {
                balance = calculateSpecialClosingBalance(closing, queryReqVO.getAccountSetId(),
                        month, subjects, balanceMap, userId);
            }
            List<FmsClosingVoucherDO> closingVouchers = closingVoucherMap.getOrDefault(
                    closing.getId(), Collections.emptyList());
            BigDecimal displayBalance = CollUtil.isNotEmpty(closingVouchers)
                    ? sumBigDecimal(closingVouchers,
                            item -> NumberUtils.zeroIfNull(item.getAmount())) : balance;
            List<FmsClosingSchemeRespVO.SubjectRule> subjectRules = CollUtil.isEmpty(closing.getSubjectRules())
                    ? Collections.emptyList() : BeanUtils.toBean(
                            closing.getSubjectRules(), FmsClosingSchemeRespVO.SubjectRule.class);
            FmsClosingSchemeRespVO respVO = BeanUtils.toBean(closing, FmsClosingSchemeRespVO.class)
                    .setSubjects(subjectRules)
                    .setBalance(displayBalance)
                    .setVoucherIds(closingVouchers.stream().map(FmsClosingVoucherDO::getVoucherId)
                            .collect(Collectors.toList()));
            results.add(respVO);
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_SCHEME_CREATE_SUB_TYPE,
            bizNo = "{{#createReqVO.accountSetId}}", success = FMS_CLOSING_SCHEME_CREATE_SUCCESS)
    public Long createClosingScheme(FmsClosingSchemeSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验凭证字和科目规则
        voucherWordService.validateVoucherWordExists(createReqVO.getAccountSetId(), createReqVO.getVoucherWordId());
        List<FmsSubjectDO> subjects = validateClosingScheme(createReqVO, userId);

        // 2. 创建结账方案
        FmsClosingSchemeDO closing = BeanUtils.toBean(createReqVO, FmsClosingSchemeDO.class)
                .setId(null).setType(FmsClosingTypeEnum.REGULAR.getType())
                .setDigest(createReqVO.getName())
                .setSubjectRules(buildSubjectRules(createReqVO.getSubjects(), subjects));
        closingSchemeMapper.insert(closing);
        return closing.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_SCHEME_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_CLOSING_SCHEME_UPDATE_SUCCESS)
    public void updateClosingScheme(FmsClosingSchemeSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验结账方案
        FmsClosingSchemeDO closing = validateClosingSchemeExists(
                updateReqVO.getAccountSetId(), updateReqVO.getId());
        if (!ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.REGULAR.getType())) {
            throw exception(CLOSING_SCHEME_NOT_EXISTS);
        }
        // 1.3 校验凭证字和科目规则
        voucherWordService.validateVoucherWordExists(updateReqVO.getAccountSetId(), updateReqVO.getVoucherWordId());
        List<FmsSubjectDO> subjects = validateClosingScheme(updateReqVO, userId);

        // 2. 更新结账方案
        closingSchemeMapper.updateById(BeanUtils.toBean(updateReqVO, FmsClosingSchemeDO.class)
                .setType(FmsClosingTypeEnum.REGULAR.getType()).setDigest(updateReqVO.getName())
                .setSubjectRules(buildSubjectRules(updateReqVO.getSubjects(), subjects)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_SETTINGS_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_CLOSING_SPECIAL_SETTINGS_UPDATE_SUCCESS)
    public void updateSpecialClosingSettings(
            FmsSpecialClosingSettingsSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验专用结转方案
        FmsClosingSchemeDO closing = validateClosingSchemeExists(
                updateReqVO.getAccountSetId(), updateReqVO.getId());
        if (!isSpecialClosing(closing)) {
            throw exception(CLOSING_SCHEME_NOT_EXISTS);
        }
        // 1.3 校验凭证字和科目规则
        voucherWordService.validateVoucherWordExists(
                updateReqVO.getAccountSetId(), updateReqVO.getVoucherWordId());
        List<FmsSubjectDO> subjects = validateSpecialClosingSettings(
                closing.getType(), updateReqVO, userId);

        // 2. 更新专用结转设置
        closingSchemeMapper.updateById(new FmsClosingSchemeDO().setId(closing.getId())
                .setVoucherWordId(updateReqVO.getVoucherWordId())
                .setSubjectRules(buildSpecialSubjectRules(updateReqVO.getSubjects(), subjects)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_SCHEME_DELETE_SUB_TYPE,
            bizNo = "{{#accountSetId}}", success = FMS_CLOSING_SCHEME_DELETE_SUCCESS)
    public void deleteClosingScheme(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验结账方案
        FmsClosingSchemeDO closing = validateClosingSchemeExists(accountSetId, id);
        if (!ObjUtil.equal(closing.getType(), FmsClosingTypeEnum.REGULAR.getType())) {
            throw exception(CLOSING_SCHEME_NOT_EXISTS);
        }
        if (closingVoucherService.getClosingVoucherCountByClosingId(id) > 0) {
            throw exception(CLOSING_SCHEME_IN_USE);
        }

        // 2. 删除结账方案
        closingSchemeMapper.deleteById(id);
    }

    /**
     * 计算结转损益余额：末级损益科目本期借贷差额绝对值之和
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param userId 用户编号
     * @return 结转损益余额
     */
    private BigDecimal calculateProfitLossBalance(Long accountSetId, YearMonth month, Long userId) {
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = getSubjectBalanceMap(accountSetId, month, userId);
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
     * 构造预置结账方案
     *
     * @param accountSetId 账套编号
     * @param voucherWordId 默认凭证字编号
     * @param preset 预置方案
     * @param subjectMap 标准科目编码到实际科目的 Map
     * @return 结账方案
     */
    private FmsClosingSchemeDO buildPresetClosingScheme(Long accountSetId, Long voucherWordId,
            SchemePreset preset, Map<String, FmsSubjectDO> subjectMap) {
        // 1. 构造结账方案公共字段
        FmsClosingTypeEnum closingType = FmsClosingTypeEnum.valueOf(preset.getClosingType());
        FmsClosingSchemeDO scheme = new FmsClosingSchemeDO().setName(preset.getName())
                .setPeriodEnd(preset.getPeriodEnd()).setVoucherWordId(voucherWordId)
                .setDigest(preset.getDigest()).setType(closingType.getType())
                .setAccountSetId(accountSetId).setClosingDay(preset.getClosingDay());
        if (closingType == FmsClosingTypeEnum.PROFIT_LOSS) {
            // 2. 构造结转损益专用字段
            return scheme.setVoucherType(FmsClosingVoucherTypeEnum.valueOf(preset.getVoucherType()).getType())
                    .setReverseBalance(preset.getReverseBalance())
                    .setPriorYearAdjustmentSubjectId(getPresetSubject(subjectMap, preset, preset.getPriorYearAdjustmentSubject()).getId())
                    .setAdjustmentClosingSubjectId(getPresetSubject(subjectMap, preset, preset.getAdjustmentClosingSubject()).getId())
                    .setOtherClosingSubjectId(getPresetSubject(subjectMap, preset, preset.getOtherClosingSubject()).getId());
        }
        // 3. 构造其他专用结转方案的科目规则
        preset.getCalculationSubjects().forEach(subject -> getPresetSubject(subjectMap, preset, subject));
        return scheme.setSubjectRules(convertList(preset.getEntries(), entry -> {
            FmsSubjectDO subject = getPresetSubject(subjectMap, preset, entry.getSubject());
            return FmsClosingSchemeDO.SubjectRule.builder().subjectId(subject.getId())
                    .subjectCode(subject.getCode()).digest(entry.getDigest())
                    .direction(FmsDebitCreditDirectionEnum.valueOf(entry.getDirection()).getType())
                    .amountRatio(entry.getAmountRatio()).build();
        }));
    }

    /**
     * 获得预置方案使用的账套科目
     *
     * @param subjectMap 标准科目编码到实际科目的 Map
     * @param preset 预置方案
     * @param subjectName 预置科目枚举名
     * @return 账套科目
     */
    private FmsSubjectDO getPresetSubject(Map<String, FmsSubjectDO> subjectMap,
                                          SchemePreset preset, String subjectName) {
        FmsClosingPresetSubjectEnum presetSubject = FmsClosingPresetSubjectEnum.valueOf(subjectName);
        FmsSubjectDO subject = subjectMap.get(presetSubject.getCode());
        if (subject == null) {
            throw exception(CLOSING_SCHEME_PRESET_SUBJECT_NOT_EXISTS,
                    preset.getCode(), presetSubject.getCode());
        }
        return subject;
    }

    /**
     * 加载结账方案预置文件
     *
     * @return 预置文件
     */
    private List<SchemePreset> loadSchemePresets() {
        try (InputStream inputStream = new ClassPathResource(PRESET_RESOURCE).getInputStream()) {
            return JsonUtils.parseArray(IoUtil.readUtf8(inputStream), SchemePreset.class);
        } catch (Exception ex) {
            throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
        }
    }

    /**
     * 校验结账方案预置文件
     *
     * @param presets 预置方案数组
     */
    private void validateSchemePresets(List<SchemePreset> presets) {
        // 1. 校验预置编码和结账类型唯一
        if (CollUtil.isEmpty(presets)
                || convertSet(presets, SchemePreset::getCode).size() != presets.size()
                || convertSet(presets, SchemePreset::getClosingType).size() != presets.size()) {
            throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
        }
        // 2. 校验各类型必需的枚举字段和科目规则
        try {
            for (SchemePreset preset : presets) {
                FmsClosingTypeEnum closingType = FmsClosingTypeEnum.valueOf(preset.getClosingType());
                if (closingType == FmsClosingTypeEnum.PROFIT_LOSS) {
                    FmsClosingVoucherTypeEnum.valueOf(preset.getVoucherType());
                    FmsClosingPresetSubjectEnum.valueOf(preset.getPriorYearAdjustmentSubject());
                    FmsClosingPresetSubjectEnum.valueOf(preset.getAdjustmentClosingSubject());
                    FmsClosingPresetSubjectEnum.valueOf(preset.getOtherClosingSubject());
                    continue;
                }
                if (CollUtil.size(preset.getEntries()) < 2) {
                    throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
                }
                for (String subject : preset.getCalculationSubjects()) {
                    FmsClosingPresetSubjectEnum.valueOf(subject);
                }
                for (SchemePreset.Entry entry : preset.getEntries()) {
                    FmsClosingPresetSubjectEnum.valueOf(entry.getSubject());
                    FmsDebitCreditDirectionEnum.valueOf(entry.getDirection());
                }
                BigDecimal debitRatio = sumBigDecimal(filterList(preset.getEntries(), entry -> Objects.equals(
                        entry.getDirection(), FmsDebitCreditDirectionEnum.DEBIT.name())),
                        SchemePreset.Entry::getAmountRatio);
                BigDecimal creditRatio = sumBigDecimal(filterList(preset.getEntries(), entry -> Objects.equals(
                        entry.getDirection(), FmsDebitCreditDirectionEnum.CREDIT.name())),
                        SchemePreset.Entry::getAmountRatio);
                if (debitRatio.compareTo(creditRatio) != 0) {
                    throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
                }
            }
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
        }
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
        Map<String, FmsSubjectDO> subjectMap = buildPresetSubjectMap(accountSetId, subjects);
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
     * 按标准科目编码构建预置科目 Map
     *
     * @param accountSetId 账套编号
     * @param subjects 账套科目列表
     * @return 标准科目编码到实际科目的 Map
     */
    private Map<String, FmsSubjectDO> buildPresetSubjectMap(Long accountSetId, List<FmsSubjectDO> subjects) {
        // 1. 按账套实际科目编码建立索引
        Map<String, FmsSubjectDO> actualSubjectMap = convertMap(subjects, FmsSubjectDO::getCode,
                Function.identity(), (first, second) -> first);

        // 2. 将各预置标准编码转换为账套实际编码
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(accountSetId);
        Map<String, FmsSubjectDO> result = new LinkedHashMap<>();
        for (FmsClosingPresetSubjectEnum presetSubject : FmsClosingPresetSubjectEnum.values()) {
            String actualCode = financeParameterService.convertStandardSubjectCode(
                    presetSubject.getCode(), financeParameter.getSubjectCodeRule());
            FmsSubjectDO subject = actualSubjectMap.get(actualCode);
            if (subject != null) {
                result.put(presetSubject.getCode(), subject);
            }
        }
        return result;
    }

    /**
     * 校验常规结账方案的来源科目、科目规则和金额比例，返回账套科目列表
     *
     * @param saveReqVO 方案信息
     * @param userId 用户编号
     * @return 账套下的科目列表
     */
    private List<FmsSubjectDO> validateClosingScheme(
            FmsClosingSchemeSaveReqVO saveReqVO, Long userId) {
        // 1. 校验来源科目和规则科目存在且为末级科目
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(
                saveReqVO.getAccountSetId(), null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        if (!subjectMap.containsKey(saveReqVO.getSubjectId())
                || parentIds.contains(saveReqVO.getSubjectId())) {
            throw exception(CLOSING_SCHEME_SUBJECT_INVALID);
        }
        for (FmsClosingSchemeSaveReqVO.SubjectRule subjectRule : saveReqVO.getSubjects()) {
            if (!subjectMap.containsKey(subjectRule.getSubjectId())
                    || parentIds.contains(subjectRule.getSubjectId())) {
                throw exception(CLOSING_SCHEME_SUBJECT_INVALID);
            }
        }
        // 2. 校验借方和贷方金额比例分别等于 100%
        BigDecimal debitRatio = sumBigDecimal(filterList(saveReqVO.getSubjects(),
                item -> ObjUtil.equal(item.getDirection(), FmsDebitCreditDirectionEnum.DEBIT.getType())),
                FmsClosingSchemeSaveReqVO.SubjectRule::getAmountRatio);
        BigDecimal creditRatio = sumBigDecimal(filterList(saveReqVO.getSubjects(),
                item -> ObjUtil.equal(item.getDirection(), FmsDebitCreditDirectionEnum.CREDIT.getType())),
                FmsClosingSchemeSaveReqVO.SubjectRule::getAmountRatio);
        if (debitRatio.compareTo(NumberUtils.ONE_HUNDRED) != 0
                || creditRatio.compareTo(NumberUtils.ONE_HUNDRED) != 0) {
            throw exception(CLOSING_SCHEME_RATIO_INVALID);
        }
        return subjects;
    }

    /**
     * 构造常规结账方案的科目规则，按科目编号补充科目编码快照
     *
     * @param saveRules 保存的科目规则
     * @param subjects 账套下的科目列表
     * @return 科目规则
     */
    private List<FmsClosingSchemeDO.SubjectRule> buildSubjectRules(
            List<FmsClosingSchemeSaveReqVO.SubjectRule> saveRules, List<FmsSubjectDO> subjects) {
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        List<FmsClosingSchemeDO.SubjectRule> rules = BeanUtils.toBean(saveRules, FmsClosingSchemeDO.SubjectRule.class);
        rules.forEach(rule -> rule.setSubjectCode(subjectMap.get(rule.getSubjectId()).getCode()));
        return rules;
    }

    /**
     * 校验专用结转的科目规则和金额比例，返回账套科目列表
     *
     * 借方比例必须等于贷方比例且不超过 100%；转出未交增值税的借方比例必须等于 100%
     *
     * @param closingType 结转类型
     * @param saveReqVO 设置信息
     * @param userId 用户编号
     * @return 账套下的科目列表
     */
    private List<FmsSubjectDO> validateSpecialClosingSettings(Integer closingType,
            FmsSpecialClosingSettingsSaveReqVO saveReqVO, Long userId) {
        // 1. 校验规则科目存在且为末级科目
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(
                saveReqVO.getAccountSetId(), null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        if (saveReqVO.getSubjects().stream().anyMatch(subjectRule ->
                !subjectMap.containsKey(subjectRule.getSubjectId())
                        || parentIds.contains(subjectRule.getSubjectId()))) {
            throw exception(CLOSING_SPECIAL_SETTINGS_INVALID);
        }
        // 2. 校验借贷比例相等且不超过 100%
        BigDecimal debitRatio = sumBigDecimal(filterList(saveReqVO.getSubjects(),
                item -> ObjUtil.equal(item.getDirection(), FmsDebitCreditDirectionEnum.DEBIT.getType())),
                FmsSpecialClosingSettingsSaveReqVO.SubjectRule::getAmountRatio);
        BigDecimal creditRatio = sumBigDecimal(filterList(saveReqVO.getSubjects(),
                item -> ObjUtil.equal(item.getDirection(), FmsDebitCreditDirectionEnum.CREDIT.getType())),
                FmsSpecialClosingSettingsSaveReqVO.SubjectRule::getAmountRatio);
        boolean unpaidVatRatioInvalid = ObjUtil.equal(
                closingType, FmsClosingTypeEnum.UNPAID_VAT.getType())
                && debitRatio.compareTo(NumberUtils.ONE_HUNDRED) != 0;
        if (debitRatio.signum() <= 0 || debitRatio.compareTo(creditRatio) != 0
                || debitRatio.compareTo(NumberUtils.ONE_HUNDRED) > 0 || unpaidVatRatioInvalid) {
            throw exception(CLOSING_SPECIAL_SETTINGS_INVALID);
        }
        return subjects;
    }

    /**
     * 构造专用结转的科目规则，按科目编号补充科目编码快照
     *
     * @param saveRules 保存的科目规则
     * @param subjects 账套下的科目列表
     * @return 科目规则
     */
    private List<FmsClosingSchemeDO.SubjectRule> buildSpecialSubjectRules(
            List<FmsSpecialClosingSettingsSaveReqVO.SubjectRule> saveRules,
            List<FmsSubjectDO> subjects) {
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        List<FmsClosingSchemeDO.SubjectRule> rules = BeanUtils.toBean(
                saveRules, FmsClosingSchemeDO.SubjectRule.class);
        rules.forEach(rule -> rule.setSubjectCode(
                subjectMap.get(rule.getSubjectId()).getCode()));
        return rules;
    }

    /**
     * 校验结转损益设置的科目合法性
     *
     * @param saveReqVO 设置信息
     * @param userId 用户编号
     */
    private void validateProfitLossSettings(FmsProfitLossSettingsSaveReqVO saveReqVO, Long userId) {
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(saveReqVO.getAccountSetId(), null, userId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        validateProfitLossSubjects(BeanUtils.toBean(saveReqVO, FmsClosingSchemeDO.class),
                subjects, parentIds);
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
                || ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.SUBJECT_DEBIT_BALANCE.getRule())) {
            return debitBalance;
        }
        if (ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.CREDIT_BALANCE.getRule())
                || ObjUtil.equal(closing.getFormulaRule(), FmsFormulaRuleEnum.SUBJECT_CREDIT_BALANCE.getRule())) {
            return creditBalance;
        }
        // 本期或本年累计发生额口径
        BigDecimal debitAmount = ObjUtil.equal(closing.getTimeType(), FmsClosingTimeTypeEnum.YEAR_BEGIN.getType())
                ? NumberUtils.zeroIfNull(balance.getYearDebitAmount())
                : NumberUtils.zeroIfNull(balance.getPeriodDebitAmount());
        BigDecimal creditAmount = ObjUtil.equal(closing.getTimeType(), FmsClosingTimeTypeEnum.YEAR_BEGIN.getType())
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
     * 汇总科目规则中指定借贷方向的金额比例
     *
     * @param subjectRules 科目规则
     * @param direction 借贷方向
     * @return 金额比例合计
     */
    private BigDecimal getDirectionRatio(List<FmsClosingSchemeDO.SubjectRule> subjectRules, Integer direction) {
        return sumBigDecimal(filterList(subjectRules,
                        item -> ObjUtil.equal(item.getDirection(), direction)), FmsClosingSchemeDO.SubjectRule::getAmountRatio);
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

    @Data
    private static class SchemePreset {

        /**
         * 预置编码
         */
        private String code;
        /**
         * 方案名称
         */
        private String name;
        /**
         * 结账类型
         */
        private String closingType;
        /**
         * 凭证摘要
         */
        private String digest;
        /**
         * 是否期末结转
         */
        private Boolean periodEnd;
        /**
         * 结转日期
         */
        private Integer closingDay;
        /**
         * 结转凭证类型
         */
        private String voucherType;
        /**
         * 是否按余额反向结转
         */
        private Boolean reverseBalance;
        /**
         * 以前年度损益调整科目
         */
        private String priorYearAdjustmentSubject;
        /**
         * 以前年度损益调整结转科目
         */
        private String adjustmentClosingSubject;
        /**
         * 其他损益结转科目
         */
        private String otherClosingSubject;
        /**
         * 计算科目数组
         */
        private List<String> calculationSubjects;
        /**
         * 分录数组
         */
        private List<Entry> entries;

        @Data
        private static class Entry {

            /**
             * 预置科目
             */
            private String subject;
            /**
             * 摘要
             */
            private String digest;
            /**
             * 借贷方向
             */
            private String direction;
            /**
             * 金额比例
             */
            private BigDecimal amountRatio;
        }
    }

}

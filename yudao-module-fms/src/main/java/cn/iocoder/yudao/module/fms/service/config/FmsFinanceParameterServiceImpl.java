package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeparameter.FmsFinanceParameterUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsFinanceParameterMapper;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_NOT_INITIALIZED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_SETTINGS_RULE_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_SETTINGS_RULE_SHRINK;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.FINANCE_PARAMETER_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_FINANCE_PARAMETER_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_FINANCE_PARAMETER_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_FINANCE_PARAMETER_UPDATE_SUCCESS;

/**
 * FMS 财务参数 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsFinanceParameterServiceImpl implements FmsFinanceParameterService {

    private static final int SUBJECT_CODE_LENGTH_MIN = 2;
    private static final int SUBJECT_CODE_LENGTH_MAX = 5;

    @Resource
    private FmsFinanceParameterMapper financeParameterMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private FmsSubjectService subjectService;

    @Override
    public void initializeFinanceParameter(Long accountSetId, FmsAccountSetInitializeReqVO initializeReqVO) {
        // 1. 校验科目层级和编码规则
        List<Integer> subjectCodeRules = parseSubjectCodeRules(initializeReqVO.getSubjectCodeRule());
        validateSubjectCodeRules(initializeReqVO.getLevel(), subjectCodeRules);

        // 2. 创建财务参数
        FmsFinanceParameterDO financeParameter = new FmsFinanceParameterDO().setAccountSetId(accountSetId)
                .setLevel(initializeReqVO.getLevel()).setSubjectCodeRule(initializeReqVO.getSubjectCodeRule())
                .setLedgerBalanceMode(initializeReqVO.getLedgerBalanceMode())
                .setVoucherReviewRequired(FmsFinanceParameterDO.DEFAULT_VOUCHER_REVIEW_REQUIRED)
                .setDeficitCheck(FmsFinanceParameterDO.DEFAULT_DEFICIT_CHECK)
                .setAssetPeriodLocked(FmsFinanceParameterDO.DEFAULT_ASSET_PERIOD_LOCKED);
        financeParameterMapper.insert(financeParameter);
    }

    @Override
    public FmsFinanceParameterDO getFinanceParameter(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询财务参数
        return getFinanceParameter(accountSetId);
    }

    @Override
    public FmsFinanceParameterDO getFinanceParameter(Long accountSetId) {
        return financeParameterMapper.selectByAccountSetId(accountSetId);
    }

    @Override
    public String convertStandardSubjectCode(String standardCode, String subjectCodeRule) {
        // 1. 解析标准编码规则和账套实际编码规则
        List<Integer> standardRules = parseSubjectCodeRules(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE);
        List<Integer> actualRules = parseSubjectCodeRules(subjectCodeRule);
        // 2. 按科目层级逐段扩展编码
        StringBuilder result = new StringBuilder();
        int offset = 0;
        for (int index = 0; index < standardRules.size() && offset < standardCode.length(); index++) {
            int standardLength = standardRules.get(index);
            int actualLength = actualRules.get(index);
            if (offset + standardLength > standardCode.length()) {
                throw exception(ACCOUNT_SET_SETTINGS_RULE_INVALID);
            }
            String segment = standardCode.substring(offset, offset + standardLength);
            int increment = actualLength - standardLength;
            if (increment > 0 && index == 0) {
                result.append(segment.charAt(0)).append(CharSequenceUtil.repeat('0', increment)).append(segment.substring(1));
            } else if (increment > 0) {
                result.append(CharSequenceUtil.repeat('0', increment)).append(segment);
            } else {
                result.append(segment);
            }
            offset += standardLength;
        }
        // 3. 标准编码必须由完整的科目层级组成
        if (offset != standardCode.length()) {
            throw exception(ACCOUNT_SET_SETTINGS_RULE_INVALID);
        }
        return result.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_FINANCE_PARAMETER_TYPE, subType = FMS_FINANCE_PARAMETER_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_FINANCE_PARAMETER_UPDATE_SUCCESS)
    public void updateFinanceParameter(FmsFinanceParameterUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetWritePermission(
                updateReqVO.getAccountSetId(), userId);
        // 1.2 校验账套初始化状态
        validateAccountSetInitialized(accountSet);

        // 2. 校验财务参数和编码规则
        FmsFinanceParameterDO financeParameter = validateFinanceParameterExists(accountSet.getId());
        List<Integer> oldRules = parseSubjectCodeRules(financeParameter.getSubjectCodeRule());
        List<Integer> newRules = parseSubjectCodeRules(updateReqVO.getSubjectCodeRule());
        validateSubjectCodeRules(financeParameter.getLevel(), oldRules, updateReqVO.getLevel(), newRules);

        // 3. 扩展现有科目编码并更新财务参数
        subjectService.expandSubjectCodes(accountSet.getId(), oldRules, newRules);
        if (updateReqVO.getStandard() != null && !ObjUtil.equal(accountSet.getStandard(), updateReqVO.getStandard())) {
            accountSetService.updateAccountSetStandard(accountSet.getId(), updateReqVO.getStandard(), userId);
        }
        // 当前页面未维护的领域参数保持原值
        financeParameterMapper.updateById(new FmsFinanceParameterDO().setId(financeParameter.getId())
                .setLevel(updateReqVO.getLevel()).setSubjectCodeRule(updateReqVO.getSubjectCodeRule())
                .setLedgerBalanceMode(updateReqVO.getLedgerBalanceMode())
                .setVoucherReviewRequired(updateReqVO.getVoucherReviewRequired()));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("accountSet", accountSet);
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(financeParameter, FmsFinanceParameterUpdateReqVO.class));
    }

    private FmsFinanceParameterDO validateFinanceParameterExists(Long accountSetId) {
        FmsFinanceParameterDO financeParameter = financeParameterMapper.selectByAccountSetId(accountSetId);
        if (financeParameter == null) {
            throw exception(FINANCE_PARAMETER_NOT_EXISTS);
        }
        return financeParameter;
    }

    private void validateAccountSetInitialized(FmsAccountSetDO accountSet) {
        if (Boolean.TRUE.equals(accountSet.getInitialized())) {
            return;
        }
        throw exception(ACCOUNT_SET_NOT_INITIALIZED);
    }

    private static List<Integer> parseSubjectCodeRules(String subjectCodeRule) {
        return StrUtils.splitToInteger(subjectCodeRule, "-");
    }

    private static void validateSubjectCodeRules(Integer level, List<Integer> rules) {
        if (ObjUtil.notEqual(rules.size(), level) || rules.stream().anyMatch(
                rule -> rule < SUBJECT_CODE_LENGTH_MIN || rule > SUBJECT_CODE_LENGTH_MAX)) {
            throw exception(ACCOUNT_SET_SETTINGS_RULE_INVALID);
        }
    }

    /**
     * 校验财务参数只扩展科目层级和各级编码长度，避免已使用的科目编码失效
     *
     * 每级编码长度限制为 2 至 5 位，与既有财务规则保持一致
     *
     * @param oldLevel 原科目层级
     * @param oldRules 原科目编码规则
     * @param newLevel 新科目层级
     * @param newRules 新科目编码规则
     */
    private static void validateSubjectCodeRules(Integer oldLevel, List<Integer> oldRules,
            Integer newLevel, List<Integer> newRules) {
        validateSubjectCodeRules(newLevel, newRules);
        if (newLevel < oldLevel || oldRules.size() != oldLevel) {
            throw exception(ACCOUNT_SET_SETTINGS_RULE_SHRINK);
        }
        for (int index = 0; index < oldRules.size(); index++) {
            if (newRules.get(index) < oldRules.get(index)) {
                throw exception(ACCOUNT_SET_SETTINGS_RULE_SHRINK);
            }
        }
    }

}

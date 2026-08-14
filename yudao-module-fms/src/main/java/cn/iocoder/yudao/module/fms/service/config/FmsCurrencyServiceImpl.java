package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency.FmsCurrencySaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsCurrencyMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsCurrencyPresetEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_STANDARD_NOT_DELETABLE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CURRENCY_UPDATE_SUCCESS;

/**
 * FMS 币别 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsCurrencyServiceImpl implements FmsCurrencyService {

    @Resource
    private FmsCurrencyMapper currencyMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private FmsSubjectService subjectService;

    @Override
    public FmsCurrencyDO initializeStandardCurrency(Long accountSetId, FmsCurrencyPresetEnum presetCurrency) {
        FmsCurrencyDO currency = new FmsCurrencyDO().setAccountSetId(accountSetId)
                .setCode(presetCurrency.getCode()).setName(presetCurrency.getName())
                .setExchangeRate(presetCurrency.getExchangeRate()).setStandard(true);
        currencyMapper.insert(currency);
        return currency;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CURRENCY_TYPE, subType = FMS_CURRENCY_CREATE_SUB_TYPE,
            bizNo = "{{#currencyId}}", success = FMS_CURRENCY_CREATE_SUCCESS)
    public Long createCurrency(FmsCurrencySaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验币别编码唯一
        validateCurrencyCodeUnique(null, createReqVO.getAccountSetId(), createReqVO.getCode());

        // 2. 创建币别
        FmsCurrencyDO currency = BeanUtils.toBean(createReqVO, FmsCurrencyDO.class)
                .setId(null).setStandard(false);
        currencyMapper.insert(currency);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("currencyId", currency.getId());
        return currency.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CURRENCY_TYPE, subType = FMS_CURRENCY_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_CURRENCY_UPDATE_SUCCESS)
    public void updateCurrency(FmsCurrencySaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验币别
        FmsCurrencyDO currency = validateCurrencyExists(updateReqVO.getAccountSetId(), updateReqVO.getId());
        if (Boolean.TRUE.equals(currency.getStandard())) {
            updateReqVO.setCode(currency.getCode());
        }
        // 1.3 校验币别编码唯一
        validateCurrencyCodeUnique(currency.getId(), updateReqVO.getAccountSetId(), updateReqVO.getCode());

        // 2. 更新币别
        if (Boolean.TRUE.equals(currency.getStandard())) {
            updateReqVO.setExchangeRate(BigDecimal.ONE);
        }
        currencyMapper.updateById(BeanUtils.toBean(updateReqVO, FmsCurrencyDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_CURRENCY_TYPE, subType = FMS_CURRENCY_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_CURRENCY_DELETE_SUCCESS)
    public void deleteCurrency(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验币别存在且不是本位币
        FmsCurrencyDO currency = validateCurrencyExists(accountSetId, id);
        if (Boolean.TRUE.equals(currency.getStandard())) {
            throw exception(CURRENCY_STANDARD_NOT_DELETABLE);
        }
        // 1.3 校验币别未被科目使用
        if (subjectService.getSubjectCountByCurrencyId(accountSetId, id) > 0) {
            throw exception(CURRENCY_IN_USE);
        }

        // 2. 删除币别
        currencyMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("currency", currency);
    }

    @Override
    public List<FmsCurrencyDO> getCurrencyList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询币别列表
        return currencyMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public FmsCurrencyDO getCurrency(Long accountSetId, Long id, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询并校验币别
        return validateCurrencyExists(accountSetId, id);
    }

    @Override
    public List<FmsCurrencyDO> validateCurrencyList(Long accountSetId, List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<FmsCurrencyDO> currencies = currencyMapper.selectListByIdsAndAccountSetId(ids, accountSetId);
        if (currencies.size() != ids.size()) {
            throw exception(CURRENCY_NOT_EXISTS);
        }
        return currencies;
    }

    private FmsCurrencyDO validateCurrencyExists(Long accountSetId, Long id) {
        FmsCurrencyDO currency = currencyMapper.selectByIdAndAccountSetId(id, accountSetId);
        if (currency == null) {
            throw exception(CURRENCY_NOT_EXISTS);
        }
        return currency;
    }

    private void validateCurrencyCodeUnique(Long id, Long accountSetId, String code) {
        FmsCurrencyDO currency = currencyMapper.selectByAccountSetIdAndCode(accountSetId, code);
        if (currency != null && ObjUtil.notEqual(currency.getId(), id)) {
            throw exception(CURRENCY_CODE_DUPLICATE);
        }
    }
}

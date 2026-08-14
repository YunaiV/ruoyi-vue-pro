package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAccountSetMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountUserLevelEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsCurrencyPresetEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingTemplateService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_ALREADY_INITIALIZED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_ACCESS_DENIED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_INITIALIZE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_INITIALIZE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_UPDATE_SUCCESS;

/**
 * FMS 账套 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsAccountSetServiceImpl implements FmsAccountSetService {

    @Resource
    private FmsAccountSetMapper accountSetMapper;

    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsAccountUserService accountUserService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsCurrencyService currencyService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsFinanceParameterService financeParameterService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private FmsVoucherWordService voucherWordService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsSubjectService subjectService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsClosingSchemeService closingSchemeService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsFinanceIndicatorService financeIndicatorService;
    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private FmsClosingTemplateService closingTemplateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_ACCOUNT_SET_TYPE, subType = FMS_ACCOUNT_SET_CREATE_SUB_TYPE,
            bizNo = "{{#accountSetId}}", success = FMS_ACCOUNT_SET_CREATE_SUCCESS)
    public Long createAccountSet(FmsAccountSetSaveReqVO createReqVO, Long userId) {
        // 1. 校验公司编码唯一
        validateAccountSetCompanyCodeUnique(null, createReqVO.getCompanyCode());

        // 2. 创建未初始化账套
        FmsAccountSetDO accountSet = BeanUtils.toBean(createReqVO, FmsAccountSetDO.class)
                .setId(null).setInitialized(false);
        accountSetMapper.insert(accountSet);

        // 3. 将创建人加入账套
        accountUserService.createAccountOwner(accountSet.getId(), userId);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("accountSetId", accountSet.getId());
        return accountSet.getId();
    }

    @Override
    @LogRecord(type = FMS_ACCOUNT_SET_TYPE, subType = FMS_ACCOUNT_SET_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_ACCOUNT_SET_UPDATE_SUCCESS)
    public void updateAccountSet(FmsAccountSetSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套主管权限
        FmsAccountSetDO accountSet = validateAccountSetOwnerPermission(updateReqVO.getId(), userId);
        // 1.2 校验公司编码唯一
        validateAccountSetCompanyCodeUnique(updateReqVO.getId(), updateReqVO.getCompanyCode());

        // 2. 更新账套基本信息
        accountSetMapper.updateById(BeanUtils.toBean(updateReqVO, FmsAccountSetDO.class));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(accountSet, FmsAccountSetSaveReqVO.class));
    }

    @Override
    public void updateAccountSetStandard(Long accountSetId, Integer standard, Long userId) {
        // 1. 校验账套写权限
        validateAccountSetWritePermission(accountSetId, userId);

        // 2. 更新账套会计制度
        accountSetMapper.updateById(new FmsAccountSetDO().setId(accountSetId).setStandard(standard));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_ACCOUNT_SET_TYPE, subType = FMS_ACCOUNT_SET_INITIALIZE_SUB_TYPE,
            bizNo = "{{#initializeReqVO.accountSetId}}", success = FMS_ACCOUNT_SET_INITIALIZE_SUCCESS)
    public void initializeAccountSet(FmsAccountSetInitializeReqVO initializeReqVO, Long userId) {
        // 1.1 校验账套成员写权限；未初始化账套由可维护成员完成初始化
        FmsAccountSetDO accountSet = validateAccountSetWritePermission(initializeReqVO.getAccountSetId(), userId);
        // 1.2 校验账套初始化状态
        FmsCurrencyPresetEnum presetCurrency = FmsCurrencyPresetEnum.valueOfCode(initializeReqVO.getCurrencyCode());
        if (Boolean.TRUE.equals(accountSet.getInitialized())) {
            throw exception(ACCOUNT_SET_ALREADY_INITIALIZED);
        }

        // 2. 初始化账套基础数据
        FmsCurrencyDO currency = currencyService.initializeStandardCurrency(accountSet.getId(), presetCurrency);
        financeParameterService.initializeFinanceParameter(accountSet.getId(), initializeReqVO);
        voucherWordService.initializeDefaultVoucherWords(accountSet.getId());
        auxiliaryTypeService.initializeDefaultTypes(accountSet.getId());
        subjectService.initializeDefaultSubjects(accountSet.getId());
        closingTemplateService.initializeClosingTemplates(accountSet.getId(), userId);
        closingSchemeService.initializeDefaultClosingSchemes(accountSet.getId(), userId);
        financeIndicatorService.initializeDefaultFinanceIndicators(accountSet.getId(), userId);

        // 3. 回写账套初始化信息和本位币
        accountSetMapper.updateById(new FmsAccountSetDO().setId(accountSet.getId())
                .setInitialized(true).setStandard(initializeReqVO.getStandard())
                .setStartTime(LocalDateTimeUtils.beginOfMonth(initializeReqVO.getStartTime()))
                .setCurrencyId(currency.getId()));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("accountSet", accountSet);
    }

    @Override
    public FmsAccountSetDO getAccountSet(Long id) {
        return accountSetMapper.selectById(id);
    }

    @Override
    public FmsAccountSetDO validateAccountSetExists(Long id) {
        FmsAccountSetDO accountSet = getAccountSet(id);
        if (accountSet == null) {
            throw exception(ACCOUNT_SET_NOT_EXISTS);
        }
        return accountSet;
    }

    @Override
    public FmsAccountSetDO validateAccountSetReadPermission(Long accountSetId, Long userId) {
        FmsAccountSetDO accountSet = validateAccountSetExists(accountSetId);
        FmsAccountUserDO accountUser = accountUserService.getAccountUser(accountSetId, userId);
        if (accountUser == null || !FmsAccountUserLevelEnum.isReadable(accountUser.getLevel())) {
            throw exception(ACCOUNT_SET_ACCESS_DENIED);
        }
        return accountSet;
    }

    @Override
    public FmsAccountSetDO validateAccountSetWritePermission(Long accountSetId, Long userId) {
        FmsAccountSetDO accountSet = validateAccountSetExists(accountSetId);
        FmsAccountUserDO accountUser = accountUserService.getAccountUser(accountSetId, userId);
        if (accountUser == null || !FmsAccountUserLevelEnum.isWritable(accountUser.getLevel())) {
            throw exception(ACCOUNT_SET_ACCESS_DENIED);
        }
        return accountSet;
    }

    @Override
    public void lockAccountSet(Long accountSetId) {
        FmsAccountSetDO accountSet = accountSetMapper.selectByIdForUpdate(accountSetId);
        if (accountSet == null) {
            throw exception(ACCOUNT_SET_NOT_EXISTS);
        }
    }

    @Override
    public FmsAccountSetDO validateAccountSetOwnerPermission(Long accountSetId, Long userId) {
        FmsAccountSetDO accountSet = validateAccountSetExists(accountSetId);
        FmsAccountUserDO accountUser = accountUserService.getAccountUser(accountSetId, userId);
        if (accountUser == null || !FmsAccountUserLevelEnum.isOwner(accountUser.getLevel())) {
            throw exception(ACCOUNT_SET_ACCESS_DENIED);
        }
        return accountSet;
    }

    @Override
    public List<FmsAccountSetDO> getAccountSetList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return accountSetMapper.selectByIds(ids);
    }

    private void validateAccountSetCompanyCodeUnique(Long id, String companyCode) {
        FmsAccountSetDO accountSet = accountSetMapper.selectByCompanyCode(companyCode);
        if (accountSet != null && ObjUtil.notEqual(id, accountSet.getId())) {
            throw exception(ACCOUNT_SET_CODE_DUPLICATE);
        }
    }

}

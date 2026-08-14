package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword.FmsVoucherWordSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherWordMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsVoucherWordPresetEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_DEFAULT_NOT_DELETABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_DEFAULT_REQUIRED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_IN_USE_NOT_DELETABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_IN_USE_NOT_EDITABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_IN_USE_BY_CLOSING_SCHEME_NOT_DELETABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.VOUCHER_WORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_VOUCHER_WORD_UPDATE_SUCCESS;

/**
 * FMS 凭证字 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsVoucherWordServiceImpl implements FmsVoucherWordService {

    @Resource
    private FmsVoucherWordMapper voucherWordMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    @Lazy // 懒加载，避免循环依赖
    private FmsVoucherService voucherService;
    @Resource
    @Lazy // 懒加载，避免循环依赖
    private FmsClosingSchemeService closingSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_VOUCHER_WORD_TYPE, subType = FMS_VOUCHER_WORD_CREATE_SUB_TYPE,
            bizNo = "{{#voucherWordId}}", success = FMS_VOUCHER_WORD_CREATE_SUCCESS)
    public Long createVoucherWord(FmsVoucherWordSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验凭证字名称唯一
        validateVoucherWordNameUnique(null, createReqVO.getAccountSetId(), createReqVO.getName());
        // 1.3 确定默认状态，并清除原默认凭证字
        FmsVoucherWordDO defaultVoucherWord = voucherWordMapper.selectDefaultByAccountSetId(
                createReqVO.getAccountSetId());
        boolean defaultStatus = Boolean.TRUE.equals(createReqVO.getDefaultStatus())
                || defaultVoucherWord == null;
        if (defaultStatus && defaultVoucherWord != null) {
            voucherWordMapper.updateById(new FmsVoucherWordDO().setId(defaultVoucherWord.getId())
                    .setDefaultStatus(false));
        }

        // 2. 创建凭证字
        FmsVoucherWordDO lastVoucherWord = voucherWordMapper.selectLastByAccountSetId(createReqVO.getAccountSetId());
        FmsVoucherWordDO voucherWord = BeanUtils.toBean(createReqVO, FmsVoucherWordDO.class)
                .setId(null).setDefaultStatus(defaultStatus)
                .setSort(lastVoucherWord == null ? 1 : lastVoucherWord.getSort() + 1);
        voucherWordMapper.insert(voucherWord);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("voucherWordId", voucherWord.getId());
        return voucherWord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_VOUCHER_WORD_TYPE, subType = FMS_VOUCHER_WORD_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_VOUCHER_WORD_UPDATE_SUCCESS)
    public void updateVoucherWord(FmsVoucherWordSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验凭证字存在、使用状态和名称唯一
        FmsVoucherWordDO voucherWord = validateVoucherWordExists(updateReqVO.getAccountSetId(), updateReqVO.getId());
        boolean contentChanged = ObjUtil.notEqual(voucherWord.getName(), updateReqVO.getName())
                || ObjUtil.notEqual(voucherWord.getPrintTitle(), updateReqVO.getPrintTitle());
        if (contentChanged && voucherService.getVoucherCountByVoucherWordId(
                updateReqVO.getAccountSetId(), voucherWord.getId()) > 0) {
            throw exception(VOUCHER_WORD_IN_USE_NOT_EDITABLE);
        }
        validateVoucherWordNameUnique(voucherWord.getId(), updateReqVO.getAccountSetId(), updateReqVO.getName());

        // 1.3 校验并切换默认凭证字
        if (Boolean.TRUE.equals(voucherWord.getDefaultStatus())
                && Boolean.FALSE.equals(updateReqVO.getDefaultStatus())) {
            throw exception(VOUCHER_WORD_DEFAULT_REQUIRED);
        }
        if (Boolean.TRUE.equals(updateReqVO.getDefaultStatus())) {
            FmsVoucherWordDO defaultVoucherWord = voucherWordMapper.selectDefaultByAccountSetId(
                    updateReqVO.getAccountSetId());
            if (defaultVoucherWord != null && ObjUtil.notEqual(defaultVoucherWord.getId(), voucherWord.getId())) {
                voucherWordMapper.updateById(new FmsVoucherWordDO().setId(defaultVoucherWord.getId())
                        .setDefaultStatus(false));
            }
        }

        // 2. 更新凭证字
        voucherWordMapper.updateById(new FmsVoucherWordDO().setId(voucherWord.getId())
                .setName(updateReqVO.getName()).setPrintTitle(updateReqVO.getPrintTitle())
                .setDefaultStatus(updateReqVO.getDefaultStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_VOUCHER_WORD_TYPE, subType = FMS_VOUCHER_WORD_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_VOUCHER_WORD_DELETE_SUCCESS)
    public void deleteVoucherWord(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验凭证字存在且不是默认凭证字
        FmsVoucherWordDO voucherWord = validateVoucherWordExists(accountSetId, id);
        if (Boolean.TRUE.equals(voucherWord.getDefaultStatus())) {
            throw exception(VOUCHER_WORD_DEFAULT_NOT_DELETABLE);
        }
        // 1.3 校验凭证字未被凭证使用
        Long voucherCount = voucherService.getVoucherCountByVoucherWordId(accountSetId, id);
        if (voucherCount > 0) {
            throw exception(VOUCHER_WORD_IN_USE_NOT_DELETABLE, voucherCount);
        }
        // 1.4 校验凭证字未被结账方案使用
        Long closingSchemeCount = closingSchemeService.getClosingSchemeCountByVoucherWordId(accountSetId, id);
        if (closingSchemeCount > 0) {
            throw exception(VOUCHER_WORD_IN_USE_BY_CLOSING_SCHEME_NOT_DELETABLE, closingSchemeCount);
        }

        // 2. 删除凭证字
        voucherWordMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("voucherWord", voucherWord);
    }

    @Override
    public void initializeDefaultVoucherWords(Long accountSetId) {
        List<FmsVoucherWordDO> list = convertList(FmsVoucherWordPresetEnum.values(), preset ->
                new FmsVoucherWordDO().setAccountSetId(accountSetId).setName(preset.getName())
                        .setPrintTitle(preset.getPrintTitle()).setDefaultStatus(preset.getDefaultStatus())
                        .setSort(preset.getSort()));
        voucherWordMapper.insertBatch(list);
    }

    @Override
    public List<FmsVoucherWordDO> getVoucherWordList(Long accountSetId) {
        return voucherWordMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public List<FmsVoucherWordDO> getVoucherWordList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询凭证字列表
        return getVoucherWordList(accountSetId);
    }

    @Override
    public FmsVoucherWordDO validateVoucherWordExists(Long accountSetId, Long id) {
        FmsVoucherWordDO voucherWord = voucherWordMapper.selectById(id);
        if (voucherWord == null || ObjUtil.notEqual(voucherWord.getAccountSetId(), accountSetId)) {
            throw exception(VOUCHER_WORD_NOT_EXISTS);
        }
        return voucherWord;
    }

    private void validateVoucherWordNameUnique(Long id, Long accountSetId, String name) {
        FmsVoucherWordDO voucherWord = voucherWordMapper.selectByAccountSetIdAndName(accountSetId, name);
        if (voucherWord != null && ObjUtil.notEqual(voucherWord.getId(), id)) {
            throw exception(VOUCHER_WORD_NAME_DUPLICATE);
        }
    }

}

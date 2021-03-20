package cn.iocoder.dashboard.modules.system.service.errorcode;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.errorcode.vo.ErrorCodeVO;
import cn.iocoder.dashboard.modules.system.convert.errorcode.ErrorCodeConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.errorcode.ErrorCodeDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.errorcode.ErrorCodeMapper;
import cn.iocoder.dashboard.modules.system.enums.errorcode.ErrorCodeTypeEnum;
import cn.iocoder.dashboard.modules.system.service.errorcode.bo.*;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_DUPLICATE;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_NOT_EXISTS;
import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

/**
 * 错误码 Service
 */
@Slf4j
@Validated
@Service
public class ErrorCodeService {

    @Autowired
    ErrorCodeMapper errorCodeMapper;

    /**
     * 创建错误码
     *
     * @param createBO 创建错误码 BO
     * @return 错误码
     */
    public ErrorCodeBO createErrorCode(@Valid ErrorCodeCreateBO createBO) {
        checkDuplicateErrorCode(createBO.getCode(), null);
        // 插入到数据库
        ErrorCodeDO errorCodeDO = ErrorCodeConvert.INSTANCE.convert(createBO);
        errorCodeMapper.insert(errorCodeDO);
        // 返回
        return ErrorCodeConvert.INSTANCE.convert(errorCodeDO);
    }

    /**
     * 更新错误码
     *
     * @param updateBO 更新错误码 BO
     */
    public void updateErrorCode(@Valid ErrorCodeUpdateBO updateBO) {
        checkDuplicateErrorCode(updateBO.getCode(), updateBO.getId());
        // 校验更新的错误码是否存在
        if (errorCodeMapper.selectById(updateBO.getId()) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS);
        }
        // 更新到数据库
        ErrorCodeDO updateObject = ErrorCodeConvert.INSTANCE.convert(updateBO);
        errorCodeMapper.updateById(updateObject);
    }

    @Transactional
    public void autoGenerateErrorCodes(@Valid List<ErrorCodeAutoGenerateBO> autoGenerateBOs) {
        if (CollUtil.isEmpty(autoGenerateBOs)) {
            return;
        }
        List<ErrorCodeDO> errorCodeDOs = errorCodeMapper.selectListByCodes(
                CollectionUtils.convertSet(autoGenerateBOs, ErrorCodeAutoGenerateBO::getCode));
        Map<Integer, ErrorCodeDO> errorCodeDOMap = CollectionUtils.convertMap(errorCodeDOs, ErrorCodeDO::getCode);
        // 遍历 autoGenerateBOs 数组，逐个插入或更新。考虑到每次量级不大，就不走批量了
        autoGenerateBOs.forEach(autoGenerateBO -> {
            ErrorCodeDO errorCodeDO = errorCodeDOMap.get(autoGenerateBO.getCode());
            // 不存在，则进行新增
            if (errorCodeDO == null) {
                errorCodeDO = ErrorCodeConvert.INSTANCE.convert(autoGenerateBO)
                        .setType(ErrorCodeTypeEnum.AUTO_GENERATION.getType());
                errorCodeMapper.insert(errorCodeDO);
                return;
            }
            // 存在，则进行更新。更新有三个前置条件：
            // 条件 1. 只更新自动生成的错误码，即 Type 为 ErrorCodeTypeEnum.AUTO_GENERATION
            if (!ErrorCodeTypeEnum.AUTO_GENERATION.getType().equals(errorCodeDO.getType())) {
                return;
            }
            // 条件 2. 分组 group 必须匹配，避免存在错误码冲突的情况
            if (!autoGenerateBO.getGroup().equals(errorCodeDO.getGroup())) {
                log.error("[autoGenerateErrorCodes][自动创建({}/{}) 错误码失败，数据库中已经存在({}/{})]",
                        autoGenerateBO.getCode(), autoGenerateBO.getGroup(),
                        errorCodeDO.getCode(), errorCodeDO.getGroup());
                return;
            }
            // 条件 3. 错误提示语存在差异
            if (autoGenerateBO.getMessage().equals(errorCodeDO.getMessage())) {
                return;
            }
            // 最终匹配，进行更新
            errorCodeMapper.updateById(new ErrorCodeDO().setId(errorCodeDO.getId()).setMessage(autoGenerateBO.getMessage()));
        });
    }

    public CommonResult<Boolean> autoGenerateErrorCodes1(@Valid List<ErrorCodeAutoGenerateBO> autoGenerateBOs) {
        autoGenerateErrorCodes(autoGenerateBOs);
        return success(Boolean.TRUE);
    }


    /**
     * 删除错误码
     *
     * @param errorCodeId 错误码编号
     */
    public void deleteErrorCode(Integer errorCodeId) {
        // 校验删除的错误码是否存在
        if (errorCodeMapper.selectById(errorCodeId) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS);
        }
        // 标记删除
        errorCodeMapper.deleteById(errorCodeId);
    }

    /**
     * 获得错误码
     *
     * @param errorCodeId 错误码编号
     * @return 错误码
     */
    public ErrorCodeBO getErrorCode(Integer errorCodeId) {
        ErrorCodeDO errorCodeDO = errorCodeMapper.selectById(errorCodeId);
        return ErrorCodeConvert.INSTANCE.convert(errorCodeDO);
    }

    /**
     * 获得错误码列表
     *
     * @param errorCodeIds 错误码编号列表
     * @return 错误码列表
     */
    public List<ErrorCodeBO> listErrorCodes(List<Integer> errorCodeIds) {
        List<ErrorCodeDO> errorCodeDOs = errorCodeMapper.selectBatchIds(errorCodeIds);
        return ErrorCodeConvert.INSTANCE.convertList(errorCodeDOs);
    }

    /**
     * 获得错误码分页
     *
     * @param pageBO 错误码分页查询
     * @return 错误码分页结果
     */
    public PageResult<ErrorCodeBO> pageErrorCode(ErrorCodePageBO pageBO) {
        IPage<ErrorCodeDO> errorCodeDOPage = errorCodeMapper.selectPage(pageBO);
        return ErrorCodeConvert.INSTANCE.convertPage(errorCodeDOPage);
    }

    /**
     * 校验错误码的唯一字段是否重复
     *
     * 是否存在相同编码的错误码
     *
     * @param code 错误码编码
     * @param id 错误码编号
     */
    private void checkDuplicateErrorCode(Integer code, Integer id) {
        ErrorCodeDO errorCodeDO = errorCodeMapper.selectByCode(code);
        if (errorCodeDO == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的错误码
        if (id == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_DUPLICATE);
        }
        if (!errorCodeDO.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_DUPLICATE);
        }
    }

    public List<ErrorCodeBO> listErrorCodes(String group, Date minUpdateTime) {
        List<ErrorCodeDO> errorCodeDOs = errorCodeMapper.selectListByGroup(group, minUpdateTime);
        return ErrorCodeConvert.INSTANCE.convertList(errorCodeDOs);
    }

    public CommonResult<List<ErrorCodeVO>> listErrorCodes1(String group, Date minUpdateTime) {
        List<ErrorCodeDO> errorCodeDOs = errorCodeMapper.selectListByGroup(group, minUpdateTime);
        final List<ErrorCodeBO> errorCodeBOS = ErrorCodeConvert.INSTANCE.convertList(errorCodeDOs);
        return success(ErrorCodeConvert.INSTANCE.convertList02(errorCodeBOS));
    }

}


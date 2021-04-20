package cn.iocoder.dashboard.modules.system.service.errorcode.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.errorcode.core.dto.ErrorCodeAutoGenerateReqDTO;
import cn.iocoder.dashboard.framework.errorcode.core.dto.ErrorCodeRespDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodeCreateDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodePageDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodeUpdateDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.vo.ErrorCodeVO;
import cn.iocoder.dashboard.modules.infra.convert.errorcode.SysErrorCodeConvert;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.errorcode.InfErrorCodeMapper;
import cn.iocoder.dashboard.modules.system.enums.errorcode.SysErrorCodeTypeEnum;
import cn.iocoder.dashboard.modules.system.service.errorcode.SysErrorCodeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_DUPLICATE;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_NOT_EXISTS;
import static cn.iocoder.dashboard.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.dashboard.util.collection.CollectionUtils.convertSet;

/**
 * 错误码 Service 实现类
 */
@Service
@Validated
@Slf4j
public class ErrorCodeServiceImpl implements SysErrorCodeService {

    @Resource
    private InfErrorCodeMapper errorCodeMapper;

    /**
     * 创建错误码
     *
     * @param createDTO 创建错误码 DTO
     * @return 错误码
     */
    public ErrorCodeVO createErrorCode(ErrorCodeCreateDTO createDTO) {
        checkDuplicateErrorCode(createDTO.getCode(), null);
        // 插入到数据库
        InfErrorCodeDO errorCodeDO = SysErrorCodeConvert.INSTANCE.convert(createDTO);
        errorCodeMapper.insert(errorCodeDO);
        // 返回
        return SysErrorCodeConvert.INSTANCE.convert(errorCodeDO);
    }

    /**
     * 更新错误码
     *
     * @param updateDTO 更新错误码 DTO
     */
    public void updateErrorCode(ErrorCodeUpdateDTO updateDTO) {
        checkDuplicateErrorCode(updateDTO.getCode(), updateDTO.getId());
        // 校验更新的错误码是否存在
        if (errorCodeMapper.selectById(updateDTO.getId()) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS);
        }
        // 更新到数据库
        InfErrorCodeDO updateObject = SysErrorCodeConvert.INSTANCE.convert(updateDTO);
        errorCodeMapper.updateById(updateObject);
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
    public ErrorCodeVO getErrorCode(Integer errorCodeId) {
        InfErrorCodeDO errorCodeDO = errorCodeMapper.selectById(errorCodeId);
        return SysErrorCodeConvert.INSTANCE.convert(errorCodeDO);
    }

    /**
     * 获得错误码列表
     *
     * @param errorCodeIds 错误码编号列表
     * @return 错误码列表
     */
    public List<ErrorCodeVO> listErrorCodes(List<Integer> errorCodeIds) {
        List<InfErrorCodeDO> errorCodeDOs = errorCodeMapper.selectBatchIds(errorCodeIds);
        return SysErrorCodeConvert.INSTANCE.convertList(errorCodeDOs);
    }

    /**
     * 获得错误码分页
     *
     * @param pageDTO 错误码分页查询
     * @return 错误码分页结果
     */
    public PageResult<ErrorCodeVO> pageErrorCode(ErrorCodePageDTO pageDTO) {
        IPage<InfErrorCodeDO> errorCodeDOPage = errorCodeMapper.selectPage(pageDTO);
        return SysErrorCodeConvert.INSTANCE.convertPage(errorCodeDOPage);
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
        InfErrorCodeDO errorCodeDO = errorCodeMapper.selectByCode(code);
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

    @Override
    @Transactional
    public void autoGenerateErrorCodes(List<ErrorCodeAutoGenerateReqDTO> autoGenerateDTOs) {
        if (CollUtil.isEmpty(autoGenerateDTOs)) {
            return;
        }
        // 获得错误码
        List<InfErrorCodeDO> errorCodeDOs = errorCodeMapper.selectListByCodes(
                convertSet(autoGenerateDTOs, ErrorCodeAutoGenerateReqDTO::getCode));
        Map<Integer, InfErrorCodeDO> errorCodeDOMap = convertMap(errorCodeDOs, InfErrorCodeDO::getCode);

        // 遍历 autoGenerateBOs 数组，逐个插入或更新。考虑到每次量级不大，就不走批量了
        autoGenerateDTOs.forEach(autoGenerateDTO -> {
            InfErrorCodeDO errorCodeDO = errorCodeDOMap.get(autoGenerateDTO.getCode());
            // 不存在，则进行新增
            if (errorCodeDO == null) {
                errorCodeDO = SysErrorCodeConvert.INSTANCE.convert(autoGenerateDTO)
                        .setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
                errorCodeMapper.insert(errorCodeDO);
                return;
            }
            // 存在，则进行更新。更新有三个前置条件：
            // 条件 1. 只更新自动生成的错误码，即 Type 为 ErrorCodeTypeEnum.AUTO_GENERATION
            if (!SysErrorCodeTypeEnum.AUTO_GENERATION.getType().equals(errorCodeDO.getType())) {
                return;
            }
            // 条件 2. 分组 group 必须匹配，避免存在错误码冲突的情况
            if (!autoGenerateDTO.getApplicationName().equals(errorCodeDO.getApplicationName())) {
                log.error("[autoGenerateErrorCodes][自动创建({}/{}) 错误码失败，数据库中已经存在({}/{})]",
                        autoGenerateDTO.getCode(), autoGenerateDTO.getApplicationName(),
                        errorCodeDO.getCode(), errorCodeDO.getApplicationName());
                return;
            }
            // 条件 3. 错误提示语存在差异
            if (autoGenerateDTO.getMessage().equals(errorCodeDO.getMessage())) {
                return;
            }
            // 最终匹配，进行更新
            errorCodeMapper.updateById(new InfErrorCodeDO().setId(errorCodeDO.getId()).setMessage(autoGenerateDTO.getMessage()));
        });
    }

    @Override
    public List<ErrorCodeRespDTO> getErrorCodeList(String applicationName, Date minUpdateTime) {
        List<InfErrorCodeDO> errorCodeDOs = errorCodeMapper.selectListByApplicationNameAndUpdateTimeGt(
                applicationName, minUpdateTime);
        return SysErrorCodeConvert.INSTANCE.convertList02(errorCodeDOs);
    }

}


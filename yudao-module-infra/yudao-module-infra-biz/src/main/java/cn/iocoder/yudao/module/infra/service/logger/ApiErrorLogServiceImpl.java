package cn.iocoder.yudao.module.infra.service.logger;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.logger.ApiErrorLogMapper;
import cn.iocoder.yudao.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiErrorLogDO.REQUEST_PARAMS_MAX_LENGTH;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_PROCESSED;

/**
 * API 错误日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ApiErrorLogServiceImpl implements ApiErrorLogService {

    @Resource
    private ApiErrorLogMapper apiErrorLogMapper;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        ApiErrorLogDO apiErrorLog = BeanUtils.toBean(createDTO, ApiErrorLogDO.class)
                .setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());
        apiErrorLog.setRequestParams(StrUtil.maxLength(apiErrorLog.getRequestParams(), REQUEST_PARAMS_MAX_LENGTH));
        if (TenantContextHolder.getTenantId() != null) {
            apiErrorLogMapper.insert(apiErrorLog);
        } else {
            // 极端情况下，上下文中没有租户时，此时忽略租户上下文，避免插入失败！
            TenantUtils.executeIgnore(() -> apiErrorLogMapper.insert(apiErrorLog));
        }
    }

    @Override
    public PageResult<ApiErrorLogDO> getApiErrorLogPage(ApiErrorLogPageReqVO pageReqVO) {
        return apiErrorLogMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateApiErrorLogProcess(Long id, Integer processStatus, Long processUserId) {
        ApiErrorLogDO errorLog = apiErrorLogMapper.selectById(id);
        if (errorLog == null) {
            throw exception(API_ERROR_LOG_NOT_FOUND);
        }
        if (!ApiErrorLogProcessStatusEnum.INIT.getStatus().equals(errorLog.getProcessStatus())) {
            throw exception(API_ERROR_LOG_PROCESSED);
        }
        // 标记处理
        apiErrorLogMapper.updateById(ApiErrorLogDO.builder().id(id).processStatus(processStatus)
                .processUserId(processUserId).processTime(LocalDateTime.now()).build());
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Integer cleanErrorLog(Integer exceedDay, Integer deleteLimit) {
        int count = 0;
        LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
        // 循环删除，直到没有满足条件的数据
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = apiErrorLogMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
            count += deleteCount;
            // 达到删除预期条数，说明到底了
            if (deleteCount < deleteLimit) {
                break;
            }
        }
        return count;
    }

}

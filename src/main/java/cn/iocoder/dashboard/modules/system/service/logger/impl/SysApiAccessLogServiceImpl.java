package cn.iocoder.dashboard.modules.system.service.logger.impl;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogPageReqVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysApiAccessLogConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysApiAccessLogDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.logger.SysApiAccessLogMapper;
import cn.iocoder.dashboard.modules.system.service.logger.SysApiAccessLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * API 访问日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SysApiAccessLogServiceImpl implements SysApiAccessLogService {

    @Resource
    private SysApiAccessLogMapper apiAccessLogMapper;

    @Override
    @Async
    public void createApiAccessLogAsync(@Valid ApiAccessLogCreateDTO createDTO) {
        // 插入
        SysApiAccessLogDO apiAccessLog = SysApiAccessLogConvert.INSTANCE.convert(createDTO);
        apiAccessLogMapper.insert(apiAccessLog);
    }

    @Override
    public SysApiAccessLogDO getApiAccessLog(Long id) {
        return apiAccessLogMapper.selectById(id);
    }

    @Override
    public PageResult<SysApiAccessLogDO> getApiAccessLogPage(SysApiAccessLogPageReqVO pageReqVO) {
        return apiAccessLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SysApiAccessLogDO> getApiAccessLogList(SysApiAccessLogExportReqVO exportReqVO) {
        return apiAccessLogMapper.selectList(exportReqVO);
    }

}

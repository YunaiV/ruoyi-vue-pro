package cn.iocoder.yudao.adminserver.modules.infra.service.logger.impl;

import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogExportReqVO;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogPageReqVO;
import cn.iocoder.yudao.adminserver.modules.infra.dal.mysql.logger.InfApiAccessLogMapper;
import cn.iocoder.yudao.adminserver.modules.infra.service.logger.InfApiAccessLogService;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * API 访问日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InfApiAccessLogServiceImpl implements InfApiAccessLogService {

    @Resource
    private InfApiAccessLogMapper apiAccessLogMapper;

    @Override
    public PageResult<InfApiAccessLogDO> getApiAccessLogPage(InfApiAccessLogPageReqVO pageReqVO) {
        return apiAccessLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InfApiAccessLogDO> getApiAccessLogList(InfApiAccessLogExportReqVO exportReqVO) {
        return apiAccessLogMapper.selectList(exportReqVO);
    }

}

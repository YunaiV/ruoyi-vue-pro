package cn.iocoder.yudao.module.infra.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import cn.iocoder.yudao.module.infra.convert.logger.ApiAccessLogConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.logger.ApiAccessLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * API 访问日志 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class ApiAccessLogServiceImpl implements ApiAccessLogService {

    private static final Integer DELETE_LIMIT = 100;

    @Resource
    private ApiAccessLogMapper apiAccessLogMapper;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        ApiAccessLogDO apiAccessLog = ApiAccessLogConvert.INSTANCE.convert(createDTO);
        apiAccessLogMapper.insert(apiAccessLog);
    }

    @Override
    public PageResult<ApiAccessLogDO> getApiAccessLogPage(ApiAccessLogPageReqVO pageReqVO) {
        return apiAccessLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ApiAccessLogDO> getApiAccessLogList(ApiAccessLogExportReqVO exportReqVO) {
        return apiAccessLogMapper.selectList(exportReqVO);
    }

    @Override
    // TODO j-sentinel：类似 JobLogServiceImpl 的建议
    public void jobCleanAccessLog(Integer accessLogJobDay) {
        TenantUtils.executeIgnore(() -> {
            Integer result = null;
            int count = 0;
            while (result == null || DELETE_LIMIT.equals(result)) {
                result = apiAccessLogMapper.jobCleanAccessLog(accessLogJobDay);
                count += result;
            }
            if (count > 0) {
                // ALTER TABLE...FORCE 会导致表重建发生,这会根据主键索引对表空间中的物理页进行排序。
                // 它将行压缩到页面上并消除可用空间，同时确保数据处于主键查找的最佳顺序。
                apiAccessLogMapper.optimizeTable();
            }
            log.info("定时执行清理访问日志数量({})个", count);
        });
    }

}

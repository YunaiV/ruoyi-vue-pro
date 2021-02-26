package cn.iocoder.dashboard.modules.system.dal.mysql.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysApiAccessLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * API 访问日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SysApiAccessLogMapper extends BaseMapperX<SysApiAccessLogDO> {

    default PageResult<SysApiAccessLogDO> selectPage(SysApiAccessLogPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysApiAccessLogDO>()
                .eqIfPresent("user_id", reqVO.getUserId())
                .eqIfPresent("user_type", reqVO.getUserType())
                .eqIfPresent("application_name", reqVO.getApplicationName())
                .likeIfPresent("request_url", reqVO.getRequestUrl())
                .betweenIfPresent("begin_time", reqVO.getBeginBeginTime(), reqVO.getEndBeginTime())
                .geIfPresent("duration", reqVO.getDuration())
                .eqIfPresent("result_code", reqVO.getResultCode())
        );
    }

    default List<SysApiAccessLogDO> selectList(SysApiAccessLogExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysApiAccessLogDO>()
                .eqIfPresent("user_id", reqVO.getUserId())
                .eqIfPresent("user_type", reqVO.getUserType())
                .eqIfPresent("application_name", reqVO.getApplicationName())
                .likeIfPresent("request_url", reqVO.getRequestUrl())
                .betweenIfPresent("begin_time", reqVO.getBeginBeginTime(), reqVO.getEndBeginTime())
                .geIfPresent("duration", reqVO.getDuration())
                .eqIfPresent("result_code", reqVO.getResultCode())
        );
    }

}

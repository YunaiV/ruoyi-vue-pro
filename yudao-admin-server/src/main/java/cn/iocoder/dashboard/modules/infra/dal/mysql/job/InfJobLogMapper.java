package cn.iocoder.dashboard.modules.infra.dal.mysql.job;

import cn.iocoder.yudao.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.log.InfJobLogExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.log.InfJobLogPageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfJobLogMapper extends BaseMapperX<InfJobLogDO> {

    default PageResult<InfJobLogDO> selectPage(InfJobLogPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<InfJobLogDO>()
                .eqIfPresent("job_id", reqVO.getJobId())
                .likeIfPresent("handler_name", reqVO.getHandlerName())
                .geIfPresent("begin_time", reqVO.getBeginTime())
                .leIfPresent("end_time", reqVO.getEndTime())
                .eqIfPresent("status", reqVO.getStatus())
                .orderByDesc("id") // ID 倒序
        );
    }

    default List<InfJobLogDO> selectList(InfJobLogExportReqVO reqVO) {
        return selectList(new QueryWrapperX<InfJobLogDO>()
                .eqIfPresent("job_id", reqVO.getJobId())
                .likeIfPresent("handler_name", reqVO.getHandlerName())
                .geIfPresent("begin_time", reqVO.getBeginTime())
                .leIfPresent("end_time", reqVO.getEndTime())
                .eqIfPresent("status", reqVO.getStatus())
                .orderByDesc("id") // ID 倒序
        );
    }

}

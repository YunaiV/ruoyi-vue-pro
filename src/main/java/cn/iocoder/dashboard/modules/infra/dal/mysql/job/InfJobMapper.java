package cn.iocoder.dashboard.modules.infra.dal.mysql.job;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobPageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfJobMapper extends BaseMapperX<InfJobDO> {

    default InfJobDO selectByHandlerName(String handlerName) {
        return selectOne("handler_name", handlerName);
    }

    default PageResult<InfJobDO> selectPage(InfJobPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<InfJobDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .likeIfPresent("handler_name", reqVO.getHandlerName())
        );
    }

    default List<InfJobDO> selectList(InfJobExportReqVO reqVO) {
        return selectList(new QueryWrapperX<InfJobDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .likeIfPresent("handler_name", reqVO.getHandlerName())
        );
    }

}

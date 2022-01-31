package cn.iocoder.yudao.module.infra.dal.mysql.job;

import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.InfJobExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.InfJobPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.InfJobDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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
        return selectOne(InfJobDO::getHandlerName, handlerName);
    }

    default PageResult<InfJobDO> selectPage(InfJobPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InfJobDO>()
                .likeIfPresent(InfJobDO::getName, reqVO.getName())
                .eqIfPresent(InfJobDO::getStatus, reqVO.getStatus())
                .likeIfPresent(InfJobDO::getHandlerName, reqVO.getHandlerName())
        );
    }

    default List<InfJobDO> selectList(InfJobExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InfJobDO>()
                .likeIfPresent(InfJobDO::getName, reqVO.getName())
                .eqIfPresent(InfJobDO::getStatus, reqVO.getStatus())
                .likeIfPresent(InfJobDO::getHandlerName, reqVO.getHandlerName())
        );
    }

}

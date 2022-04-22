package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface OperateLogMapper extends BaseMapperX<OperateLogDO> {

    default PageResult<OperateLogDO> selectPage(OperateLogPageReqVO reqVO, Collection<Long> userIds) {
        QueryWrapperX<OperateLogDO> query = new QueryWrapperX<OperateLogDO>()
                .likeIfPresent("module", reqVO.getModule())
                .inIfPresent("user_id", userIds)
                .eqIfPresent("operate_type", reqVO.getType())
                .betweenIfPresent("start_time", reqVO.getBeginTime(), reqVO.getEndTime());
        if (Boolean.TRUE.equals(reqVO.getSuccess())) {
            query.eq("result_code", GlobalErrorCodeConstants.SUCCESS.getCode());
        } else if (Boolean.FALSE.equals(reqVO.getSuccess())) {
            query.gt("result_code", GlobalErrorCodeConstants.SUCCESS.getCode());
        }
        query.orderByDesc("id"); // 降序
        return selectPage(reqVO, query);
    }

    default List<OperateLogDO> selectList(OperateLogExportReqVO reqVO, Collection<Long> userIds) {
        QueryWrapperX<OperateLogDO> query = new QueryWrapperX<OperateLogDO>()
                .likeIfPresent("module", reqVO.getModule())
                .inIfPresent("user_id", userIds)
                .eqIfPresent("operate_type", reqVO.getType())
                .betweenIfPresent("start_time", reqVO.getBeginTime(), reqVO.getEndTime());
        if (Boolean.TRUE.equals(reqVO.getSuccess())) {
            query.eq("result_code", GlobalErrorCodeConstants.SUCCESS.getCode());
        } else if (Boolean.FALSE.equals(reqVO.getSuccess())) {
            query.gt("result_code", GlobalErrorCodeConstants.SUCCESS.getCode());
        }
        query.orderByDesc("id"); // 降序
        return selectList(query);
    }

}

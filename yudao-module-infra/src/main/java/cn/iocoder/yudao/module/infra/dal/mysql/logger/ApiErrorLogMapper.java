package cn.iocoder.yudao.module.infra.dal.mysql.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * API 错误日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ApiErrorLogMapper extends BaseMapperX<ApiErrorLogDO> {

    default PageResult<ApiErrorLogDO> selectPage(ApiErrorLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ApiErrorLogDO>()
                .eqIfPresent(ApiErrorLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ApiErrorLogDO::getUserType, reqVO.getUserType())
                .eqIfPresent(ApiErrorLogDO::getApplicationName, reqVO.getApplicationName())
                .likeIfPresent(ApiErrorLogDO::getRequestUrl, reqVO.getRequestUrl())
                .betweenIfPresent(ApiErrorLogDO::getExceptionTime, reqVO.getExceptionTime())
                .eqIfPresent(ApiErrorLogDO::getProcessStatus, reqVO.getProcessStatus())
                .orderByDesc(ApiErrorLogDO::getId)
        );
    }

    /**
     * 物理删除指定时间之前的日志
     *
     * @param createTime 最大时间
     * @param limit      删除条数，防止一次删除太多
     * @return 删除条数
     */
    @Delete("DELETE FROM infra_api_error_log WHERE create_time < #{createTime} LIMIT #{limit}")
    Integer deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

}

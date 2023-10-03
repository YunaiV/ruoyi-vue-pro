package cn.iocoder.yudao.module.infra.dal.mysql.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.JobLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.JobLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobLogDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 任务日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface JobLogMapper extends BaseMapperX<JobLogDO> {

    default PageResult<JobLogDO> selectPage(JobLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JobLogDO>()
                .eqIfPresent(JobLogDO::getJobId, reqVO.getJobId())
                .likeIfPresent(JobLogDO::getHandlerName, reqVO.getHandlerName())
                .geIfPresent(JobLogDO::getBeginTime, reqVO.getBeginTime())
                .leIfPresent(JobLogDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(JobLogDO::getStatus, reqVO.getStatus())
                .orderByDesc(JobLogDO::getId) // ID 倒序
        );
    }

    default List<JobLogDO> selectList(JobLogExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<JobLogDO>()
                .eqIfPresent(JobLogDO::getJobId, reqVO.getJobId())
                .likeIfPresent(JobLogDO::getHandlerName, reqVO.getHandlerName())
                .geIfPresent(JobLogDO::getBeginTime, reqVO.getBeginTime())
                .leIfPresent(JobLogDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(JobLogDO::getStatus, reqVO.getStatus())
                .orderByDesc(JobLogDO::getId) // ID 倒序
        );
    }

    // 另外，timingJobCleanLog 的具体 sql 这么写，性能是比较差的；可以直接 delete * from job_log where create_time < xxx order by id limit 100;

    /**
     * 目前物理删除只能通过mybatis-plus的注解实现 or mybatis的xml实现
     * 如果写xml的话就需要多写一个映射类
     *
     * @param jobCleanRetainDay 时间限制
     * @param deleteLimit 删除次数的限制
     * @return
     */
    @Delete("DELETE FROM infra_job_log WHERE create_time < #{jobCleanRetainDay} LIMIT #{deleteLimit}")
    Integer deleteByCreateTimeLt(@Param("jobCleanRetainDay") Date jobCleanRetainDay,@Param("deleteLimit")Integer deleteLimit);

    @Update("ALTER TABLE infra_job_log FORCE")
    void optimizeTable();
}

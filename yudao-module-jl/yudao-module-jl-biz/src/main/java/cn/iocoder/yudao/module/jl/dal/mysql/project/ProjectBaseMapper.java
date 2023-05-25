package cn.iocoder.yudao.module.jl.dal.mysql.project;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;

/**
 * 项目管理 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface ProjectBaseMapper extends BaseMapperX<ProjectBaseDO> {

    default PageResult<ProjectBaseDO> selectPage(ProjectBasePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProjectBaseDO>()
                .betweenIfPresent(ProjectBaseDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(ProjectBaseDO::getSalesleadId, reqVO.getSalesleadId())
                .likeIfPresent(ProjectBaseDO::getName, reqVO.getName())
                .eqIfPresent(ProjectBaseDO::getStage, reqVO.getStage())
                .eqIfPresent(ProjectBaseDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProjectBaseDO::getType, reqVO.getType())
                .betweenIfPresent(ProjectBaseDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(ProjectBaseDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(ProjectBaseDO::getManagerId, reqVO.getManagerId())
                .eqIfPresent(ProjectBaseDO::getParticipants, reqVO.getParticipants())
                .orderByDesc(ProjectBaseDO::getId));
    }

    default List<ProjectBaseDO> selectList(ProjectBaseExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProjectBaseDO>()
                .betweenIfPresent(ProjectBaseDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(ProjectBaseDO::getSalesleadId, reqVO.getSalesleadId())
                .likeIfPresent(ProjectBaseDO::getName, reqVO.getName())
                .eqIfPresent(ProjectBaseDO::getStage, reqVO.getStage())
                .eqIfPresent(ProjectBaseDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProjectBaseDO::getType, reqVO.getType())
                .betweenIfPresent(ProjectBaseDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(ProjectBaseDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(ProjectBaseDO::getManagerId, reqVO.getManagerId())
                .eqIfPresent(ProjectBaseDO::getParticipants, reqVO.getParticipants())
                .orderByDesc(ProjectBaseDO::getId));
    }

}

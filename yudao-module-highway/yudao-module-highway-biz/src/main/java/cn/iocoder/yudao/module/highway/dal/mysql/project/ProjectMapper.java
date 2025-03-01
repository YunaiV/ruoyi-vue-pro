package cn.iocoder.yudao.module.highway.dal.mysql.project;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.highway.dal.dataobject.project.ProjectDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.highway.controller.admin.project.vo.*;

/**
 * 项目管理 Mapper
 *
 * @author 研值担当
 */
@Mapper
public interface ProjectMapper extends BaseMapperX<ProjectDO> {

    default PageResult<ProjectDO> selectPage(ProjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProjectDO>()
                .eqIfPresent(ProjectDO::getCode, reqVO.getCode())
                .likeIfPresent(ProjectDO::getPname, reqVO.getPname())
                .orderByDesc(ProjectDO::getId));
    }

}
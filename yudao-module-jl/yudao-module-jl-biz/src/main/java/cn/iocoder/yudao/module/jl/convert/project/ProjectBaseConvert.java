package cn.iocoder.yudao.module.jl.convert.project;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;

/**
 * 项目管理 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface ProjectBaseConvert {

    ProjectBaseConvert INSTANCE = Mappers.getMapper(ProjectBaseConvert.class);

    ProjectBaseDO convert(ProjectBaseCreateReqVO bean);

    ProjectBaseDO convert(ProjectBaseUpdateReqVO bean);

    ProjectBaseRespVO convert(ProjectBaseDO bean);

    List<ProjectBaseRespVO> convertList(List<ProjectBaseDO> list);

    PageResult<ProjectBaseRespVO> convertPage(PageResult<ProjectBaseDO> page);

    List<ProjectBaseExcelVO> convertList02(List<ProjectBaseDO> list);

}

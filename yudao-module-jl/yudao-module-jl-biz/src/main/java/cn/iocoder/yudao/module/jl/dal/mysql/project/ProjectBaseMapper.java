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

}

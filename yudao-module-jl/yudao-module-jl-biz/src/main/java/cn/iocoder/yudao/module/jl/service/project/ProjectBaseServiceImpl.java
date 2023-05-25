package cn.iocoder.yudao.module.jl.service.project;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.project.ProjectBaseConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.project.ProjectBaseMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目管理 Service 实现类
 *
 * @author 惟象科技
 */
@Service
@Validated
public class ProjectBaseServiceImpl implements ProjectBaseService {

    @Resource
    private ProjectBaseMapper projectBaseMapper;

    @Override
    public Long createProjectBase(ProjectBaseCreateReqVO createReqVO) {
        // 插入
        ProjectBaseDO projectBase = ProjectBaseConvert.INSTANCE.convert(createReqVO);
        projectBaseMapper.insert(projectBase);
        // 返回
        return projectBase.getId();
    }

    @Override
    public void updateProjectBase(ProjectBaseUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectBaseExists(updateReqVO.getId());
        // 更新
        ProjectBaseDO updateObj = ProjectBaseConvert.INSTANCE.convert(updateReqVO);
        projectBaseMapper.updateById(updateObj);
    }

    @Override
    public void deleteProjectBase(Long id) {
        // 校验存在
        validateProjectBaseExists(id);
        // 删除
        projectBaseMapper.deleteById(id);
    }

    private void validateProjectBaseExists(Long id) {
        if (projectBaseMapper.selectById(id) == null) {
            throw exception(PROJECT_BASE_NOT_EXISTS);
        }
    }

    @Override
    public ProjectBaseDO getProjectBase(Long id) {
        return projectBaseMapper.selectById(id);
    }

    @Override
    public List<ProjectBaseDO> getProjectBaseList(Collection<Long> ids) {
        return projectBaseMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProjectBaseDO> getProjectBasePage(ProjectBasePageReqVO pageReqVO) {
        return projectBaseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProjectBaseDO> getProjectBaseList(ProjectBaseExportReqVO exportReqVO) {
        return projectBaseMapper.selectList(exportReqVO);
    }

}

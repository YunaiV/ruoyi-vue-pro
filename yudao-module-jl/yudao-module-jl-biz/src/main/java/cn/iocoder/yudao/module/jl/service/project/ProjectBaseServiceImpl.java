package cn.iocoder.yudao.module.jl.service.project;

import cn.iocoder.yudao.module.jl.repository.ProjectBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;

import cn.iocoder.yudao.module.jl.convert.project.ProjectBaseConvert;

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
    private ProjectBaseRepository projectBaseRepository;

    @Override
    public Long createProjectBase(ProjectBaseCreateReqVO createReqVO) {
        // 插入
        ProjectBaseDO projectBase = ProjectBaseConvert.INSTANCE.convert(createReqVO);
        projectBaseRepository.save(projectBase);
        // 返回
        return projectBase.getId();
    }

    @Override
    public void updateProjectBase(ProjectBaseUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectBaseExists(updateReqVO.getId());
        // 更新
        ProjectBaseDO updateObj = ProjectBaseConvert.INSTANCE.convert(updateReqVO);
        projectBaseRepository.save(updateObj);
    }

    @Override
    public void deleteProjectBase(Long id) {
        // 校验存在
        validateProjectBaseExists(id);
        // 删除
        projectBaseRepository.deleteById(id);
    }

    private void validateProjectBaseExists(Long id) {
        if (!projectBaseRepository.existsById(id)) {
            throw exception(PROJECT_BASE_NOT_EXISTS);
        }
    }

    @Override
    public ProjectBaseDO getProjectBase(Long id) {
        return projectBaseRepository.findById(id).orElseThrow(
                () -> exception(PROJECT_BASE_NOT_EXISTS)
        );
    }

    @Override
    public List<ProjectBaseDO> getProjectBaseList(Collection<Long> ids) {
        return (List<ProjectBaseDO>) projectBaseRepository.findAllById(ids);
    }

    @Override
    public Page<ProjectBaseDO> getProjectBasePage(ProjectBasePageReqVO pageParam) {
        Pageable pageable = PageRequest.of(pageParam.getPageNo() - 1, pageParam.getPageSize());
        return projectBaseRepository.findAll(pageable);

    }

    @Override
    public List<ProjectBaseDO> getProjectBaseList(ProjectBaseExportReqVO exportReqVO) {
        return null;
//        return projectBaseRepository.findAll(exportReqVO);
    }

}

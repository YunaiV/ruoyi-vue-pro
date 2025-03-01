package cn.iocoder.yudao.module.highway.service.project;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.highway.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.highway.dal.dataobject.project.ProjectDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.highway.dal.mysql.project.ProjectMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.highway.enums.ErrorCodeConstants.*;

/**
 * 项目管理 Service 实现类
 *
 * @author 研值担当
 */
@Service
@Validated
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectMapper projectMapper;

    @Override
    public Long createProject(ProjectSaveReqVO createReqVO) {
        // 插入
        ProjectDO project = BeanUtils.toBean(createReqVO, ProjectDO.class);
        projectMapper.insert(project);
        // 返回
        return project.getId();
    }

    @Override
    public void updateProject(ProjectSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectExists(updateReqVO.getId());
        // 更新
        ProjectDO updateObj = BeanUtils.toBean(updateReqVO, ProjectDO.class);
        projectMapper.updateById(updateObj);
    }

    @Override
    public void deleteProject(Long id) {
        // 校验存在
        validateProjectExists(id);
        // 删除
        projectMapper.deleteById(id);
    }

    private void validateProjectExists(Long id) {
        if (projectMapper.selectById(id) == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
    }

    @Override
    public ProjectDO getProject(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectDO> getProjectPage(ProjectPageReqVO pageReqVO) {
        return projectMapper.selectPage(pageReqVO);
    }

}
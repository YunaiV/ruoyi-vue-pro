package cn.iocoder.yudao.module.highway.service.project;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.highway.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.highway.dal.dataobject.project.ProjectDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 项目管理 Service 接口
 *
 * @author 研值担当
 */
public interface ProjectService {

    /**
     * 创建项目管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProject(@Valid ProjectSaveReqVO createReqVO);

    /**
     * 更新项目管理
     *
     * @param updateReqVO 更新信息
     */
    void updateProject(@Valid ProjectSaveReqVO updateReqVO);

    /**
     * 删除项目管理
     *
     * @param id 编号
     */
    void deleteProject(Long id);

    /**
     * 获得项目管理
     *
     * @param id 编号
     * @return 项目管理
     */
    ProjectDO getProject(Long id);

    /**
     * 获得项目管理分页
     *
     * @param pageReqVO 分页查询
     * @return 项目管理分页
     */
    PageResult<ProjectDO> getProjectPage(ProjectPageReqVO pageReqVO);

}
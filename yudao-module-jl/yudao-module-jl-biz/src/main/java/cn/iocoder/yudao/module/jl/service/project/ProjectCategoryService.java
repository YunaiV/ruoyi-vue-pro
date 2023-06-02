package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目的实验名目 Service 接口
 *
 */
public interface ProjectCategoryService {

    /**
     * 创建项目的实验名目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectCategory(@Valid ProjectCategoryCreateReqVO createReqVO);

    /**
     * 更新项目的实验名目
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectCategory(@Valid ProjectCategoryUpdateReqVO updateReqVO);

    /**
     * 删除项目的实验名目
     *
     * @param id 编号
     */
    void deleteProjectCategory(Long id);

    /**
     * 获得项目的实验名目
     *
     * @param id 编号
     * @return 项目的实验名目
     */
    Optional<ProjectCategory> getProjectCategory(Long id);

    /**
     * 获得项目的实验名目列表
     *
     * @param ids 编号
     * @return 项目的实验名目列表
     */
    List<ProjectCategory> getProjectCategoryList(Collection<Long> ids);

    /**
     * 获得项目的实验名目分页
     *
     * @param pageReqVO 分页查询
     * @return 项目的实验名目分页
     */
    PageResult<ProjectCategory> getProjectCategoryPage(ProjectCategoryPageReqVO pageReqVO, ProjectCategoryPageOrder orderV0);

    /**
     * 获得项目的实验名目列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目的实验名目列表
     */
    List<ProjectCategory> getProjectCategoryList(ProjectCategoryExportReqVO exportReqVO);

}

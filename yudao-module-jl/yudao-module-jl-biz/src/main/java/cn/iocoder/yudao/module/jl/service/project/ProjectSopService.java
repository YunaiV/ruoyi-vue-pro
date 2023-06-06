package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSop;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目中的实验名目的操作SOP Service 接口
 *
 */
public interface ProjectSopService {

    /**
     * 创建项目中的实验名目的操作SOP
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectSop(@Valid ProjectSopCreateReqVO createReqVO);

    /**
     * 更新项目中的实验名目的操作SOP
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectSop(@Valid ProjectSopUpdateReqVO updateReqVO);

    /**
     * 删除项目中的实验名目的操作SOP
     *
     * @param id 编号
     */
    void deleteProjectSop(Long id);

    /**
     * 获得项目中的实验名目的操作SOP
     *
     * @param id 编号
     * @return 项目中的实验名目的操作SOP
     */
    Optional<ProjectSop> getProjectSop(Long id);

    /**
     * 获得项目中的实验名目的操作SOP列表
     *
     * @param ids 编号
     * @return 项目中的实验名目的操作SOP列表
     */
    List<ProjectSop> getProjectSopList(Collection<Long> ids);

    /**
     * 获得项目中的实验名目的操作SOP分页
     *
     * @param pageReqVO 分页查询
     * @return 项目中的实验名目的操作SOP分页
     */
    PageResult<ProjectSop> getProjectSopPage(ProjectSopPageReqVO pageReqVO, ProjectSopPageOrder orderV0);

    /**
     * 获得项目中的实验名目的操作SOP列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目中的实验名目的操作SOP列表
     */
    List<ProjectSop> getProjectSopList(ProjectSopExportReqVO exportReqVO);

}

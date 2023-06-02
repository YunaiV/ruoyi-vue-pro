package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSchedule;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目安排单 Service 接口
 *
 */
public interface ProjectScheduleService {

    /**
     * 创建项目安排单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectSchedule(@Valid ProjectScheduleCreateReqVO createReqVO);

    /**
     * 更新项目安排单
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectSchedule(@Valid ProjectScheduleUpdateReqVO updateReqVO);

    /**
     * 删除项目安排单
     *
     * @param id 编号
     */
    void deleteProjectSchedule(Long id);

    /**
     * 获得项目安排单
     *
     * @param id 编号
     * @return 项目安排单
     */
    Optional<ProjectSchedule> getProjectSchedule(Long id);

    /**
     * 获得项目安排单列表
     *
     * @param ids 编号
     * @return 项目安排单列表
     */
    List<ProjectSchedule> getProjectScheduleList(Collection<Long> ids);

    /**
     * 获得项目安排单分页
     *
     * @param pageReqVO 分页查询
     * @return 项目安排单分页
     */
    PageResult<ProjectSchedule> getProjectSchedulePage(ProjectSchedulePageReqVO pageReqVO, ProjectSchedulePageOrder orderV0);

    /**
     * 获得项目安排单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目安排单列表
     */
    List<ProjectSchedule> getProjectScheduleList(ProjectScheduleExportReqVO exportReqVO);

}

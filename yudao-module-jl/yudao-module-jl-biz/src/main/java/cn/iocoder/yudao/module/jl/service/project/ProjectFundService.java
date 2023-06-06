package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectFund;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目款项 Service 接口
 *
 */
public interface ProjectFundService {

    /**
     * 创建项目款项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectFund(@Valid ProjectFundCreateReqVO createReqVO);

    /**
     * 更新项目款项
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectFund(@Valid ProjectFundUpdateReqVO updateReqVO);

    /**
     * 删除项目款项
     *
     * @param id 编号
     */
    void deleteProjectFund(Long id);

    /**
     * 获得项目款项
     *
     * @param id 编号
     * @return 项目款项
     */
    Optional<ProjectFund> getProjectFund(Long id);

    /**
     * 获得项目款项列表
     *
     * @param ids 编号
     * @return 项目款项列表
     */
    List<ProjectFund> getProjectFundList(Collection<Long> ids);

    /**
     * 获得项目款项分页
     *
     * @param pageReqVO 分页查询
     * @return 项目款项分页
     */
    PageResult<ProjectFund> getProjectFundPage(ProjectFundPageReqVO pageReqVO, ProjectFundPageOrder orderV0);

    /**
     * 获得项目款项列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目款项列表
     */
    List<ProjectFund> getProjectFundList(ProjectFundExportReqVO exportReqVO);

}

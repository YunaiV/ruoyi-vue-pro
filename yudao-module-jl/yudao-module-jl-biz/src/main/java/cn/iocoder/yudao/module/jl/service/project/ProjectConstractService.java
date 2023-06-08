package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectConstract;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目合同 Service 接口
 *
 */
public interface ProjectConstractService {

    /**
     * 创建项目合同
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectConstract(@Valid ProjectConstractCreateReqVO createReqVO);

    /**
     * 更新项目合同
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectConstract(@Valid ProjectConstractUpdateReqVO updateReqVO);

    /**
     * 删除项目合同
     *
     * @param id 编号
     */
    void deleteProjectConstract(Long id);

    /**
     * 获得项目合同
     *
     * @param id 编号
     * @return 项目合同
     */
    Optional<ProjectConstract> getProjectConstract(Long id);

    /**
     * 获得项目合同列表
     *
     * @param ids 编号
     * @return 项目合同列表
     */
    List<ProjectConstract> getProjectConstractList(Collection<Long> ids);

    /**
     * 获得项目合同分页
     *
     * @param pageReqVO 分页查询
     * @return 项目合同分页
     */
    PageResult<ProjectConstract> getProjectConstractPage(ProjectConstractPageReqVO pageReqVO, ProjectConstractPageOrder orderV0);

    /**
     * 获得项目合同列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目合同列表
     */
    List<ProjectConstract> getProjectConstractList(ProjectConstractExportReqVO exportReqVO);

}

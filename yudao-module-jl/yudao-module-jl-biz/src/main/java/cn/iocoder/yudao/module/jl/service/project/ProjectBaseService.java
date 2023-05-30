package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import org.springframework.data.domain.Page;

/**
 * 项目管理 Service 接口
 *
 * @author 惟象科技
 */
public interface ProjectBaseService {

    /**
     * 创建项目管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectBase(@Valid ProjectBaseCreateReqVO createReqVO);

    /**
     * 更新项目管理
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectBase(@Valid ProjectBaseUpdateReqVO updateReqVO);

    /**
     * 删除项目管理
     *
     * @param id 编号
     */
    void deleteProjectBase(Long id);

    /**
     * 获得项目管理
     *
     * @param id 编号
     * @return 项目管理
     */
    ProjectBaseDO getProjectBase(Long id);

    /**
     * 获得项目管理列表
     *
     * @param ids 编号
     * @return 项目管理列表
     */
    List<ProjectBaseDO> getProjectBaseList(Collection<Long> ids);

    /**
     * 获得项目管理分页
     *
     * @param pageReqVO 分页查询
     * @return 项目管理分页
     */
    Page<ProjectBaseDO> getProjectBasePage(ProjectBasePageReqVO pageReqVO);

    /**
     * 获得项目管理列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目管理列表
     */
    List<ProjectBaseDO> getProjectBaseList(ProjectBaseExportReqVO exportReqVO);

}

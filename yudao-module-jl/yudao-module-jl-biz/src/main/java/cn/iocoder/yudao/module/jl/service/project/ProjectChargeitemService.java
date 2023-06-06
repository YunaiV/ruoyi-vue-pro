package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目中的实验名目的收费项 Service 接口
 *
 */
public interface ProjectChargeitemService {

    /**
     * 创建项目中的实验名目的收费项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectChargeitem(@Valid ProjectChargeitemCreateReqVO createReqVO);

    /**
     * 更新项目中的实验名目的收费项
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectChargeitem(@Valid ProjectChargeitemUpdateReqVO updateReqVO);

    /**
     * 删除项目中的实验名目的收费项
     *
     * @param id 编号
     */
    void deleteProjectChargeitem(Long id);

    /**
     * 获得项目中的实验名目的收费项
     *
     * @param id 编号
     * @return 项目中的实验名目的收费项
     */
    Optional<ProjectChargeitem> getProjectChargeitem(Long id);

    /**
     * 获得项目中的实验名目的收费项列表
     *
     * @param ids 编号
     * @return 项目中的实验名目的收费项列表
     */
    List<ProjectChargeitem> getProjectChargeitemList(Collection<Long> ids);

    /**
     * 获得项目中的实验名目的收费项分页
     *
     * @param pageReqVO 分页查询
     * @return 项目中的实验名目的收费项分页
     */
    PageResult<ProjectChargeitem> getProjectChargeitemPage(ProjectChargeitemPageReqVO pageReqVO, ProjectChargeitemPageOrder orderV0);

    /**
     * 获得项目中的实验名目的收费项列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目中的实验名目的收费项列表
     */
    List<ProjectChargeitem> getProjectChargeitemList(ProjectChargeitemExportReqVO exportReqVO);

}

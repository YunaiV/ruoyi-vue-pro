package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSupply;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目中的实验名目的物资项 Service 接口
 *
 */
public interface ProjectSupplyService {

    /**
     * 创建项目中的实验名目的物资项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectSupply(@Valid ProjectSupplyCreateReqVO createReqVO);

    /**
     * 更新项目中的实验名目的物资项
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectSupply(@Valid ProjectSupplyUpdateReqVO updateReqVO);

    /**
     * 删除项目中的实验名目的物资项
     *
     * @param id 编号
     */
    void deleteProjectSupply(Long id);

    /**
     * 获得项目中的实验名目的物资项
     *
     * @param id 编号
     * @return 项目中的实验名目的物资项
     */
    Optional<ProjectSupply> getProjectSupply(Long id);

    /**
     * 获得项目中的实验名目的物资项列表
     *
     * @param ids 编号
     * @return 项目中的实验名目的物资项列表
     */
    List<ProjectSupply> getProjectSupplyList(Collection<Long> ids);

    /**
     * 获得项目中的实验名目的物资项分页
     *
     * @param pageReqVO 分页查询
     * @return 项目中的实验名目的物资项分页
     */
    PageResult<ProjectSupply> getProjectSupplyPage(ProjectSupplyPageReqVO pageReqVO, ProjectSupplyPageOrder orderV0);

    /**
     * 获得项目中的实验名目的物资项列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目中的实验名目的物资项列表
     */
    List<ProjectSupply> getProjectSupplyList(ProjectSupplyExportReqVO exportReqVO);

}

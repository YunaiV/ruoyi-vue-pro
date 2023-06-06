package cn.iocoder.yudao.module.jl.service.project;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 项目报价 Service 接口
 *
 */
public interface ProjectQuoteService {

    /**
     * 创建项目报价
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectQuote(@Valid ProjectQuoteCreateReqVO createReqVO);

    Long saveProjectQuote(@Valid ProjectQuoteSaveReqVO createReqVO);

    /**
     * 更新项目报价
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectQuote(@Valid ProjectQuoteUpdateReqVO updateReqVO);

    /**
     * 删除项目报价
     *
     * @param id 编号
     */
    void deleteProjectQuote(Long id);

    /**
     * 获得项目报价
     *
     * @param id 编号
     * @return 项目报价
     */
    Optional<ProjectQuote> getProjectQuote(Long id);

    /**
     * 获得项目报价列表
     *
     * @param ids 编号
     * @return 项目报价列表
     */
    List<ProjectQuote> getProjectQuoteList(Collection<Long> ids);

    /**
     * 获得项目报价分页
     *
     * @param pageReqVO 分页查询
     * @return 项目报价分页
     */
    PageResult<ProjectQuote> getProjectQuotePage(ProjectQuotePageReqVO pageReqVO, ProjectQuotePageOrder orderV0);

    /**
     * 获得项目报价列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 项目报价列表
     */
    List<ProjectQuote> getProjectQuoteList(ProjectQuoteExportReqVO exportReqVO);

}

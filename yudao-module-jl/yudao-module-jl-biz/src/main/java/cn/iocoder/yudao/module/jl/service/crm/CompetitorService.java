package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CompetitorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 友商 Service 接口
 *
 * @author 惟象科技
 */
public interface CompetitorService {

    /**
     * 创建友商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCompetitor(@Valid CompetitorCreateReqVO createReqVO);

    /**
     * 更新友商
     *
     * @param updateReqVO 更新信息
     */
    void updateCompetitor(@Valid CompetitorUpdateReqVO updateReqVO);

    /**
     * 删除友商
     *
     * @param id 编号
     */
    void deleteCompetitor(Long id);

    /**
     * 获得友商
     *
     * @param id 编号
     * @return 友商
     */
    CompetitorDO getCompetitor(Long id);

    /**
     * 获得友商列表
     *
     * @param ids 编号
     * @return 友商列表
     */
    List<CompetitorDO> getCompetitorList(Collection<Long> ids);

    /**
     * 获得友商分页
     *
     * @param pageReqVO 分页查询
     * @return 友商分页
     */
    PageResult<CompetitorDO> getCompetitorPage(CompetitorPageReqVO pageReqVO);

    /**
     * 获得友商列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 友商列表
     */
    List<CompetitorDO> getCompetitorList(CompetitorExportReqVO exportReqVO);

}

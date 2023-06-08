package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索中竞争对手的报价 Service 接口
 *
 */
public interface SalesleadCompetitorService {

    /**
     * 创建销售线索中竞争对手的报价
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalesleadCompetitor(@Valid SalesleadCompetitorCreateReqVO createReqVO);

    /**
     * 更新销售线索中竞争对手的报价
     *
     * @param updateReqVO 更新信息
     */
    void updateSalesleadCompetitor(@Valid SalesleadCompetitorUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中竞争对手的报价
     *
     * @param id 编号
     */
    void deleteSalesleadCompetitor(Long id);

    /**
     * 获得销售线索中竞争对手的报价
     *
     * @param id 编号
     * @return 销售线索中竞争对手的报价
     */
    Optional<SalesleadCompetitor> getSalesleadCompetitor(Long id);

    /**
     * 获得销售线索中竞争对手的报价列表
     *
     * @param ids 编号
     * @return 销售线索中竞争对手的报价列表
     */
    List<SalesleadCompetitor> getSalesleadCompetitorList(Collection<Long> ids);

    /**
     * 获得销售线索中竞争对手的报价分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中竞争对手的报价分页
     */
    PageResult<SalesleadCompetitor> getSalesleadCompetitorPage(SalesleadCompetitorPageReqVO pageReqVO, SalesleadCompetitorPageOrder orderV0);

    /**
     * 获得销售线索中竞争对手的报价列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中竞争对手的报价列表
     */
    List<SalesleadCompetitor> getSalesleadCompetitorList(SalesleadCompetitorExportReqVO exportReqVO);

}

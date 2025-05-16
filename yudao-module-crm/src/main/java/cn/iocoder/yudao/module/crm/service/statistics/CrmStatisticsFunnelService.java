package cn.iocoder.yudao.module.crm.service.statistics;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;

import java.util.List;

/**
 * CRM 销售漏斗分析 Service
 *
 * @author HUIHUI
 */
public interface CrmStatisticsFunnelService {

    /**
     * 获得销售漏斗数据
     *
     * @param reqVO 请求
     * @return 销售漏斗数据
     */
    CrmStatisticFunnelSummaryRespVO getFunnelSummary(CrmStatisticsFunnelReqVO reqVO);

    /**
     * 获得商机结束状态统计
     *
     * @param reqVO 请求
     * @return 商机结束状态统计
     */
    List<CrmStatisticsBusinessSummaryByEndStatusRespVO> getBusinessSummaryByEndStatus(CrmStatisticsFunnelReqVO reqVO);

    /**
     * 获取新增商机分析(按日期)
     *
     * @param reqVO 请求
     * @return 新增商机分析
     */
    List<CrmStatisticsBusinessSummaryByDateRespVO> getBusinessSummaryByDate(CrmStatisticsFunnelReqVO reqVO);

    /**
     * 获得商机转化率分析(按日期)
     *
     * @param reqVO 请求
     * @return 商机转化率分析
     */
    List<CrmStatisticsBusinessInversionRateSummaryByDateRespVO> getBusinessInversionRateSummaryByDate(CrmStatisticsFunnelReqVO reqVO);

    /**
     * 获得商机分页(按日期)
     *
     * @param pageVO 请求
     * @return 商机分页
     */
    PageResult<CrmBusinessDO> getBusinessPageByDate(CrmStatisticsFunnelReqVO pageVO);

}

package cn.iocoder.yudao.module.crm.service.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerAreaRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerLevelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerSourceRespVO;

import java.util.List;

/**
 * CRM 客户分析 Service 接口
 *
 * @author dhb52
 */
public interface CrmStatisticsCustomerService {

    /**
     * 总量分析(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByDateRespVO> getCustomerSummaryByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 总量分析(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByUserRespVO> getCustomerSummaryByUser(CrmStatisticsCustomerReqVO reqVO);


    /**
     * 跟进次数分析(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowupSummaryByDateRespVO> getFollowupSummaryByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进次数分析(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowupSummaryByUserRespVO> getFollowupSummaryByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户跟进次数分析(按类型)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowupSummaryByTypeRespVO> getFollowupSummaryByType(CrmStatisticsCustomerReqVO reqVO);


    /**
     * 获取合同摘要信息(客户转化率页面)
     *
     * @param reqVO 请求参数
     * @return 合同摘要列表
     */
    List<CrmStatisticsCustomerContractSummaryRespVO> getContractSummary(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户成交周期(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerDealCycleByDateRespVO> getCustomerDealCycleByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户成交周期(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerDealCycleByUserRespVO> getCustomerDealCycleByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户行业统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerIndustryRespVO> getCustomerIndustry(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户来源统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerSourceRespVO> getCustomerSource(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户级别统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerLevelRespVO> getCustomerLevel(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户地区统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerAreaRespVO> getCustomerArea(CrmStatisticsCustomerReqVO reqVO);

}

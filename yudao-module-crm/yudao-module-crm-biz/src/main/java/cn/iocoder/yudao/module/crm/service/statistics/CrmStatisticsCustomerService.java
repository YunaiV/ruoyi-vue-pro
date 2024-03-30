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
    List<CrmStatisticsFollowUpSummaryByDateRespVO> getFollowUpSummaryByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进次数分析(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByUserRespVO> getFollowUpSummaryByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户跟进次数分析(按类型)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByTypeRespVO> getFollowUpSummaryByType(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户的首次合同、回款信息列表，用于【客户转化率】页面
     *
     * @param reqVO 请求参数
     * @return 客户的首次合同、回款信息列表
     */
    List<CrmStatisticsCustomerContractSummaryRespVO> getContractSummary(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户成交周期(按日期)
     *
     * 成交的定义：客户 customer 在创建出来，到合同 contract 第一次成交的时间差
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

package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 数据统计 员工客户分析 Mapper
 *
 * @author dhb52
 */
@Mapper
public interface CrmStatisticsCustomerMapper {

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerCreateCountGroupByDate(CrmStatisticsCustomerReqVO reqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerDealCountGroupByDate(CrmStatisticsCustomerReqVO reqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerCreateCountGroupByUser(CrmStatisticsCustomerReqVO reqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerDealCountGroupByUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectContractPriceGroupByUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectReceivablePriceGroupByUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO);  // 已经 review

    List<CrmStatisticsFollowupSummaryByDateRespVO> selectFollowupRecordCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByDateRespVO> selectFollowupCustomerCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByUserRespVO> selectFollowupRecordCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByUserRespVO> selectFollowupCustomerCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerContractSummaryRespVO> selectContractSummary(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByTypeRespVO> selectFollowupRecordCountGroupByType(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByDateRespVO> selectCustomerDealCycleGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByUserRespVO> selectCustomerDealCycleGroupByUser(CrmStatisticsCustomerReqVO reqVO);

}

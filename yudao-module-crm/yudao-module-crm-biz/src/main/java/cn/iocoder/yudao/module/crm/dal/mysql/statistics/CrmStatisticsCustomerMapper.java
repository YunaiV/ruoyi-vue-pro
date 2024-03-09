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

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerCreateCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerDealCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerCreateCountGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerDealCountGroupbyUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectContractPriceGroupbyUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectReceivablePriceGroupbyUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO);

    List<CrmStatisticsFollowupSummaryByDateRespVO> selectFollowupRecordCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByDateRespVO> selectFollowupCustomerCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByUserRespVO> selectFollowupRecordCountGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByUserRespVO> selectFollowupCustomerCountGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerContractSummaryRespVO> selectContractSummary(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByTypeRespVO> selectFollowupRecordCountGroupbyType(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByDateRespVO> selectCustomerDealCycleGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByUserRespVO> selectCustomerDealCycleGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

}

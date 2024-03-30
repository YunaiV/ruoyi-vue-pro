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

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerCreateCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerDealCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerCreateCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerDealCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectContractPriceGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectReceivablePriceGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowUpSummaryByDateRespVO> selectFollowUpRecordCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowUpSummaryByDateRespVO> selectFollowUpCustomerCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowUpSummaryByUserRespVO> selectFollowUpRecordCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowUpSummaryByUserRespVO> selectFollowUpCustomerCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerContractSummaryRespVO> selectContractSummary(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowUpSummaryByTypeRespVO> selectFollowUpRecordCountGroupByType(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByDateRespVO> selectCustomerDealCycleGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByUserRespVO> selectCustomerDealCycleGroupByUser(CrmStatisticsCustomerReqVO reqVO);

}

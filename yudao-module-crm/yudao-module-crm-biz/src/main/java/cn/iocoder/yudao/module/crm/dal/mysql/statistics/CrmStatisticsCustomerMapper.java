package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerAreaRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerLevelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze.CrmStatisticCustomerSourceRespVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 数据统计 员工客户分析 Mapper
 *
 * @author dhb52
 */
@Mapper
public interface CrmStatisticsCustomerMapper {

    // TODO @dhb52：拼写，GroupBy。一般 idea 如果出现绿色的警告，可能是单词拼写错误，建议是要修改的哈；
    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerCreateCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerDealCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerCreateCountGroupbyUser(CrmStatisticsCustomerReqVO reqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerDealCountGroupbyUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectContractPriceGroupbyUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO); // 已经 review

    List<CrmStatisticsCustomerSummaryByUserRespVO> selectReceivablePriceGroupbyUser(CrmStatisticsCustomerReqVO crmStatisticsCustomerReqVO);  // 已经 review

    List<CrmStatisticsFollowupSummaryByDateRespVO> selectFollowupRecordCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByDateRespVO> selectFollowupCustomerCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByUserRespVO> selectFollowupRecordCountGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByUserRespVO> selectFollowupCustomerCountGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerContractSummaryRespVO> selectContractSummary(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsFollowupSummaryByTypeRespVO> selectFollowupRecordCountGroupbyType(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByDateRespVO> selectCustomerDealCycleGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerDealCycleByUserRespVO> selectCustomerDealCycleGroupbyUser(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerIndustryRespVO> selectCustomerIndustryListGroupbyIndustryId(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerSourceRespVO> selectCustomerSourceListGroupbySource(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerLevelRespVO> selectCustomerLevelListGroupbyLevel(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerAreaRespVO> selectSummaryListByAreaId(CrmStatisticsCustomerReqVO reqVO);

}

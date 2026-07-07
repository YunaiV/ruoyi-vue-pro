package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 客户分析 Mapper
 *
 * @author dhb52
 */
@Mapper
public interface CrmStatisticsCustomerMapper {

    /**
     * 新建客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerCreateCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 成交客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByDateRespVO> selectCustomerDealCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 新建客户数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerCreateCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 成交客户数(按用户)
     *
     * @param reqVO 请求参数@param reqVO 请求参数@param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByUserRespVO> selectCustomerDealCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 合同总金额(按用户)
     *
     * @return 统计数据@return 统计数据@param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByUserRespVO> selectContractPriceGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 合同回款金额(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerSummaryByUserRespVO> selectReceivablePriceGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进次数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByDateRespVO> selectFollowUpRecordCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByDateRespVO> selectFollowUpCustomerCountGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进次数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByUserRespVO> selectFollowUpRecordCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进客户数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByUserRespVO> selectFollowUpCustomerCountGroupByUser(CrmStatisticsCustomerReqVO reqVO);


    /**
     * 首次合同、回款信息(用于【客户转化率】页面)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerContractSummaryRespVO> selectContractSummary(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 跟进次数(按类型)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsFollowUpSummaryByTypeRespVO> selectFollowUpRecordCountGroupByType(CrmStatisticsCustomerReqVO reqVO);


    /**
     * 客户成交周期(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerDealCycleByDateRespVO> selectCustomerDealCycleGroupByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户成交周期(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerDealCycleByUserRespVO> selectCustomerDealCycleGroupByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户成交周期(按区域)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerDealCycleByAreaRespVO> selectCustomerDealCycleGroupByAreaId(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 客户成交周期(按产品)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsCustomerDealCycleByProductRespVO> selectCustomerDealCycleGroupByProductId(CrmStatisticsCustomerReqVO reqVO);

}

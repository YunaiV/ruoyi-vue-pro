package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

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
     * 进入公海客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    // TODO: @芋艿 模拟数据, 需要增加 crm_owner_record 表
    default List<CrmStatisticsPoolSummaryByDateRespVO> selectPoolCustomerPutCountByDate(CrmStatisticsCustomerReqVO reqVO) {
        LocalDateTime currrentDate = LocalDateTimeUtil.beginOfDay(reqVO.getTimes()[0]);
        LocalDateTime endDate = LocalDateTimeUtil.endOfDay(reqVO.getTimes()[1]);
        List<CrmStatisticsPoolSummaryByDateRespVO> voList = new ArrayList<>();
        while (currrentDate.isBefore(endDate)) {
            voList.add(new CrmStatisticsPoolSummaryByDateRespVO()
                .setTime(LocalDateTimeUtil.format(currrentDate, "yyyy-MM-dd"))
                .setCustomerPutCount(RandomUtil.randomInt(0, 10))
                .setCustomerTakeCount(RandomUtil.randomInt(0, 10)));
            currrentDate = currrentDate.plusDays(1);
        }

        return voList;
    }

    /**
     * 公海领取客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    // TODO: @芋艿 模拟数据, 需要增加 crm_owner_record 表
    default List<CrmStatisticsPoolSummaryByDateRespVO> selectPoolCustomerTakeCountByDate(CrmStatisticsCustomerReqVO reqVO) {
        return selectPoolCustomerPutCountByDate(reqVO);
    }

    /**
     * 进入公海客户数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    // TODO: @芋艿 模拟数据, 需要增加 crm_owner_record 表
    default List<CrmStatisticsPoolSummaryByUserRespVO> selectPoolCustomerPutCountByUser(CrmStatisticsCustomerReqVO reqVO) {
        return convertList(reqVO.getUserIds(), userId ->
            (CrmStatisticsPoolSummaryByUserRespVO) new CrmStatisticsPoolSummaryByUserRespVO()
                .setCustomerPutCount(RandomUtil.randomInt(0, 10))
                .setCustomerTakeCount(RandomUtil.randomInt(0, 10))
                .setOwnerUserId(userId));
    }

    /**
     * 公海领取客户数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    // TODO: @芋艿 模拟数据, 需要增加 crm_owner_record 表
    default List<CrmStatisticsPoolSummaryByUserRespVO> selectPoolCustomerTakeCountByUser(CrmStatisticsCustomerReqVO reqVO) {
        return selectPoolCustomerPutCountByUser(reqVO);
    }

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

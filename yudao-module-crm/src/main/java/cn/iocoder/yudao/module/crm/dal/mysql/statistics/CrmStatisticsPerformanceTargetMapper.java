package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetActualRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 业绩目标完成情况 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmStatisticsPerformanceTargetMapper {

    /**
     * 按月统计合同金额
     *
     * @param reqVO 请求参数
     * @return 按月统计合同金额
     */
    List<CrmStatisticsPerformanceTargetActualRespVO> selectContractActualByMonth(CrmStatisticsPerformanceTargetReqVO reqVO);

    /**
     * 按月统计回款金额
     *
     * @param reqVO 请求参数
     * @return 按月统计回款金额
     */
    List<CrmStatisticsPerformanceTargetActualRespVO> selectReceivableActualByMonth(CrmStatisticsPerformanceTargetReqVO reqVO);

}

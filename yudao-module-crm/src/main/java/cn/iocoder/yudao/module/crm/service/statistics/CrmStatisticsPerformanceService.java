package cn.iocoder.yudao.module.crm.service.statistics;



import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceRespVO;

import java.util.List;

/**
 * CRM 员工绩效统计 Service 接口
 *
 * @author scholar
 */
public interface CrmStatisticsPerformanceService {

    /**
     * 员工签约合同数量分析
     *
     * @param performanceReqVO 排行参数
     * @return 员工签约合同数量排行分析
     */
    List<CrmStatisticsPerformanceRespVO> getContractCountPerformance(CrmStatisticsPerformanceReqVO performanceReqVO);

    /**
     * 员工签约合同金额分析
     *
     * @param performanceReqVO 排行参数
     * @return 员工签约合同金额分析
     */
    List<CrmStatisticsPerformanceRespVO> getContractPricePerformance(CrmStatisticsPerformanceReqVO performanceReqVO);

    /**
     * 员工获得回款金额分析
     *
     * @param performanceReqVO 排行参数
     * @return 员工获得回款金额分析
     */
    List<CrmStatisticsPerformanceRespVO> getReceivablePricePerformance(CrmStatisticsPerformanceReqVO performanceReqVO);


}

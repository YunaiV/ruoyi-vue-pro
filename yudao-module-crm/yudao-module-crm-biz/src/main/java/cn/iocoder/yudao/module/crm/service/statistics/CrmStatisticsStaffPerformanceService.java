package cn.iocoder.yudao.module.crm.service.statistics;



import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceRespVO;

import java.util.List;

/**
 * CRM 员工绩效统计 Service 接口
 *
 * @author scholar
 */
public interface CrmStatisticsStaffPerformanceService {

    /**
     * 员工签约合同数量分析
     *
     * @param staffPerformanceReqVO 排行参数
     * @return 员工签约合同数量排行分析
     */
    List<CrmStatisticsStaffPerformanceRespVO> getContractCountStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO);

    /**
     * 员工签约合同金额分析
     *
     * @param staffPerformanceReqVO 排行参数
     * @return 员工签约合同金额分析
     */
    List<CrmStatisticsStaffPerformanceRespVO> getContractPriceStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO);

    /**
     * 员工获得回款金额分析
     *
     * @param staffPerformanceReqVO 排行参数
     * @return 员工获得回款金额分析
     */
    List<CrmStatisticsStaffPerformanceRespVO> getReceivablePriceStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO);


}

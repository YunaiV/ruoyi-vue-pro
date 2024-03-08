package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceRespVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface CrmStatisticsStaffPerformanceMapper {

    /**
     * 员工签约合同数量
     *
     * @param staffPerformanceReqVO 参数
     * @return 员工签约合同数量
     */
    List<CrmStatisticsStaffPerformanceRespVO> selectContractCountStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO);


    /**
     * 员工签约合同金额
     *
     * @param staffPerformanceReqVO 参数
     * @return 员工签约合同金额
     */
    List<CrmStatisticsStaffPerformanceRespVO> selectContractPriceStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO);

    /**
     * 员工回款金额
     *
     * @param staffPerformanceReqVO 参数
     * @return 员工回款金额
     */
    List<CrmStatisticsStaffPerformanceRespVO> selectReceivablePriceStaffPerformance(CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO);


}

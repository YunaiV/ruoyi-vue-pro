package cn.iocoder.yudao.module.crm.service.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerCountVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;

import java.util.List;

/**
 * CRM 数据统计 员工客户分析 Service 接口
 *
 * @author dhb52
 */
public interface CrmStatisticsCustomerService {

    /**
     * 获取新建客户数量
     *
     * @param reqVO 请求参数
     * @return 新建客户数量统计
     */
    List<CrmStatisticsCustomerCountVO> getTotalCustomerCount(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取成交客户数量
     *
     * @param reqVO 请求参数
     * @return 成交客户数量统计
     */
    List<CrmStatisticsCustomerCountVO> getDealTotalCustomerCount(CrmStatisticsCustomerReqVO reqVO);


    /**
     * 获取客户跟进次数
     *
     * @param reqVO 请求参数
     * @return 客户跟进次数
     */
    List<CrmStatisticsCustomerCountVO> getRecordCount(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取已跟进客户数
     *
     * @param reqVO 请求参数
     * @return 已跟进客户数
     */
    List<CrmStatisticsCustomerCountVO> getDistinctRecordCount(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户跟进方式统计数
     *
     * @param reqVO 请求参数
     * @return 客户跟进方式统计数
     */
    List<CrmStatisticsCustomerCountVO> getRecordTypeCount(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户成交周期
     *
     * @param reqVO 请求参数
     * @return 客户成交周期
     */
    List<CrmStatisticsCustomerCountVO> getCustomerCycle(CrmStatisticsCustomerReqVO reqVO);

}

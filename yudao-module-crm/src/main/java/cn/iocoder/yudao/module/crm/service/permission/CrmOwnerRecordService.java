package cn.iocoder.yudao.module.crm.service.permission;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsPoolSummaryByDateRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsPoolSummaryByUserRespVO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmOwnerRecordCreateReqBO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * CRM 负责人变更记录 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmOwnerRecordService {

    /**
     * 创建负责人变更记录
     *
     * @param createReqBO 创建信息
     * @return 编号
     */
    Long createOwnerRecord(@Valid CrmOwnerRecordCreateReqBO createReqBO);

    /**
     * 批量创建负责人变更记录
     *
     * @param createReqBOs 创建信息
     */
    void createOwnerRecordList(@Valid List<CrmOwnerRecordCreateReqBO> createReqBOs);

    /**
     * 进入公海客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsPoolSummaryByDateRespVO> getPoolCustomerPutCountByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 公海领取客户数(按日期)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsPoolSummaryByDateRespVO> getPoolCustomerTakeCountByDate(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 进入公海客户数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsPoolSummaryByUserRespVO> getPoolCustomerPutCountByUser(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 公海领取客户数(按用户)
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticsPoolSummaryByUserRespVO> getPoolCustomerTakeCountByUser(CrmStatisticsCustomerReqVO reqVO);

}

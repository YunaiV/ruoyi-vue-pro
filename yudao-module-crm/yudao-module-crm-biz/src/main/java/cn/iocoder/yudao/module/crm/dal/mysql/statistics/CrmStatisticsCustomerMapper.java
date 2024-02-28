package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerCountVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 数据统计 员工客户分析 Mapper
 *
 * @author dhb52
 */
@Mapper
public interface CrmStatisticsCustomerMapper {

    List<CrmStatisticsCustomerCountVO> selectCustomerCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerCountVO> selectDealCustomerCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerCountVO> selectRecordCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticsCustomerCountVO> selectDistinctRecordCountGroupbyDate(CrmStatisticsCustomerReqVO reqVO);

}

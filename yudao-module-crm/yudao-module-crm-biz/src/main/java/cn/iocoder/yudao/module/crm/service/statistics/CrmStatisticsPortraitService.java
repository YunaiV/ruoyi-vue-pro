package cn.iocoder.yudao.module.crm.service.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerAreaRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerLevelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerSourceRespVO;

import java.util.List;

/**
 * CRM 客户画像 Service 接口
 *
 * @author HUIHUI
 */
public interface CrmStatisticsPortraitService {

    /**
     * 获取客户地区统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerAreaRespVO> getCustomerArea(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户行业统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerIndustryRespVO> getCustomerIndustry(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户级别统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerLevelRespVO> getCustomerLevel(CrmStatisticsCustomerReqVO reqVO);

    /**
     * 获取客户来源统计数据
     *
     * @param reqVO 请求参数
     * @return 统计数据
     */
    List<CrmStatisticCustomerSourceRespVO> getCustomerSource(CrmStatisticsCustomerReqVO reqVO);

}

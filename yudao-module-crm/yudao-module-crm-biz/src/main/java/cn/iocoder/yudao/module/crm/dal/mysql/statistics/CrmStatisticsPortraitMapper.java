package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerAreaRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerLevelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerSourceRespVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 数据画像 Mapper
 *
 * @author dhb52
 */
@Mapper
public interface CrmStatisticsPortraitMapper {

    List<CrmStatisticCustomerIndustryRespVO> selectCustomerIndustryListGroupbyIndustryId(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerSourceRespVO> selectCustomerSourceListGroupbySource(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerLevelRespVO> selectCustomerLevelListGroupbyLevel(CrmStatisticsCustomerReqVO reqVO);

    List<CrmStatisticCustomerAreaRespVO> selectSummaryListByAreaId(CrmStatisticsCustomerReqVO reqVO);

}

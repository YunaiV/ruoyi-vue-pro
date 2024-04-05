package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 数据画像 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmStatisticsPortraitMapper {

    List<CrmStatisticCustomerAreaRespVO> selectSummaryListGroupByAreaId(CrmStatisticsPortraitReqVO reqVO);

    List<CrmStatisticCustomerIndustryRespVO> selectCustomerIndustryListGroupByIndustryId(CrmStatisticsPortraitReqVO reqVO);

    List<CrmStatisticCustomerSourceRespVO> selectCustomerSourceListGroupBySource(CrmStatisticsPortraitReqVO reqVO);

    List<CrmStatisticCustomerLevelRespVO> selectCustomerLevelListGroupByLevel(CrmStatisticsPortraitReqVO reqVO);

}

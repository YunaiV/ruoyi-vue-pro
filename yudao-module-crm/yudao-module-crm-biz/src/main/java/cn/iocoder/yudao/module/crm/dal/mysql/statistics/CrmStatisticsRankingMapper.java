package cn.iocoder.yudao.module.crm.dal.mysql.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsRanKRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsRankReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 排行榜统计 Mapper
 *
 * @author anhaohao
 */
@Mapper
public interface CrmStatisticsRankingMapper {

    /**
     * 查询合同金额排行榜
     *
     * @param rankReqVO 参数
     * @return 合同金额排行榜
     */
    List<CrmStatisticsRanKRespVO> selectContractPriceRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询回款金额排行榜
     *
     * @param rankReqVO 参数
     * @return 回款金额排行榜
     */
    List<CrmStatisticsRanKRespVO> selectReceivablePriceRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询签约合同数量排行榜
     *
     * @param rankReqVO 参数
     * @return 签约合同数量排行榜
     */
    List<CrmStatisticsRanKRespVO> selectContractCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询产品销量排行榜
     *
     * @param rankReqVO 参数
     * @return 产品销量排行榜
     */
    List<CrmStatisticsRanKRespVO> selectProductSalesRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询新增客户数排行榜
     *
     * @param rankReqVO 参数
     * @return 新增客户数排行榜
     */
    List<CrmStatisticsRanKRespVO> selectCustomerCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询联系人数量排行榜
     *
     * @param rankReqVO 参数
     * @return 联系人数量排行榜
     */
    List<CrmStatisticsRanKRespVO> selectContactsCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询跟进次数排行榜
     *
     * @param rankReqVO 参数
     * @return 跟进次数排行榜
     */
    List<CrmStatisticsRanKRespVO> selectFollowCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 查询跟进客户数排行榜
     *
     * @param rankReqVO 参数
     * @return 跟进客户数排行榜
     */
    List<CrmStatisticsRanKRespVO> selectFollowCustomerCountRank(CrmStatisticsRankReqVO rankReqVO);

}

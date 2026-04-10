package cn.iocoder.yudao.module.crm.service.statistics;


import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.rank.CrmStatisticsRankRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.rank.CrmStatisticsRankReqVO;

import java.util.List;

/**
 * CRM 排行榜统计 Service 接口
 *
 * @author anhaohao
 */
public interface CrmStatisticsRankService {

    /**
     * 获得合同金额排行榜
     *
     * @param rankReqVO 排行参数
     * @return 合同金额排行榜
     */
    List<CrmStatisticsRankRespVO> getContractPriceRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得回款金额排行榜
     *
     * @param rankReqVO 排行参数
     * @return 回款金额排行榜
     */
    List<CrmStatisticsRankRespVO> getReceivablePriceRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得签约合同数量排行榜
     *
     * @param rankReqVO 排行参数
     * @return 签约合同数量排行榜
     */
    List<CrmStatisticsRankRespVO> getContractCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得产品销量排行榜
     *
     * @param rankReqVO 排行参数
     * @return 产品销量排行榜
     */
    List<CrmStatisticsRankRespVO> getProductSalesRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得新增客户数排行榜
     *
     * @param rankReqVO 排行参数
     * @return 新增客户数排行榜
     */
    List<CrmStatisticsRankRespVO> getCustomerCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得联系人数量排行榜
     *
     * @param rankReqVO 排行参数
     * @return 联系人数量排行榜
     */
    List<CrmStatisticsRankRespVO> getContactsCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得跟进次数排行榜
     *
     * @param rankReqVO 排行参数
     * @return 跟进次数排行榜
     */
    List<CrmStatisticsRankRespVO> getFollowCountRank(CrmStatisticsRankReqVO rankReqVO);

    /**
     * 获得跟进客户数排行榜
     *
     * @param rankReqVO 排行参数
     * @return 跟进客户数排行榜
     */
    List<CrmStatisticsRankRespVO> getFollowCustomerCountRank(CrmStatisticsRankReqVO rankReqVO);

}

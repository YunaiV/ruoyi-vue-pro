package cn.iocoder.yudao.module.crm.service.bi;


import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.CrmBiRanKRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.CrmBiRankReqVO;

import java.util.List;

/**
 * CRM BI 排行榜 Service 接口
 *
 * @author anhaohao
 */
public interface CrmBiRankingService {

    /**
     * 获得合同金额排行榜
     *
     * @param rankReqVO 排行参数
     * @return 合同金额排行榜
     */
    List<CrmBiRanKRespVO> getContractPriceRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得回款金额排行榜
     *
     * @param rankReqVO 排行参数
     * @return 回款金额排行榜
     */
    List<CrmBiRanKRespVO> getReceivablePriceRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得签约合同数量排行榜
     *
     * @param rankReqVO 排行参数
     * @return 签约合同数量排行榜
     */
    List<CrmBiRanKRespVO> getContractCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得产品销量排行榜
     *
     * @param rankReqVO 排行参数
     * @return 产品销量排行榜
     */
    List<CrmBiRanKRespVO> getProductSalesRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得新增客户数排行榜
     *
     * @param rankReqVO 排行参数
     * @return 新增客户数排行榜
     */
    List<CrmBiRanKRespVO> getCustomerCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得联系人数量排行榜
     *
     * @param rankReqVO 排行参数
     * @return 联系人数量排行榜
     */
    List<CrmBiRanKRespVO> getContactsCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得跟进次数排行榜
     *
     * @param rankReqVO 排行参数
     * @return 跟进次数排行榜
     */
    List<CrmBiRanKRespVO> getFollowCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 获得跟进客户数排行榜
     *
     * @param rankReqVO 排行参数
     * @return 跟进客户数排行榜
     */
    List<CrmBiRanKRespVO> getFollowCustomerCountRank(CrmBiRankReqVO rankReqVO);

}

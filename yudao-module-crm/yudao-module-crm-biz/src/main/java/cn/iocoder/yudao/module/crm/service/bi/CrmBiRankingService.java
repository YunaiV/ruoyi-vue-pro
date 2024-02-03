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

}

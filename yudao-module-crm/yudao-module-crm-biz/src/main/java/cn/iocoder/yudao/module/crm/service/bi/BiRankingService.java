package cn.iocoder.yudao.module.crm.service.bi;


import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRanKingRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRankingReqVO;

import java.util.List;

/**
 * BI 排行榜 Service 接口
 *
 * @author anhaohao
 */
public interface BiRankingService {

    /**
     * 合同金额排行榜
     *
     * @param biRankingReqVO 参数
     * @return List<BiContractAmountRankingRespVO>
     */
    List<BiRanKingRespVO> contractRanKing(BiRankingReqVO biRankingReqVO);

    /**
     * 回款金额排行榜
     *
     * @param biRankingReqVO 参数
     * @return List<BiContractAmountRankingRespVO>
     */
    List<BiRanKingRespVO> receivablesRanKing(BiRankingReqVO biRankingReqVO);
}

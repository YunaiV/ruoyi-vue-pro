package cn.iocoder.yudao.module.bi.service.ranking;

import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiContractRanKingRespVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiRankReqVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiReceivablesRanKingRespVO;

import java.util.List;

/**
 * BI 排行榜 Service 接口
 *
 * @author anhaohao
 */
public interface BiRankingService { // TODO @anhaohao：第一个方法，和类要有一个空行
    /**
     * 合同金额排行榜
     *
     * @param biRankReqVO 参数
     * @return List<BiContractAmountRankingRespVO>
     */
    List<BiContractRanKingRespVO> contractRanKing(BiRankReqVO biRankReqVO);

    /**
     * 回款金额排行榜
     *
     * @param biRankReqVO 参数
     * @return List<BiContractAmountRankingRespVO>
     */
    List<BiReceivablesRanKingRespVO> receivablesRanKing(BiRankReqVO biRankReqVO);
}

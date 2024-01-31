package cn.iocoder.yudao.module.crm.dal.mysql.bi;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRanKingRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRankingReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author anhaohao
 */
@Mapper
public interface BiRankingMapper extends BaseMapperX {
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

package cn.iocoder.yudao.module.bi.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiContractRanKingRespVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiReceivablesRanKingRespVO;
import cn.iocoder.yudao.module.bi.util.BiTimeUtil;
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
     * @param biTimeEntity 参数
     * @return List<BiContractAmountRankingRespVO>
     */
    List<BiContractRanKingRespVO> contractRanKing(BiTimeUtil.BiTimeEntity biTimeEntity);

    /**
     * 回款金额排行榜
     *
     * @param biTimeEntity 参数
     * @return List<BiContractAmountRankingRespVO>
     */
    List<BiReceivablesRanKingRespVO> receivablesRanKing(BiTimeUtil.BiTimeEntity biTimeEntity);
}

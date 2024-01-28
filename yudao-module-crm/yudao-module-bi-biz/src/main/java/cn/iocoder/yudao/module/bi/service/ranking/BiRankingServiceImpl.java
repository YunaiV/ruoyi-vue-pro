package cn.iocoder.yudao.module.bi.service.ranking;

import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiContractRanKingRespVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiParams;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiRankReqVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiReceivablesRanKingRespVO;
import cn.iocoder.yudao.module.bi.dal.mysql.BiRankingMapper;
import cn.iocoder.yudao.module.bi.util.BiTimeUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anhaohao
 */
@Service(value = "biRankService")
@Validated
public class BiRankingServiceImpl implements BiRankingService {

    @Resource
    private BiRankingMapper biRankingMapper;

    @Override
    public List<BiContractRanKingRespVO> contractRanKing(BiRankReqVO biRankReqVO) {
        BiParams biParams = new BiParams();
        biParams.setType(biRankReqVO.getType());
        biParams.setDeptId(biRankReqVO.getDeptId());
        biParams.setIsUser(0);
        BiTimeUtil.BiTimeEntity biTimeEntity = BiTimeUtil.analyzeType(biParams);
        List<Long> userIds = biTimeEntity.getUserIds();
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return biRankingMapper.contractRanKing(biTimeEntity);
    }

    @Override
    public List<BiReceivablesRanKingRespVO> receivablesRanKing(BiRankReqVO biRankReqVO) {
        BiParams biParams = new BiParams();
        biParams.setType(biRankReqVO.getType());
        biParams.setDeptId(biRankReqVO.getDeptId());
        biParams.setIsUser(0);
        BiTimeUtil.BiTimeEntity biTimeEntity = BiTimeUtil.analyzeType(biParams);
        List<Long> userIds = biTimeEntity.getUserIds();
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return biRankingMapper.receivablesRanKing(biTimeEntity);
    }
}

package cn.iocoder.yudao.module.bi.controller.admin.ranking;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiContractRanKingRespVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiRankReqVO;
import cn.iocoder.yudao.module.bi.controller.admin.ranking.vo.BiReceivablesRanKingRespVO;
import cn.iocoder.yudao.module.bi.service.ranking.BiRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @anhaohao：写了 swagger 注解，不写注释哈
/**
 * @author anhaohao
 */
@Tag(name = "管理后台 - 排行榜")
@RestController
@RequestMapping("/bi/ranking")
@Validated
public class BiRankingController {

    @Resource
    private BiRankingService biRankingService;

    /**
     * 合同金额排行榜
     */
    @GetMapping("/contract-ranking")
    @Operation(summary = "合同金额排行榜")
    @PreAuthorize("@ss.hasPermission('bi:ranking:query')")
    public CommonResult<List<BiContractRanKingRespVO>> contractAmountRanking(BiRankReqVO biRankReqVO) {
        return success(biRankingService.contractRanKing(biRankReqVO));
    }

    /**
     * 回款金额排行榜
     */
    @GetMapping("/receivables-ranking")
    @Operation(summary = "回款金额排行榜")
    @PreAuthorize("@ss.hasPermission('bi:ranking:query')")
    public CommonResult<List<BiReceivablesRanKingRespVO>> receivablesRanKing(BiRankReqVO biRankReqVO) {
        return success(biRankingService.receivablesRanKing(biRankReqVO));
    }

}

package cn.iocoder.yudao.module.crm.controller.admin.bi;



import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRanKingRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRankingReqVO;
import cn.iocoder.yudao.module.crm.service.bi.BiRankingService;
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


@Tag(name = "管理后台 - 排行榜")
@RestController
@RequestMapping("/crm/bi-ranking")
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
    public CommonResult<List<BiRanKingRespVO>> contractAmountRanking(BiRankingReqVO biRankingReqVO) {
        return success(biRankingService.contractRanKing(biRankingReqVO));
    }

    /**
     * 回款金额排行榜
     */
    @GetMapping("/receivables-ranking")
    @Operation(summary = "回款金额排行榜")
    @PreAuthorize("@ss.hasPermission('bi:ranking:query')")
    public CommonResult<List<BiRanKingRespVO>> receivablesRanKing(BiRankingReqVO biRankingReqVO) {
        return success(biRankingService.receivablesRanKing(biRankingReqVO));
    }

}

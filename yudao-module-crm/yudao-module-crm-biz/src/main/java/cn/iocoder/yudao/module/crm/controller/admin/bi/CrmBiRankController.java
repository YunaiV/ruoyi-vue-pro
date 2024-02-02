package cn.iocoder.yudao.module.crm.controller.admin.bi;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.CrmBiRanKRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.CrmBiRankReqVO;
import cn.iocoder.yudao.module.crm.service.bi.CrmBiRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - CRM BI 排行榜")
@RestController
@RequestMapping("/crm/bi-rank")
@Validated
public class CrmBiRankController {

    @Resource
    private CrmBiRankingService rankingService;

    @GetMapping("/get-contract-price-rank")
    @Operation(summary = "获得合同金额排行榜")
    @PreAuthorize("@ss.hasPermission('crm:bi-rank:query')")
    public CommonResult<List<CrmBiRanKRespVO>> getContractPriceRank(@Valid CrmBiRankReqVO rankingReqVO) {
        return success(rankingService.getContractPriceRank(rankingReqVO));
    }

    @GetMapping("/get-receivable-price-rank")
    @Operation(summary = "获得回款金额排行榜")
    @PreAuthorize("@ss.hasPermission('crm:bi-rank:query')")
    public CommonResult<List<CrmBiRanKRespVO>> getReceivablePriceRank(@Valid CrmBiRankReqVO rankingReqVO) {
        return success(rankingService.getReceivablePriceRank(rankingReqVO));
    }

}

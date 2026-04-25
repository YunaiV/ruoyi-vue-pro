package cn.iocoder.yudao.module.product.controller.app.review;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.app.review.vo.AppTrustworthyReviewRespVO;
import cn.iocoder.yudao.module.product.controller.app.review.vo.AppTrustworthyReviewSubmitReqVO;
import cn.iocoder.yudao.module.product.service.review.TrustworthyReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 可信评价（TrustworthyReviewSystem）
 *
 * @author deepay
 */
@Tag(name = "用户 App - 可信评价")
@RestController
@RequestMapping("/product/trustworthy-review")
@Validated
public class AppTrustworthyReviewController {

    @Resource
    private TrustworthyReviewService trustworthyReviewService;

    @PostMapping("/submit")
    @Operation(summary = "提交可信评价（含反欺诈检测 + 区块链存证）")
    public CommonResult<AppTrustworthyReviewRespVO> submitReview(
            @Valid @RequestBody AppTrustworthyReviewSubmitReqVO reqVO) {
        TrustworthyReviewService.ReviewSubmitResultDTO dto = trustworthyReviewService.submitReview(
                getLoginUserId(),
                reqVO.getSpuId(),
                reqVO.getOrderId(),
                reqVO.getOrderItemId(),
                reqVO.getScores(),
                reqVO.getContent(),
                reqVO.getPicUrls(),
                Boolean.TRUE.equals(reqVO.getAnonymous()));

        AppTrustworthyReviewRespVO vo = new AppTrustworthyReviewRespVO();
        vo.setStatus(dto.status);
        vo.setReason(dto.reason);
        vo.setReviewId(dto.reviewId);
        vo.setTrustScore(dto.trustScore);
        vo.setWeightedRating(dto.weightedRating);
        vo.setVisible(dto.visible);
        vo.setBlockchainTaskId(dto.blockchainTaskId);
        vo.setTrustFactors(dto.trustFactors);
        return success(vo);
    }

}

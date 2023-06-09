package cn.iocoder.yudao.module.product.controller.app.comment;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.service.comment.ProductCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

// TODO @puhui999：AppCommentController =》 AppProductCommentController
@Tag(name = "用户 APP - 商品评价")
@RestController
@RequestMapping("/product/comment")
@Validated
public class AppCommentController {

    @Resource
    private ProductCommentService productCommentService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/list")
    @Operation(summary = "获得最近的 n 条商品评价")
    @Parameters({
            @Parameter(name = "spuId", description = "商品 SPU 编号", required = true, example = "1024"),
            @Parameter(name = "count", description = "数量", required = true, example = "10")
    })
    public CommonResult<List<AppCommentRespVO>> getCommentList(@RequestParam("spuId") Long spuId,
                                                               @RequestParam(value = "count", defaultValue = "10") Integer count) {

        List<AppProductPropertyValueDetailRespVO> list = new ArrayList<>();

        AppProductPropertyValueDetailRespVO item1 = new AppProductPropertyValueDetailRespVO();
        item1.setPropertyId(1L);
        item1.setPropertyName("颜色");
        item1.setValueId(1024L);
        item1.setValueName("红色");
        list.add(item1);

        AppProductPropertyValueDetailRespVO item2 = new AppProductPropertyValueDetailRespVO();
        item2.setPropertyId(2L);
        item2.setPropertyName("尺寸");
        item2.setValueId(2048L);
        item2.setValueName("大号");
        list.add(item2);

        AppProductPropertyValueDetailRespVO item3 = new AppProductPropertyValueDetailRespVO();
        item3.setPropertyId(3L);
        item3.setPropertyName("重量");
        item3.setValueId(3072L);
        item3.setValueName("500克");
        list.add(item3);

        // TODO 生成 mock 的数据
        AppCommentRespVO appCommentRespVO = new AppCommentRespVO();
        appCommentRespVO.setUserId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setUserNickname("用户" + new Random().nextInt(100));
        appCommentRespVO.setUserAvatar("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        appCommentRespVO.setId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setAnonymous(new Random().nextBoolean());
        appCommentRespVO.setOrderId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setOrderItemId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setReplyStatus(new Random().nextBoolean());
        appCommentRespVO.setReplyUserId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setReplyContent("回复内容" + new Random().nextInt(100));
        appCommentRespVO.setReplyTime(LocalDateTime.now().minusDays(new Random().nextInt(30)));
        appCommentRespVO.setCreateTime(LocalDateTime.now().minusDays(new Random().nextInt(30)));
        appCommentRespVO.setFinalScore(new Random().nextInt(5) + 1);
        appCommentRespVO.setSpuId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setSpuName("商品" + new Random().nextInt(100));
        appCommentRespVO.setSkuId((long) (new Random().nextInt(100000) + 10000));
        appCommentRespVO.setSkuProperties(list);
        appCommentRespVO.setScores(new Random().nextInt(5) + 1);
        appCommentRespVO.setDescriptionScores(new Random().nextInt(5) + 1);
        appCommentRespVO.setBenefitScores(new Random().nextInt(5) + 1);
        appCommentRespVO.setContent("评论内容" + new Random().nextInt(100));
        appCommentRespVO.setPicUrls(Arrays.asList("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg"));

        return success(Arrays.asList(appCommentRespVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品评价分页")
    public CommonResult<PageResult<AppCommentRespVO>> getCommentPage(@Valid AppCommentPageReqVO pageVO) {
        return success(productCommentService.getCommentPage(pageVO, Boolean.TRUE));
    }

    // TODO @puhui999：方法名改成 getCommentStatistics？然后搞个对应的 vo？想了下，这样更优雅
    @GetMapping("/statistics")
    @Operation(summary = "获得商品的评价统计") // TODO @puhui999：@RequestParam 哈，针对 spuId
    public CommonResult<Map<String, Object>> getCommentStatistics(@Valid Long spuId) {
        // TODO 生成 mock 的数据
        if (true) {
            return success(MapUtil.<String, Object>builder("allCount", 10L).put("goodPercent", "10.33").build());
        }
        return null;
//        return success(productCommentService.getCommentPageTabsCount(spuId, Boolean.TRUE));
    }

    @PostMapping(value = "/create")
    @Operation(summary = "创建商品评价")
    public CommonResult<Boolean> createComment(@RequestBody AppCommentCreateReqVO createReqVO) {
        // TODO: 2023/3/20 要不要判断订单、商品是否存在
        // TODO @ouhui999：这个接口，搞到交易那比较合适；
        MemberUserRespDTO user = memberUserApi.getUser(getLoginUserId());
        productCommentService.createComment(ProductCommentConvert.INSTANCE.convert(user, createReqVO), Boolean.FALSE);
        return success(true);
    }

}

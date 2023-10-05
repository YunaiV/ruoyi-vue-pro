package cn.iocoder.yudao.module.promotion.controller.app.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordSummaryRespVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainRecordConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import cn.iocoder.yudao.module.promotion.enums.bargain.BargainRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainRecordService;
import cn.iocoder.yudao.module.trade.api.order.TradeOrderApi;
import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 砍价记录")
@RestController
@RequestMapping("/promotion/bargain-record")
@Validated
public class AppBargainRecordController {

    @Resource
    private BargainRecordService bargainRecordService;
    @Resource
    private BargainActivityService bargainActivityService;

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private TradeOrderApi tradeOrderApi;

    @GetMapping("/get-summary")
    @Operation(summary = "获得砍价记录的概要信息", description = "用于小程序首页")
    public CommonResult<AppBargainRecordSummaryRespVO> getBargainRecordSummary() {
        // 砍价成功的用户数量
        Integer successUserCount = bargainRecordService.getBargainRecordUserCount(
                BargainRecordStatusEnum.SUCCESS.getStatus());
        if (successUserCount == 0) {
            return success(new AppBargainRecordSummaryRespVO().setSuccessUserCount(0)
                    .setSuccessList(Collections.emptyList()));
        }
        // 砍价成功的用户列表
        List<BargainRecordDO> successList = bargainRecordService.getBargainRecordList(
                BargainRecordStatusEnum.SUCCESS.getStatus(), 7);
        List<BargainActivityDO> activityList = bargainActivityService.getBargainActivityList(
                convertSet(successList, BargainRecordDO::getActivityId));
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                convertSet(successList, BargainRecordDO::getUserId));
        // 拼接返回
        return success(BargainRecordConvert.INSTANCE.convert(successUserCount, successList, activityList, userMap));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得砍价记录的明细")
    // TODO 芋艿：swagger；id  和 activityId 二选一
    public CommonResult<AppBargainRecordDetailRespVO> getBargainRecordDetail(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "activityId", required = false) Long activityId) {
        AppBargainRecordDetailRespVO detail = new AppBargainRecordDetailRespVO();
        detail.setId(1L);
        detail.setUserId(1L);
        detail.setSpuId(1L);
        detail.setSkuId(1L);
        detail.setPrice(500);
        detail.setActivityId(1L);
        detail.setBargainPrice(150);
        detail.setPrice(200);
        detail.setPayPrice(180);
        detail.setStatus(1);
        detail.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        return success(detail);
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价记录的分页")
    public CommonResult<PageResult<AppBargainRecordRespVO>> getBargainRecordPage(PageParam pageParam) {
        PageResult<BargainRecordDO> pageResult = bargainRecordService.getBargainRecordPage(getLoginUserId(), pageParam);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<BargainActivityDO> activityList = bargainActivityService.getBargainActivityList(
                convertSet(pageResult.getList(), BargainRecordDO::getActivityId));
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(
                convertSet(pageResult.getList(), BargainRecordDO::getSpuId));
        List<TradeOrderRespDTO> orderList = tradeOrderApi.getOrderList(
                convertSet(pageResult.getList(), BargainRecordDO::getOrderId));
        return success(BargainRecordConvert.INSTANCE.convertPage02(pageResult, activityList, spuList, orderList));
    }

    @PostMapping("/create")
    @Operation(summary = "创建砍价记录", description = "参与砍价活动")
    @PreAuthenticated
    public CommonResult<Long> createBargainRecord(@RequestBody AppBargainRecordCreateReqVO reqVO) {
        Long recordId = bargainRecordService.createBargainRecord(getLoginUserId(), reqVO);
        return success(recordId);
    }

}

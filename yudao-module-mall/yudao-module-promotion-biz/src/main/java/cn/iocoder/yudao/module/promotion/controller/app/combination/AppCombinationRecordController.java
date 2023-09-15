package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordSummaryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-record")
@Validated
public class AppCombinationRecordController {

    @GetMapping("/get-summary")
    @Operation(summary = "获得拼团记录的概要信息", description = "用于小程序首页")
    // TODO 芋艿：增加 @Cache 缓存，1 分钟过期
    public CommonResult<AppCombinationRecordSummaryRespVO> getCombinationRecordSummary() {
        AppCombinationRecordSummaryRespVO summary = new AppCombinationRecordSummaryRespVO();
        summary.setUserCount(1024);
        summary.setAvatars(new ArrayList<>());
        summary.getAvatars().add("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTLjFK35Wvia9lJKHoXfQuHhk0qZbvpPNxrAiaEKF7aL2k4I8kuqrdTWwliamdPHeyAA7DjAg725X2GIQ/132");
        summary.getAvatars().add("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTK1pXgdj5DvBMwrbe8v3tFibSWeQATEsAibt3fllD8XwJ460P2r6KS3WCQvDefuv1bVpDhNCle6CTCA/132");
        summary.getAvatars().add("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL7KRGHBE62N0awFyBesmmxiaCicf1fJ7E7UCh6zA8GWlT1QC1zT01gG4OxI7BWDESkdPZ5o7tno4hA/132");
        summary.getAvatars().add("https://thirdwx.qlogo.cn/mmopen/vi_32/ouwtwJycbic2JrCoZjETict0klxd1uRuicRneKk00ewMcCClxVcVHQT91Sh9MJGtwibf1fOicD1WpwSP4icJM6eQq1AA/132");
        summary.getAvatars().add("https://thirdwx.qlogo.cn/mmopen/vi_32/RpUrhwens58qc99OcGs993xL4M5QPOe05ekqF9Eia440kRicAlicicIdQWicHBmy2bzLgHzHguWEzHHxnIgeictL7bLA/132");
        summary.getAvatars().add("https://thirdwx.qlogo.cn/mmopen/vi_32/S4tfqmxc8GZGsKc1K4mnhpvtG16gtMrLnTQfDibhr7jJich9LRI5RQKZDoqEjZM3azMib5nic7F4ZXKMEgYyLO08KA/132");
        summary.getAvatars().add("https://static.iocoder.cn/mall/132.jpeg");
        return success(summary);
    }

    @GetMapping("/get-head-list")
    @Operation(summary = "获得最近 n 条拼团记录（团长发起的）")
    // TODO @芋艿：注解要补全
    public CommonResult<List<AppCombinationRecordRespVO>> getHeadCombinationRecordList(
            @RequestParam(value = "activityId", required = false) Long activityId,
            @RequestParam("status") Integer status,
            @RequestParam(value = "count", defaultValue = "20") @Max(20) Integer count) {
        List<AppCombinationRecordRespVO> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            AppCombinationRecordRespVO record = new AppCombinationRecordRespVO();
            record.setId((long) i);
            record.setNickname("用户" + i);
            record.setAvatar("头像" + i);
            record.setExpireTime(LocalDateTime.now());
            record.setUserSize(10);
            record.setUserCount(i);
            record.setPicUrl("https://static.iocoder.cn/mall/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
            record.setActivityId(1L);
            record.setSpuName("活动：" + i);
            list.add(record);
        }
        return success(list);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团记录明细")
    @Parameter(name = "id", description = "拼团记录编号", required = true, example = "1024")
    public CommonResult<AppCombinationRecordDetailRespVO> getCombinationRecordDetail(@RequestParam("id") Long id) {
        AppCombinationRecordDetailRespVO detail = new AppCombinationRecordDetailRespVO();
        // 团长
        AppCombinationRecordRespVO headRecord = new AppCombinationRecordRespVO();
        headRecord.setId(1L);
        headRecord.setNickname("用户" + 1);
        headRecord.setAvatar("头像" + 1);
        headRecord.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(1)));
        headRecord.setUserSize(10);
        headRecord.setUserCount(3);
        headRecord.setStatus(1);
        headRecord.setActivityId(10L);
        headRecord.setPicUrl("https://static.iocoder.cn/mall/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        headRecord.setCombinationPrice(100);
        detail.setHeadRecord(headRecord);
        // 团员
        List<AppCombinationRecordRespVO> list = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            AppCombinationRecordRespVO record = new AppCombinationRecordRespVO();
            record.setId((long) i);
            record.setNickname("用户" + i);
            record.setAvatar("头像" + i);
            record.setExpireTime(LocalDateTime.now());
            record.setUserSize(10);
            record.setUserCount(i);
            record.setStatus(1);
            list.add(record);
        }
        detail.setMemberRecords(list);
        // 订单编号
        detail.setOrderId(100L);
        return success(detail);
    }

}

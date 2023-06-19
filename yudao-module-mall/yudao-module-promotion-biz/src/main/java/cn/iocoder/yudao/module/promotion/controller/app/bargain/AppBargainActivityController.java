package cn.iocoder.yudao.module.promotion.controller.app.bargain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity.AppBargainActivityRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 砍价活动")
@RestController
@RequestMapping("/promotion/bargain-activity")
@Validated
public class AppBargainActivityController {

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动活动") // TODO 芋艿：只查询进行中，且在时间范围内的
    // TODO 芋艿：缺少 swagger 注解
    public CommonResult<PageResult<AppBargainActivityRespVO>> getBargainActivityPage(PageParam pageReqVO) {
        List<AppBargainActivityRespVO> activityList = new ArrayList<>();
        AppBargainActivityRespVO activity1 = new AppBargainActivityRespVO();
        activity1.setId(1L);
        activity1.setName("618 大砍价");
        activity1.setSpuId(2048L);
        activity1.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        activity1.setMarketPrice(50);
        activity1.setBargainPrice(100);
        activity1.setStartTime(LocalDateTimeUtils.addTime(Duration.ofDays(-2)));
        activity1.setEndTime(LocalDateTimeUtils.addTime(Duration.ofDays(1)));
        activity1.setStock(10);
        activityList.add(activity1);

        AppBargainActivityRespVO activity2 = new AppBargainActivityRespVO();
        activity2.setId(2L);
        activity2.setName("双十一砍价");
        activity2.setSpuId(4096L);
        activity2.setPicUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKXMYJOomfp7cebz3cIeb8sHk3GGSIJtWEgREe3j7J1WoAbTvIOicpcNdFkWAziatBSMod8b5RyS4CQ/132");
        activity2.setMarketPrice(100);
        activity2.setBargainPrice(200);
        activity2.setStartTime(LocalDateTimeUtils.addTime(Duration.ofDays(-2)));
        activity2.setEndTime(LocalDateTimeUtils.addTime(Duration.ofDays(1)));
        activity2.setStock(0);
        activityList.add(activity2);

        return success(new PageResult<>(activityList, 10L));
    }

    @GetMapping("/list")
    @Operation(summary = "获得砍价活动列表", description = "用于小程序首页")
    // TODO 芋艿：增加 Spring Cache
    // TODO 芋艿：缺少 swagger 注解
    public CommonResult<List<AppBargainActivityRespVO>> getBargainActivityList(
            @RequestParam(name = "count", defaultValue = "6") Integer count) {
        List<AppBargainActivityRespVO> activityList = new ArrayList<>();
        AppBargainActivityRespVO activity1 = new AppBargainActivityRespVO();
        activity1.setId(1L);
        activity1.setName("618 大砍价");
        activity1.setSpuId(2048L);
        activity1.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        activity1.setMarketPrice(50);
        activity1.setBargainPrice(100);
        activityList.add(activity1);

        AppBargainActivityRespVO activity2 = new AppBargainActivityRespVO();
        activity2.setId(2L);
        activity2.setName("双十一砍价");
        activity2.setSpuId(4096L);
        activity2.setPicUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKXMYJOomfp7cebz3cIeb8sHk3GGSIJtWEgREe3j7J1WoAbTvIOicpcNdFkWAziatBSMod8b5RyS4CQ/132");
        activity2.setMarketPrice(100);
        activity2.setBargainPrice(200);
        activityList.add(activity2);

        return success(activityList);
    }

}

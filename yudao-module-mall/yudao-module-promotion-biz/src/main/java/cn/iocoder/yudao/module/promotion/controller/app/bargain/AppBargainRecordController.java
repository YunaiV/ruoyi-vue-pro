package cn.iocoder.yudao.module.promotion.controller.app.bargain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordSummaryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 砍价记录")
@RestController
@RequestMapping("/promotion/bargain-record")
@Validated
public class AppBargainRecordController {

    @GetMapping("/get-summary")
    @Operation(summary = "获得砍价记录的概要信息", description = "用于小程序首页")
    // TODO 芋艿：增加 @Cache 缓存，1 分钟过期
    public CommonResult<AppBargainRecordSummaryRespVO> getBargainRecordSummary() {
        AppBargainRecordSummaryRespVO summary = new AppBargainRecordSummaryRespVO();
        summary.setUserCount(1024);
        summary.setSuccessRecords(new ArrayList<>());
        AppBargainRecordSummaryRespVO.Record record1 = new AppBargainRecordSummaryRespVO.Record();
        record1.setNickname("王**");
        record1.setAvatar("https://www.iocoder.cn/xxx.jpg");
        record1.setActivityName("天蚕土豆");
        AppBargainRecordSummaryRespVO.Record record2 = new AppBargainRecordSummaryRespVO.Record();
        record2.setNickname("张**");
        record2.setAvatar("https://www.iocoder.cn/yyy.jpg");
        record2.setActivityName("斗罗大陆");
        summary.getSuccessRecords().add(record1);
        summary.getSuccessRecords().add(record2);
        return success(summary);
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
        detail.setAction(AppBargainRecordDetailRespVO.ACTION_SUCCESS);
        detail.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        return success(detail);
    }

}

package cn.iocoder.yudao.module.promotion.controller.app.bargain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordSummaryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        detail.setAction(AppBargainRecordDetailRespVO.ACTION_ORDER_CREATE);
        detail.setHelpAction(AppBargainRecordDetailRespVO.HELP_ACTION_NONE);
        detail.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        return success(detail);
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价记录的分页")
    public CommonResult<PageResult<AppBargainRecordRespVO>> getBargainRecordPage(PageParam pageParam) {
        PageResult<AppBargainRecordRespVO> page = new PageResult<>();
        page.setList(new ArrayList<>());
        AppBargainRecordRespVO record1 = new AppBargainRecordRespVO();
        record1.setId(1L);
        record1.setUserId(1L);
        record1.setSpuId(1L);
        record1.setSkuId(1L);
        record1.setPrice(500);
        record1.setActivityId(1L);
        record1.setBargainPrice(150);
        record1.setPrice(200);
        record1.setPayPrice(180);
        record1.setStatus(1);
        record1.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        record1.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        page.getList().add(record1);

        AppBargainRecordRespVO record2 = new AppBargainRecordRespVO();
        record2.setId(1L);
        record2.setUserId(1L);
        record2.setSpuId(1L);
        record2.setSkuId(1L);
        record2.setPrice(500);
        record2.setActivityId(1L);
        record2.setBargainPrice(150);
        record2.setPrice(200);
        record2.setPayPrice(280);
        record2.setStatus(2);
        record2.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        record2.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        page.getList().add(record2);

        AppBargainRecordRespVO record3 = new AppBargainRecordRespVO();
        record3.setId(1L);
        record3.setUserId(1L);
        record3.setSpuId(1L);
        record3.setSkuId(1L);
        record3.setPrice(500);
        record3.setActivityId(1L);
        record3.setBargainPrice(150);
        record3.setPrice(200);
        record3.setPayPrice(380);
        record3.setStatus(2);
        record3.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        record3.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        record3.setOrderId(100L);
        page.getList().add(record3);

        AppBargainRecordRespVO record4 = new AppBargainRecordRespVO();
        record4.setId(1L);
        record4.setUserId(1L);
        record4.setSpuId(1L);
        record4.setSkuId(1L);
        record4.setPrice(500);
        record4.setActivityId(1L);
        record4.setBargainPrice(150);
        record4.setPrice(200);
        record4.setPayPrice(380);
        record4.setStatus(3);
        record4.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        record4.setExpireTime(LocalDateTimeUtils.addTime(Duration.ofDays(2)));
        record4.setOrderId(100L);
        page.getList().add(record4);

        page.setTotal(1L);
        return success(page);
    }

    @PostMapping("/create")
    @Operation(summary = "创建砍价记录", description = "参与拼团活动")
    public CommonResult<Long> createBargainRecord(@RequestBody AppBargainRecordCreateReqVO reqVO) {
         return success(1L);
    }

}

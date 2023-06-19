package cn.iocoder.yudao.module.promotion.controller.app.bargain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordSummaryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

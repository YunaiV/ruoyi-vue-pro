package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordSimpleRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-record")
@Validated
public class AppCombinationRecordController {

    @GetMapping("/get-head-list")
    @Operation(summary = "获得最近 n 条拼团记录（团长发起的）")
    public CommonResult<List<AppCombinationRecordSimpleRespVO>> getHeadCombinationRecordList(
            @RequestParam("status") Integer status,
            @RequestParam(value = "count", defaultValue = "20") @Max(20) Integer count) {
        List<AppCombinationRecordSimpleRespVO> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            AppCombinationRecordSimpleRespVO record = new AppCombinationRecordSimpleRespVO();
            record.setId((long) i);
            record.setNickname("用户" + i);
            record.setAvatar("头像" + i);
            record.setExpireTime(new Date());
            record.setUserSize(10);
            record.setUserCount(i);
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
        AppCombinationRecordSimpleRespVO headRecord = new AppCombinationRecordSimpleRespVO();
        headRecord.setId(1L);
        headRecord.setNickname("用户" + 1);
        headRecord.setAvatar("头像" + 1);
        headRecord.setExpireTime(new Date());
        headRecord.setUserSize(10);
        headRecord.setUserCount(3);
        // 团员
        List<AppCombinationRecordSimpleRespVO> list = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            AppCombinationRecordSimpleRespVO record = new AppCombinationRecordSimpleRespVO();
            record.setId((long) i);
            record.setNickname("用户" + i);
            record.setAvatar("头像" + i);
            record.setExpireTime(new Date());
            record.setUserSize(10);
            record.setUserCount(i);
            list.add(record);
        }
        detail.setMemberRecords(list);
        // 订单编号
        detail.setOrderId(100L);
        return success(detail);
    }

}

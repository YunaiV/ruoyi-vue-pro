package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record.AppCombinationRecordSummaryRespVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-record")
@Validated
public class AppCombinationRecordController {

    @Resource
    private CombinationRecordService combinationRecordService;

    @GetMapping("/get-summary")
    @Operation(summary = "获得拼团记录的概要信息", description = "用于小程序首页")
    public CommonResult<AppCombinationRecordSummaryRespVO> getCombinationRecordSummary() {
        AppCombinationRecordSummaryRespVO summary = new AppCombinationRecordSummaryRespVO();
        // 1. 获得拼团记录数量
        Long count = combinationRecordService.getCombinationRecordCount();
        if (count == 0) {
            summary.setAvatars(Collections.emptyList());
            summary.setUserCount(count);
            return success(summary);
        }
        summary.setUserCount(count);

        // 2. 获得拼团记录头像
        List<CombinationRecordDO> records = combinationRecordService.getLatestCombinationRecordList(
                AppCombinationRecordSummaryRespVO.AVATAR_COUNT);
        summary.setAvatars(convertList(records, CombinationRecordDO::getAvatar));
        return success(summary);
    }

    @GetMapping("/get-head-list")
    @Operation(summary = "获得最近 n 条拼团记录（团长发起的）")
    @Parameters({
            @Parameter(name = "activityId", description = "拼团活动编号"),
            @Parameter(name = "status", description = "拼团状态"), // 对应 CombinationRecordStatusEnum 枚举
            @Parameter(name = "count", description = "数量")
    })
    public CommonResult<List<AppCombinationRecordRespVO>> getHeadCombinationRecordList(
            @RequestParam(value = "activityId", required = false) Long activityId,
            @RequestParam("status") Integer status,
            @RequestParam(value = "count", defaultValue = "20") @Max(20) Integer count) {
        return success(CombinationActivityConvert.INSTANCE.convertList3(
                combinationRecordService.getHeadCombinationRecordList(activityId, status, count)));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团记录明细")
    @Parameter(name = "id", description = "拼团记录编号", required = true, example = "1024")
    public CommonResult<AppCombinationRecordDetailRespVO> getCombinationRecordDetail(@RequestParam("id") Long id) {
        // 1. 查找这条拼团记录
        CombinationRecordDO record = combinationRecordService.getCombinationRecordById(id);
        if (record == null) {
            return success(null);
        }

        // 2. 查找该拼团的参团记录
        CombinationRecordDO headRecord;
        List<CombinationRecordDO> memberRecords;
        if (Objects.equals(record.getHeadId(), CombinationRecordDO.HEAD_ID_GROUP)) { // 情况一：团长
            headRecord = record;
            memberRecords = combinationRecordService.getCombinationRecordListByHeadId(record.getId());
        } else { // 情况二：团员
            headRecord = combinationRecordService.getCombinationRecordById(record.getHeadId());
            memberRecords = combinationRecordService.getCombinationRecordListByHeadId(headRecord.getId());
        }

        // 3. 拼接数据
        return success(CombinationActivityConvert.INSTANCE.convert(getLoginUserId(), headRecord, memberRecords));
    }

    // TODO @puhui：新增一个取消拼团的接口，cancel
    // 1. 需要先校验拼团记录未完成；
    // 2. 在 Order 那增加一个 cancelPaidOrder 接口，用于取消已支付的订单
    // 3. order 完成后，取消拼团记录。另外，如果它是团长，则顺序（下单时间）继承

}

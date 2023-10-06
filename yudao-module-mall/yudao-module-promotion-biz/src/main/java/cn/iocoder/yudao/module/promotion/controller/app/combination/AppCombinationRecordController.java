package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
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
    // TODO 芋艿：增加 @Cache 缓存，1 分钟过期
    public CommonResult<AppCombinationRecordSummaryRespVO> getCombinationRecordSummary() {
        // 获取所有拼团记录
        Long count = combinationRecordService.getCombinationRecordsCount();
        AppCombinationRecordSummaryRespVO summary = new AppCombinationRecordSummaryRespVO();
        if (count == null || count == 0L) {
            summary.setAvatars(Collections.emptyList());
            summary.setUserCount(count);
            return success(summary);
        }

        summary.setUserCount(count);
        // TODO 只返回最近的 7 个
        int num = 7;
        summary.setAvatars(convertList(combinationRecordService.getLatestCombinationRecordList(num), CombinationRecordDO::getAvatar));
        return success(summary);
    }

    @GetMapping("/get-head-list")
    @Operation(summary = "获得最近 n 条拼团记录（团长发起的）")
    @Parameters({
            @Parameter(name = "activityId", description = "拼团活动编号"),
            @Parameter(name = "status", description = "状态"),
            @Parameter(name = "count", description = "数量")
    })
    public CommonResult<List<AppCombinationRecordRespVO>> getHeadCombinationRecordList(
            @RequestParam(value = "activityId", required = false) Long activityId,
            @RequestParam("status") Integer status,
            @RequestParam(value = "count", defaultValue = "20") @Max(20) Integer count) {
        return success(CombinationActivityConvert.INSTANCE.convertList3(
                combinationRecordService.getCombinationRecordListWithHead(activityId, status, count)));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团记录明细")
    @Parameter(name = "id", description = "拼团记录编号", required = true, example = "1024")
    public CommonResult<AppCombinationRecordDetailRespVO> getCombinationRecordDetail(@RequestParam("id") Long id) {
        // 1、查询这条记录
        CombinationRecordDO record = combinationRecordService.getCombinationRecordById(id);
        if (record == null) {
            return success(null);
        }

        AppCombinationRecordDetailRespVO detail = new AppCombinationRecordDetailRespVO();
        List<CombinationRecordDO> records;
        // 2、判断是否为团长
        if (record.getHeadId() == null) {
            detail.setHeadRecord(CombinationActivityConvert.INSTANCE.convert(record));
            // 2.1、查找团员拼团记录
            records = combinationRecordService.getCombinationRecordListByHeadId(record.getId());
        } else {
            // 2.2、查找团长拼团记录
            CombinationRecordDO headRecord = combinationRecordService.getCombinationRecordById(record.getHeadId());
            if (headRecord == null) {
                return success(null);
            }

            detail.setHeadRecord(CombinationActivityConvert.INSTANCE.convert(headRecord));
            // 2.3、查找团员拼团记录
            records = combinationRecordService.getCombinationRecordListByHeadId(headRecord.getId());

        }
        detail.setMemberRecords(CombinationActivityConvert.INSTANCE.convertList3(records));

        // 3、获取当前用户参团记录订单编号
        CombinationRecordDO userRecord = CollectionUtils.findFirst(records, r -> ObjectUtil.equal(r.getUserId(), getLoginUserId()));
        detail.setOrderId(userRecord == null ? null : userRecord.getOrderId()); // 如果没参团，返回 null
        return success(detail);
    }

}

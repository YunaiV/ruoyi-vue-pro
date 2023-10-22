package cn.iocoder.yudao.module.promotion.controller.admin.combination;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordPageItemRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordReqPageVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordSummaryVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 拼团记录")
@RestController
@RequestMapping("/promotion/combination-record")
@Validated
public class CombinationRecordController {

    @Resource
    private CombinationActivityService combinationActivityService;
    @Resource
    @Lazy
    private CombinationRecordService combinationRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得拼团记录分页")
    @PreAuthorize("@ss.hasPermission('promotion:combination-record:query')")
    public CommonResult<PageResult<CombinationRecordPageItemRespVO>> getBargainRecordPage(@Valid CombinationRecordReqPageVO pageVO) {
        PageResult<CombinationRecordDO> recordPage = combinationRecordService.getCombinationRecordPage(pageVO);
        // 拼接数据
        List<CombinationActivityDO> activities = combinationActivityService.getCombinationActivityListByIds(
                convertSet(recordPage.getList(), CombinationRecordDO::getActivityId));
        List<CombinationProductDO> products = combinationActivityService.getCombinationProductListByActivityIds(
                convertSet(recordPage.getList(), CombinationRecordDO::getActivityId));
        return success(CombinationActivityConvert.INSTANCE.convert(recordPage, activities, products));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得拼团记录的概要信息", description = "用于拼团记录页面展示")
    @PreAuthorize("@ss.hasPermission('promotion:combination-record:query')")
    public CommonResult<CombinationRecordSummaryVO> getCombinationRecordSummary() {
        CombinationRecordSummaryVO summaryVO = new CombinationRecordSummaryVO();
        summaryVO.setUserCount(combinationRecordService.getCombinationUserCount()); // 获取拼团用户参与数量
        summaryVO.setSuccessCount(combinationRecordService.getCombinationRecordCount( // 获取成团记录
                CombinationRecordStatusEnum.SUCCESS.getStatus(), null, CombinationRecordDO.HEAD_ID_GROUP));
        summaryVO.setVirtualGroupCount(combinationRecordService.getCombinationRecordCount(// 获取虚拟成团记录
                null, Boolean.TRUE, CombinationRecordDO.HEAD_ID_GROUP));
        return success(summaryVO);
    }

}

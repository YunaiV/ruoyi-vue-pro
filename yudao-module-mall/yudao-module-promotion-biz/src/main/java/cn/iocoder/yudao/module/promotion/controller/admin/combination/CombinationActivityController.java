package cn.iocoder.yudao.module.promotion.controller.admin.combination;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.*;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-activity")
@Validated
public class CombinationActivityController {

    @Resource
    private CombinationActivityService combinationActivityService;

    @PostMapping("/create")
    @Operation(summary = "创建拼团活动")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:create')")
    public CommonResult<Long> createCombinationActivity(@Valid @RequestBody CombinationActivityCreateReqVO createReqVO) {
        return success(combinationActivityService.createCombinationActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新拼团活动")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:update')")
    public CommonResult<Boolean> updateCombinationActivity(@Valid @RequestBody CombinationActivityUpdateReqVO updateReqVO) {
        combinationActivityService.updateCombinationActivity(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除拼团活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:delete')")
    public CommonResult<Boolean> deleteCombinationActivity(@RequestParam("id") Long id) {
        combinationActivityService.deleteCombinationActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得拼团活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:query')")
    public CommonResult<CombinationActivityRespVO> getCombinationActivity(@RequestParam("id") Long id) {
        CombinationActivityDO combinationActivity = combinationActivityService.getCombinationActivity(id);
        return success(CombinationActivityConvert.INSTANCE.convert(combinationActivity));
    }

    @GetMapping("/list")
    @Operation(summary = "获得拼团活动列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:query')")
    public CommonResult<List<CombinationActivityRespVO>> getCombinationActivityList(@RequestParam("ids") Collection<Long> ids) {
        List<CombinationActivityDO> list = combinationActivityService.getCombinationActivityList(ids);
        return success(CombinationActivityConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:query')")
    public CommonResult<PageResult<CombinationActivityRespVO>> getCombinationActivityPage(@Valid CombinationActivityPageReqVO pageVO) {
        PageResult<CombinationActivityDO> pageResult = combinationActivityService.getCombinationActivityPage(pageVO);
        return success(CombinationActivityConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出拼团活动 Excel")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:export')")
    @OperateLog(type = EXPORT)
    public void exportCombinationActivityExcel(@Valid CombinationActivityExportReqVO exportReqVO,
                                               HttpServletResponse response) throws IOException {
        List<CombinationActivityDO> list = combinationActivityService.getCombinationActivityList(exportReqVO);
        // 导出 Excel
        List<CombinationActivityExcelVO> datas = CombinationActivityConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "拼团活动.xls", "数据", CombinationActivityExcelVO.class, datas);
    }

}

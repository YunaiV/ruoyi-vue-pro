package cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssueRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueDO;
import cn.iocoder.yudao.module.mes.service.wm.miscissue.MesWmMiscIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 杂项出库单")
@RestController
@RequestMapping("/mes/wm/misc-issue")
@Validated
public class MesWmMiscIssueController {

    @Resource
    private MesWmMiscIssueService miscIssueService;

    @PostMapping("/create")
    @Operation(summary = "创建杂项出库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:create')")
    public CommonResult<Long> createMiscIssue(@Valid @RequestBody MesWmMiscIssueSaveReqVO createReqVO) {
        return success(miscIssueService.createMiscIssue(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改杂项出库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:update')")
    public CommonResult<Boolean> updateMiscIssue(@Valid @RequestBody MesWmMiscIssueSaveReqVO updateReqVO) {
        miscIssueService.updateMiscIssue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除杂项出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:delete')")
    public CommonResult<Boolean> deleteMiscIssue(@RequestParam("id") Long id) {
        miscIssueService.deleteMiscIssue(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得杂项出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:query')")
    public CommonResult<MesWmMiscIssueRespVO> getMiscIssue(@RequestParam("id") Long id) {
        MesWmMiscIssueDO issue = miscIssueService.getMiscIssue(id);
        if (issue == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(issue)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得杂项出库单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:query')")
    public CommonResult<PageResult<MesWmMiscIssueRespVO>> getMiscIssuePage(
            @Valid MesWmMiscIssuePageReqVO pageReqVO) {
        PageResult<MesWmMiscIssueDO> pageResult = miscIssueService.getMiscIssuePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出杂项出库单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMiscIssueExcel(@Valid MesWmMiscIssuePageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmMiscIssueDO> pageResult = miscIssueService.getMiscIssuePage(pageReqVO);
        ExcelUtils.write(response, "杂项出库单.xls", "数据", MesWmMiscIssueRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交杂项出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:update')")
    public CommonResult<Boolean> submitMiscIssue(@RequestParam("id") Long id) {
        miscIssueService.submitMiscIssue(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "执行出库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:finish')")
    public CommonResult<Boolean> finishMiscIssue(@RequestParam("id") Long id) {
        miscIssueService.finishMiscIssue(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消杂项出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:update')")
    public CommonResult<Boolean> cancelMiscIssue(@RequestParam("id") Long id) {
        miscIssueService.cancelMiscIssue(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmMiscIssueRespVO> buildRespVOList(List<MesWmMiscIssueDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanUtils.toBean(list, MesWmMiscIssueRespVO.class);
    }

}

package cn.iocoder.yudao.module.report.controller.admin.ureport;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UreportFilePageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UreportFileRespVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UreportFileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.module.report.service.ureport.UreportFileService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

// TODO @赤焰: Ureport 改成 UReport
@Tag(name = "管理后台 - UReport2 报表")
@RestController
@RequestMapping("/report/ureport-file")
@Validated
public class UreportFileController {

    @Resource
    private UreportFileService ureportFileService;

    @PostMapping("/create")
    @Operation(summary = "创建 UReport2 报表")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:create')")
    public CommonResult<Long> createUreportFile(@Valid @RequestBody UreportFileSaveReqVO createReqVO) {
        return success(ureportFileService.createUreportFile(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 UReport2 报表")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:update')")
    public CommonResult<Boolean> updateUreportFile(@Valid @RequestBody UreportFileSaveReqVO updateReqVO) {
        ureportFileService.updateUreportFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 UReport2 报表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('report:ureport-file:delete')")
    public CommonResult<Boolean> deleteUreportFile(@RequestParam("id") Long id) {
        ureportFileService.deleteUreportFile(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 UReport2 报表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:query')")
    public CommonResult<UreportFileRespVO> getUreportFile(@RequestParam("id") Long id) {
        UreportFileDO ureportFile = ureportFileService.getUreportFile(id);
        return success(BeanUtils.toBean(ureportFile, UreportFileRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 UReport2 报表分页")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:query')")
    public CommonResult<PageResult<UreportFileRespVO>> getUreportFilePage(@Valid UreportFilePageReqVO pageReqVO) {
        PageResult<UreportFileDO> pageResult = ureportFileService.getUreportFilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UreportFileRespVO.class));
    }

    // TODO @赤焰：导出是必须的么？没用可以删除哈
    @GetMapping("/export-excel")
    @Operation(summary = "导出 UReport2 报表 Excel")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:export')")
    @OperateLog(type = EXPORT)
    public void exportUreportFileExcel(@Valid UreportFilePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UreportFileDO> list = ureportFileService.getUreportFilePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Ureport2报表.xls", "数据", UreportFileRespVO.class,
                        BeanUtils.toBean(list, UreportFileRespVO.class));
    }

}
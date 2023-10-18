package cn.iocoder.yudao.module.crm.controller.admin.clue;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.convert.clue.CrmClueConvert;
import cn.iocoder.yudao.module.crm.service.clue.CrmClueService;

@Tag(name = "管理后台 - 线索")
@RestController
@RequestMapping("/crm/clue")
@Validated
public class CrmClueController {

    @Resource
    private CrmClueService clueService;

    @PostMapping("/create")
    @Operation(summary = "创建线索")
    @PreAuthorize("@ss.hasPermission('crm:clue:create')")
    public CommonResult<Long> createClue(@Valid @RequestBody CrmClueCreateReqVO createReqVO) {
        return success(clueService.createClue(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新线索")
    @PreAuthorize("@ss.hasPermission('crm:clue:update')")
    public CommonResult<Boolean> updateClue(@Valid @RequestBody CrmClueUpdateReqVO updateReqVO) {
        clueService.updateClue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除线索")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:clue:delete')")
    public CommonResult<Boolean> deleteClue(@RequestParam("id") Long id) {
        clueService.deleteClue(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得线索")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:clue:query')")
    public CommonResult<CrmClueRespVO> getClue(@RequestParam("id") Long id) {
        CrmClueDO clue = clueService.getClue(id);
        return success(CrmClueConvert.INSTANCE.convert(clue));
    }

    @GetMapping("/list")
    @Operation(summary = "获得线索列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('crm:clue:query')")
    public CommonResult<List<CrmClueRespVO>> getClueList(@RequestParam("ids") Collection<Long> ids) {
        List<CrmClueDO> list = clueService.getClueList(ids);
        return success(CrmClueConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得线索分页")
    @PreAuthorize("@ss.hasPermission('crm:clue:query')")
    public CommonResult<PageResult<CrmClueRespVO>> getCluePage(@Valid CrmCluePageReqVO pageVO) {
        PageResult<CrmClueDO> pageResult = clueService.getCluePage(pageVO);
        return success(CrmClueConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出线索 Excel")
    @PreAuthorize("@ss.hasPermission('crm:clue:export')")
    @OperateLog(type = EXPORT)
    public void exportClueExcel(@Valid CrmClueExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CrmClueDO> list = clueService.getClueList(exportReqVO);
        // 导出 Excel
        List<CrmClueExcelVO> datas = CrmClueConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "线索.xls", "数据", CrmClueExcelVO.class, datas);
    }

}

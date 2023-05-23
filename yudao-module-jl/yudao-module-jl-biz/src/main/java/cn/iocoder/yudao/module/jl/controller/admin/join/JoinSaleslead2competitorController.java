package cn.iocoder.yudao.module.jl.controller.admin.join;

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

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2competitorConvert;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2competitorService;

@Tag(name = "管理后台 - 销售线索中竞争对手的报价")
@RestController
@RequestMapping("/jl/join-saleslead2competitor")
@Validated
public class JoinSaleslead2competitorController {

    @Resource
    private JoinSaleslead2competitorService joinSaleslead2competitorService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中竞争对手的报价")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:create')")
    public CommonResult<Long> createJoinSaleslead2competitor(@Valid @RequestBody JoinSaleslead2competitorCreateReqVO createReqVO) {
        return success(joinSaleslead2competitorService.createJoinSaleslead2competitor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中竞争对手的报价")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:update')")
    public CommonResult<Boolean> updateJoinSaleslead2competitor(@Valid @RequestBody JoinSaleslead2competitorUpdateReqVO updateReqVO) {
        joinSaleslead2competitorService.updateJoinSaleslead2competitor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索中竞争对手的报价")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:delete')")
    public CommonResult<Boolean> deleteJoinSaleslead2competitor(@RequestParam("id") Long id) {
        joinSaleslead2competitorService.deleteJoinSaleslead2competitor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索中竞争对手的报价")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:query')")
    public CommonResult<JoinSaleslead2competitorRespVO> getJoinSaleslead2competitor(@RequestParam("id") Long id) {
        JoinSaleslead2competitorDO joinSaleslead2competitor = joinSaleslead2competitorService.getJoinSaleslead2competitor(id);
        return success(JoinSaleslead2competitorConvert.INSTANCE.convert(joinSaleslead2competitor));
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索中竞争对手的报价列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:query')")
    public CommonResult<List<JoinSaleslead2competitorRespVO>> getJoinSaleslead2competitorList(@RequestParam("ids") Collection<Long> ids) {
        List<JoinSaleslead2competitorDO> list = joinSaleslead2competitorService.getJoinSaleslead2competitorList(ids);
        return success(JoinSaleslead2competitorConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索中竞争对手的报价分页")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:query')")
    public CommonResult<PageResult<JoinSaleslead2competitorRespVO>> getJoinSaleslead2competitorPage(@Valid JoinSaleslead2competitorPageReqVO pageVO) {
        PageResult<JoinSaleslead2competitorDO> pageResult = joinSaleslead2competitorService.getJoinSaleslead2competitorPage(pageVO);
        return success(JoinSaleslead2competitorConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中竞争对手的报价 Excel")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2competitor:export')")
    @OperateLog(type = EXPORT)
    public void exportJoinSaleslead2competitorExcel(@Valid JoinSaleslead2competitorExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<JoinSaleslead2competitorDO> list = joinSaleslead2competitorService.getJoinSaleslead2competitorList(exportReqVO);
        // 导出 Excel
        List<JoinSaleslead2competitorExcelVO> datas = JoinSaleslead2competitorConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索中竞争对手的报价.xls", "数据", JoinSaleslead2competitorExcelVO.class, datas);
    }

}

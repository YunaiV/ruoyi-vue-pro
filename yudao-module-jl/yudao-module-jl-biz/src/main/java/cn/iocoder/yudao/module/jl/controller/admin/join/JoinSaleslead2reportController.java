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
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2reportConvert;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2reportService;

@Tag(name = "管理后台 - 销售线索中的报告")
@RestController
@RequestMapping("/jl/join-saleslead2report")
@Validated
public class JoinSaleslead2reportController {

    @Resource
    private JoinSaleslead2reportService joinSaleslead2reportService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中的方案")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:create')")
    public CommonResult<Long> createJoinSaleslead2report(@Valid @RequestBody JoinSaleslead2reportCreateReqVO createReqVO) {
        return success(joinSaleslead2reportService.createJoinSaleslead2report(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中的方案")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:update')")
    public CommonResult<Boolean> updateJoinSaleslead2report(@Valid @RequestBody JoinSaleslead2reportUpdateReqVO updateReqVO) {
        joinSaleslead2reportService.updateJoinSaleslead2report(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索中的方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:delete')")
    public CommonResult<Boolean> deleteJoinSaleslead2report(@RequestParam("id") Long id) {
        joinSaleslead2reportService.deleteJoinSaleslead2report(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索中的方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:query')")
    public CommonResult<JoinSaleslead2reportRespVO> getJoinSaleslead2report(@RequestParam("id") Long id) {
        JoinSaleslead2reportDO joinSaleslead2report = joinSaleslead2reportService.getJoinSaleslead2report(id);
        return success(JoinSaleslead2reportConvert.INSTANCE.convert(joinSaleslead2report));
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索中的方案列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:query')")
    public CommonResult<List<JoinSaleslead2reportRespVO>> getJoinSaleslead2reportList(@RequestParam("ids") Collection<Long> ids) {
        List<JoinSaleslead2reportDO> list = joinSaleslead2reportService.getJoinSaleslead2reportList(ids);
        return success(JoinSaleslead2reportConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索中的方案分页")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:query')")
    public CommonResult<PageResult<JoinSaleslead2reportRespVO>> getJoinSaleslead2reportPage(@Valid JoinSaleslead2reportPageReqVO pageVO) {
        PageResult<JoinSaleslead2reportDO> pageResult = joinSaleslead2reportService.getJoinSaleslead2reportPage(pageVO);
        return success(JoinSaleslead2reportConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中的方案 Excel")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2report:export')")
    @OperateLog(type = EXPORT)
    public void exportJoinSaleslead2reportExcel(@Valid JoinSaleslead2reportExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<JoinSaleslead2reportDO> list = joinSaleslead2reportService.getJoinSaleslead2reportList(exportReqVO);
        // 导出 Excel
        List<JoinSaleslead2reportExcelVO> datas = JoinSaleslead2reportConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索中的方案.xls", "数据", JoinSaleslead2reportExcelVO.class, datas);
    }

}

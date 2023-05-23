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
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2customerplanConvert;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2customerplanService;

@Tag(name = "管理后台 - 销售线索中的客户方案")
@RestController
@RequestMapping("/jl/join-saleslead2customerplan")
@Validated
public class JoinSaleslead2customerplanController {

    @Resource
    private JoinSaleslead2customerplanService joinSaleslead2customerplanService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中的客户方案")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:create')")
    public CommonResult<Long> createJoinSaleslead2customerplan(@Valid @RequestBody JoinSaleslead2customerplanCreateReqVO createReqVO) {
        return success(joinSaleslead2customerplanService.createJoinSaleslead2customerplan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中的客户方案")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:update')")
    public CommonResult<Boolean> updateJoinSaleslead2customerplan(@Valid @RequestBody JoinSaleslead2customerplanUpdateReqVO updateReqVO) {
        joinSaleslead2customerplanService.updateJoinSaleslead2customerplan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索中的客户方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:delete')")
    public CommonResult<Boolean> deleteJoinSaleslead2customerplan(@RequestParam("id") Long id) {
        joinSaleslead2customerplanService.deleteJoinSaleslead2customerplan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索中的客户方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:query')")
    public CommonResult<JoinSaleslead2customerplanRespVO> getJoinSaleslead2customerplan(@RequestParam("id") Long id) {
        JoinSaleslead2customerplanDO joinSaleslead2customerplan = joinSaleslead2customerplanService.getJoinSaleslead2customerplan(id);
        return success(JoinSaleslead2customerplanConvert.INSTANCE.convert(joinSaleslead2customerplan));
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索中的客户方案列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:query')")
    public CommonResult<List<JoinSaleslead2customerplanRespVO>> getJoinSaleslead2customerplanList(@RequestParam("ids") Collection<Long> ids) {
        List<JoinSaleslead2customerplanDO> list = joinSaleslead2customerplanService.getJoinSaleslead2customerplanList(ids);
        return success(JoinSaleslead2customerplanConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索中的客户方案分页")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:query')")
    public CommonResult<PageResult<JoinSaleslead2customerplanRespVO>> getJoinSaleslead2customerplanPage(@Valid JoinSaleslead2customerplanPageReqVO pageVO) {
        PageResult<JoinSaleslead2customerplanDO> pageResult = joinSaleslead2customerplanService.getJoinSaleslead2customerplanPage(pageVO);
        return success(JoinSaleslead2customerplanConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中的客户方案 Excel")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2customerplan:export')")
    @OperateLog(type = EXPORT)
    public void exportJoinSaleslead2customerplanExcel(@Valid JoinSaleslead2customerplanExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<JoinSaleslead2customerplanDO> list = joinSaleslead2customerplanService.getJoinSaleslead2customerplanList(exportReqVO);
        // 导出 Excel
        List<JoinSaleslead2customerplanExcelVO> datas = JoinSaleslead2customerplanConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索中的客户方案.xls", "数据", JoinSaleslead2customerplanExcelVO.class, datas);
    }

}

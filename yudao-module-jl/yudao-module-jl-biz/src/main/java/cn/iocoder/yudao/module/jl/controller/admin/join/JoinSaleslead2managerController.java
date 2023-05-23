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
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2managerConvert;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2managerService;

@Tag(name = "管理后台 - 销售线索中的项目售前支持人员")
@RestController
@RequestMapping("/jl/join-saleslead2manager")
@Validated
public class JoinSaleslead2managerController {

    @Resource
    private JoinSaleslead2managerService joinSaleslead2managerService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中的项目售前支持人员")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:create')")
    public CommonResult<Long> createJoinSaleslead2manager(@Valid @RequestBody JoinSaleslead2managerCreateReqVO createReqVO) {
        return success(joinSaleslead2managerService.createJoinSaleslead2manager(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中的项目售前支持人员")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:update')")
    public CommonResult<Boolean> updateJoinSaleslead2manager(@Valid @RequestBody JoinSaleslead2managerUpdateReqVO updateReqVO) {
        joinSaleslead2managerService.updateJoinSaleslead2manager(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索中的项目售前支持人员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:delete')")
    public CommonResult<Boolean> deleteJoinSaleslead2manager(@RequestParam("id") Long id) {
        joinSaleslead2managerService.deleteJoinSaleslead2manager(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索中的项目售前支持人员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:query')")
    public CommonResult<JoinSaleslead2managerRespVO> getJoinSaleslead2manager(@RequestParam("id") Long id) {
        JoinSaleslead2managerDO joinSaleslead2manager = joinSaleslead2managerService.getJoinSaleslead2manager(id);
        return success(JoinSaleslead2managerConvert.INSTANCE.convert(joinSaleslead2manager));
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索中的项目售前支持人员列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:query')")
    public CommonResult<List<JoinSaleslead2managerRespVO>> getJoinSaleslead2managerList(@RequestParam("ids") Collection<Long> ids) {
        List<JoinSaleslead2managerDO> list = joinSaleslead2managerService.getJoinSaleslead2managerList(ids);
        return success(JoinSaleslead2managerConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索中的项目售前支持人员分页")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:query')")
    public CommonResult<PageResult<JoinSaleslead2managerRespVO>> getJoinSaleslead2managerPage(@Valid JoinSaleslead2managerPageReqVO pageVO) {
        PageResult<JoinSaleslead2managerDO> pageResult = joinSaleslead2managerService.getJoinSaleslead2managerPage(pageVO);
        return success(JoinSaleslead2managerConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中的项目售前支持人员 Excel")
    @PreAuthorize("@ss.hasPermission('jl:join-saleslead2manager:export')")
    @OperateLog(type = EXPORT)
    public void exportJoinSaleslead2managerExcel(@Valid JoinSaleslead2managerExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<JoinSaleslead2managerDO> list = joinSaleslead2managerService.getJoinSaleslead2managerList(exportReqVO);
        // 导出 Excel
        List<JoinSaleslead2managerExcelVO> datas = JoinSaleslead2managerConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索中的项目售前支持人员.xls", "数据", JoinSaleslead2managerExcelVO.class, datas);
    }

}

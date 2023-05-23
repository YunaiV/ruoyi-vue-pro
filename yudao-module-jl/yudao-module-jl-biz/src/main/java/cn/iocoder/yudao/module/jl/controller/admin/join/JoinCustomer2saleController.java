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
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinCustomer2saleDO;
import cn.iocoder.yudao.module.jl.convert.join.JoinCustomer2saleConvert;
import cn.iocoder.yudao.module.jl.service.join.JoinCustomer2saleService;

@Tag(name = "管理后台 - 客户所属的销售人员")
@RestController
@RequestMapping("/jl/join-customer2sale")
@Validated
public class JoinCustomer2saleController {

    @Resource
    private JoinCustomer2saleService joinCustomer2saleService;

    @PostMapping("/create")
    @Operation(summary = "创建客户所属的销售人员")
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:create')")
    public CommonResult<Long> createJoinCustomer2sale(@Valid @RequestBody JoinCustomer2saleCreateReqVO createReqVO) {
        return success(joinCustomer2saleService.createJoinCustomer2sale(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户所属的销售人员")
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:update')")
    public CommonResult<Boolean> updateJoinCustomer2sale(@Valid @RequestBody JoinCustomer2saleUpdateReqVO updateReqVO) {
        joinCustomer2saleService.updateJoinCustomer2sale(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户所属的销售人员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:delete')")
    public CommonResult<Boolean> deleteJoinCustomer2sale(@RequestParam("id") Long id) {
        joinCustomer2saleService.deleteJoinCustomer2sale(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户所属的销售人员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:query')")
    public CommonResult<JoinCustomer2saleRespVO> getJoinCustomer2sale(@RequestParam("id") Long id) {
        JoinCustomer2saleDO joinCustomer2sale = joinCustomer2saleService.getJoinCustomer2sale(id);
        return success(JoinCustomer2saleConvert.INSTANCE.convert(joinCustomer2sale));
    }

    @GetMapping("/list")
    @Operation(summary = "获得客户所属的销售人员列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:query')")
    public CommonResult<List<JoinCustomer2saleRespVO>> getJoinCustomer2saleList(@RequestParam("ids") Collection<Long> ids) {
        List<JoinCustomer2saleDO> list = joinCustomer2saleService.getJoinCustomer2saleList(ids);
        return success(JoinCustomer2saleConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户所属的销售人员分页")
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:query')")
    public CommonResult<PageResult<JoinCustomer2saleRespVO>> getJoinCustomer2salePage(@Valid JoinCustomer2salePageReqVO pageVO) {
        PageResult<JoinCustomer2saleDO> pageResult = joinCustomer2saleService.getJoinCustomer2salePage(pageVO);
        return success(JoinCustomer2saleConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户所属的销售人员 Excel")
    @PreAuthorize("@ss.hasPermission('jl:join-customer2sale:export')")
    @OperateLog(type = EXPORT)
    public void exportJoinCustomer2saleExcel(@Valid JoinCustomer2saleExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<JoinCustomer2saleDO> list = joinCustomer2saleService.getJoinCustomer2saleList(exportReqVO);
        // 导出 Excel
        List<JoinCustomer2saleExcelVO> datas = JoinCustomer2saleConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "客户所属的销售人员.xls", "数据", JoinCustomer2saleExcelVO.class, datas);
    }

}

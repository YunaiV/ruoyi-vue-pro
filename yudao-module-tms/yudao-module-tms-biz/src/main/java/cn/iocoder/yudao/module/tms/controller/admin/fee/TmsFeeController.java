/*
package cn.iocoder.yudao.module.tms.controller.admin.fee;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.fee.TmsFeeDO;
import cn.iocoder.yudao.module.tms.service.fee.TmsFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 出运订单费用明细")
@RequestMapping("/tms/fee")
@Validated
public class TmsFeeController {

    @Resource
    private TmsFeeService feeService;

//    @PostMapping("/create")
//    @Operation(summary = "创建出运订单费用明细")
//    @Idempotent
//    @PreAuthorize("@ss.hasPermission('tms:fee:create')")
//    public CommonResult<Long> createFee(@Valid @RequestBody TmsFeeSaveReqVO createReqVO) {
//        return success(feeService.createFee(createReqVO));
//    }
//
//    @PutMapping("/update")
//    @Operation(summary = "更新出运订单费用明细")
//    @PreAuthorize("@ss.hasPermission('tms:fee:update')")
//    public CommonResult<Boolean> updateFee(@Valid @RequestBody TmsFeeSaveReqVO updateReqVO) {
//        feeService.updateFee(updateReqVO);
//        return success(true);
//    }

//    @DeleteMapping("/delete")
//    @Operation(summary = "删除出运订单费用明细")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('tms:fee:delete')")
//    public CommonResult<Boolean> deleteFee(@RequestParam("id") Long id) {
//        feeService.deleteFee(id);
//        return success(true);
//    }
//
//    @GetMapping("/get")
//    @Operation(summary = "获得出运订单费用明细")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('tms:fee:query')")
//    public CommonResult<TmsFeeRespVO> getFee(@RequestParam("id") Long id) {
//        TmsFeeDO fee = feeService.getFee(id);
//        return success(BeanUtils.toBean(fee, TmsFeeRespVO.class));
//    }

    @GetMapping("/page")
    @Operation(summary = "获得出运订单费用明细分页")
    @PreAuthorize("@ss.hasPermission('tms:fee:query')")
    public CommonResult<PageResult<TmsFeeRespVO>> getFeePage(@Valid TmsFeePageReqVO pageReqVO) {
        PageResult<TmsFeeDO> pageResult = feeService.getFeePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TmsFeeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出运订单费用明细 Excel")
    @PreAuthorize("@ss.hasPermission('tms:fee:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFeeExcel(@Valid TmsFeePageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TmsFeeDO> list = feeService.getFeePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出运订单费用明细.xls", "数据", TmsFeeRespVO.class,
            BeanUtils.toBean(list, TmsFeeRespVO.class));
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入出运订单费用明细 Excel")
    @PreAuthorize("@ss.hasPermission('tms:fee:import')")
    public CommonResult
        <Boolean> importFeeExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TmsFeeSaveReqVO> list = ExcelUtils.read(file, TmsFeeSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        return success(true);
    }


}*/

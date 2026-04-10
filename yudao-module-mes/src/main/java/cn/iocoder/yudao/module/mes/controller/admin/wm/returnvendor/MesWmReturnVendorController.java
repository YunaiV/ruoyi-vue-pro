package cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorDO;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.wm.returnvendor.MesWmReturnVendorService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 供应商退货单")
@RestController
@RequestMapping("/mes/wm/return-vendor")
@Validated
public class MesWmReturnVendorController {

    @Resource
    private MesWmReturnVendorService returnVendorService;
    @Resource
    private MesMdVendorService vendorService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商退货单")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:create')")
    public CommonResult<Long> createReturnVendor(@Valid @RequestBody MesWmReturnVendorSaveReqVO createReqVO) {
        return success(returnVendorService.createReturnVendor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改供应商退货单")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:update')")
    public CommonResult<Boolean> updateReturnVendor(@Valid @RequestBody MesWmReturnVendorSaveReqVO updateReqVO) {
        returnVendorService.updateReturnVendor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:delete')")
    public CommonResult<Boolean> deleteReturnVendor(@RequestParam("id") Long id) {
        returnVendorService.deleteReturnVendor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商退货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:query')")
    public CommonResult<MesWmReturnVendorRespVO> getReturnVendor(@RequestParam("id") Long id) {
        MesWmReturnVendorDO returnVendor = returnVendorService.getReturnVendor(id);
        if (returnVendor == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(returnVendor)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商退货单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:query')")
    public CommonResult<PageResult<MesWmReturnVendorRespVO>> getReturnVendorPage(
            @Valid MesWmReturnVendorPageReqVO pageReqVO) {
        PageResult<MesWmReturnVendorDO> pageResult = returnVendorService.getReturnVendorPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出供应商退货单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReturnVendorExcel(@Valid MesWmReturnVendorPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmReturnVendorDO> pageResult = returnVendorService.getReturnVendorPage(pageReqVO);
        ExcelUtils.write(response, "供应商退货单.xls", "数据", MesWmReturnVendorRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交供应商退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:update')")
    public CommonResult<Boolean> submitReturnVendor(@RequestParam("id") Long id) {
        returnVendorService.submitReturnVendor(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "执行拣货")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:update')")
    public CommonResult<Boolean> stockReturnVendor(@RequestParam("id") Long id) {
        returnVendorService.stockReturnVendor(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成供应商退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:finish')")
    public CommonResult<Boolean> finishReturnVendor(@RequestParam("id") Long id) {
        returnVendorService.finishReturnVendor(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消供应商退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:update')")
    public CommonResult<Boolean> cancelReturnVendor(@RequestParam("id") Long id) {
        returnVendorService.cancelReturnVendor(id);
        return success(true);
    }

    @GetMapping("/check-quantity")
    @Operation(summary = "校验供应商退货单数量", description = "校验每行明细数量之和是否等于行退货数量")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:query')")
    public CommonResult<Boolean> checkReturnVendorQuantity(@RequestParam("id") Long id) {
        return success(returnVendorService.checkReturnVendorQuantity(id));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmReturnVendorRespVO> buildRespVOList(List<MesWmReturnVendorDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesWmReturnVendorDO::getVendorId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmReturnVendorRespVO.class, vo ->
                MapUtils.findAndThen(vendorMap, vo.getVendorId(), vendor ->
                        vo.setVendorCode(vendor.getCode()).setVendorName(vendor.getName()).setVendorNickname(vendor.getNickname())));
    }

}

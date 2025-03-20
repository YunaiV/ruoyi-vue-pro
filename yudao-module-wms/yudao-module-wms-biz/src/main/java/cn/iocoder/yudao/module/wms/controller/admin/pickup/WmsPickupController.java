package cn.iocoder.yudao.module.wms.controller.admin.pickup;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.service.pickup.WmsPickupService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "拣货单")
@RestController
@RequestMapping("/wms/pickup")
@Validated
public class WmsPickupController {

    @Resource
    private WmsPickupService pickupService;

    /**
     * @sign : 50A2CF839F346ECB
     */
    @PostMapping("/create")
    @Operation(summary = "创建拣货单")
    @PreAuthorize("@ss.hasPermission('wms:pickup:create')")
    public CommonResult<Long> createPickup(@Valid @RequestBody WmsPickupSaveReqVO createReqVO) {
        return success(pickupService.createPickup(createReqVO).getId());
    }

    /**
     * @sign : E1B13418492DDEBD
     */
    @PutMapping("/update")
    @Operation(summary = "更新拣货单")
    @PreAuthorize("@ss.hasPermission('wms:pickup:update')")
    public CommonResult<Boolean> updatePickup(@Valid @RequestBody WmsPickupSaveReqVO updateReqVO) {
        pickupService.updatePickup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除拣货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:pickup:delete')")
    public CommonResult<Boolean> deletePickup(@RequestParam("id") Long id) {
        pickupService.deletePickup(id);
        return success(true);
    }

    /**
     * @sign : 213DD832CA7993D8
     */
    @GetMapping("/get")
    @Operation(summary = "获得拣货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:pickup:query')")
    public CommonResult<WmsPickupRespVO> getPickup(@RequestParam("id") Long id) {
        // 查询数据
        WmsPickupDO pickup = pickupService.getPickup(id);
        if (pickup == null) {
            throw exception(PICKUP_NOT_EXISTS);
        }
        // 转换
        WmsPickupRespVO pickupVO = BeanUtils.toBean(pickup, WmsPickupRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(pickupVO))
			.mapping(WmsPickupRespVO::getCreator, WmsPickupRespVO::setCreatorName)
			.mapping(WmsPickupRespVO::getCreator, WmsPickupRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(pickupVO);
    }

    /**
     * @sign : 5D5029FCDD560031
     */
    @GetMapping("/page")
    @Operation(summary = "获得拣货单分页")
    @PreAuthorize("@ss.hasPermission('wms:pickup:query')")
    public CommonResult<PageResult<WmsPickupRespVO>> getPickupPage(@Valid WmsPickupPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsPickupDO> doPageResult = pickupService.getPickupPage(pageReqVO);
        // 转换
        PageResult<WmsPickupRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsPickupRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsPickupRespVO::getCreator, WmsPickupRespVO::setCreatorName)
			.mapping(WmsPickupRespVO::getCreator, WmsPickupRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出拣货单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:pickup:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPickupExcel(@Valid WmsPickupPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsPickupDO> list = pickupService.getPickupPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "拣货单.xls", "数据", WmsPickupRespVO.class, BeanUtils.toBean(list, WmsPickupRespVO.class));
    }
}
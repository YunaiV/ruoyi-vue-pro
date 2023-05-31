package cn.iocoder.yudao.module.trade.controller.admin.delivery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.*;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryPickUpStoreConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
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
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 自提门店")
@RestController
@RequestMapping("/trade/delivery/pick-up-store")
@Validated
public class DeliveryPickUpStoreController {

    @Resource
    private DeliveryPickUpStoreService deliveryPickUpStoreService;

    @PostMapping("/create")
    @Operation(summary = "创建自提门店")
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:create')")
    public CommonResult<Long> createDeliveryPickUpStore(@Valid @RequestBody DeliveryPickUpStoreCreateReqVO createReqVO) {
        return success(deliveryPickUpStoreService.createDeliveryPickUpStore(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新自提门店")
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:update')")
    public CommonResult<Boolean> updateDeliveryPickUpStore(@Valid @RequestBody DeliveryPickUpStoreUpdateReqVO updateReqVO) {
        deliveryPickUpStoreService.updateDeliveryPickUpStore(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除自提门店")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:delete')")
    public CommonResult<Boolean> deleteDeliveryPickUpStore(@RequestParam("id") Long id) {
        deliveryPickUpStoreService.deleteDeliveryPickUpStore(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得自提门店")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:query')")
    public CommonResult<DeliveryPickUpStoreRespVO> getDeliveryPickUpStore(@RequestParam("id") Long id) {
        DeliveryPickUpStoreDO deliveryPickUpStore = deliveryPickUpStoreService.getDeliveryPickUpStore(id);
        return success(DeliveryPickUpStoreConvert.INSTANCE.convert(deliveryPickUpStore));
    }

    @GetMapping("/list")
    @Operation(summary = "获得自提门店列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:query')")
    public CommonResult<List<DeliveryPickUpStoreRespVO>> getDeliveryPickUpStoreList(@RequestParam("ids") Collection<Long> ids) {
        List<DeliveryPickUpStoreDO> list = deliveryPickUpStoreService.getDeliveryPickUpStoreList(ids);
        return success(DeliveryPickUpStoreConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得自提门店分页")
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:query')")
    public CommonResult<PageResult<DeliveryPickUpStoreRespVO>> getDeliveryPickUpStorePage(@Valid DeliveryPickUpStorePageReqVO pageVO) {
        PageResult<DeliveryPickUpStoreDO> pageResult = deliveryPickUpStoreService.getDeliveryPickUpStorePage(pageVO);
        return success(DeliveryPickUpStoreConvert.INSTANCE.convertPage(pageResult));
    }

    // TODO @jason：导出去掉好列；简化下，一般用不到哈。
    @GetMapping("/export-excel")
    @Operation(summary = "导出自提门店 Excel")
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:export')")
    @OperateLog(type = EXPORT)
    public void exportDeliveryPickUpStoreExcel(@Valid DeliveryPickUpStoreExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<DeliveryPickUpStoreDO> list = deliveryPickUpStoreService.getDeliveryPickUpStoreList(exportReqVO);
        // 导出 Excel
        List<DeliveryPickUpStoreExcelVO> datas = DeliveryPickUpStoreConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "自提门店.xls", "数据", DeliveryPickUpStoreExcelVO.class, datas);
    }
}

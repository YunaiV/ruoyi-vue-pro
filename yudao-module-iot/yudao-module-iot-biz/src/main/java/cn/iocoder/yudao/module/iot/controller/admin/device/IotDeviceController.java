package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - IoT 设备")
@RestController
@RequestMapping("/iot/device")
@Validated
public class IotDeviceController {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('iot:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody IotDeviceSaveReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }


    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody IotDeviceSaveReqVO updateReqVO) {
        deviceService.updateDevice(updateReqVO);
        return success(true);
    }

    @PutMapping("/bind-gateway")
    @Operation(summary = "绑定子设备到网关")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> bindDeviceGateway(@Valid @RequestBody IotDeviceBindGatewayReqVO reqVO) {
        deviceService.bindDeviceGateway(reqVO.getSubIds(), reqVO.getGatewayId());
        return success(true);
    }

    @PutMapping("/unbind-gateway")
    @Operation(summary = "解绑子设备与网关")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> unbindDeviceGateway(@Valid @RequestBody IotDeviceUnbindGatewayReqVO reqVO) {
        deviceService.unbindDeviceGateway(reqVO.getSubIds(), reqVO.getGatewayId());
        return success(true);
    }

    @GetMapping("/sub-device-list")
    @Operation(summary = "获取网关的子设备列表")
    @Parameter(name = "gatewayId", description = "网关设备编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotDeviceRespVO>> getSubDeviceList(@RequestParam("gatewayId") Long gatewayId) {
        List<IotDeviceDO> list = deviceService.getDeviceListByGatewayId(gatewayId);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }

        // 补充产品名称
        Map<Long, IotProductDO> productMap = convertMap(productService.getProductList(), IotProductDO::getId);
        return success(convertList(list, device -> {
            IotDeviceRespVO respVO = BeanUtils.toBean(device, IotDeviceRespVO.class);
            MapUtils.findAndThen(productMap, device.getProductId(),
                    product -> respVO.setProductName(product.getName()));
            return respVO;
        }));
    }

    @GetMapping("/unbound-sub-device-page")
    @Operation(summary = "获取未绑定网关的子设备分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceRespVO>> getUnboundSubDevicePage(@Valid IotDevicePageReqVO pageReqVO) {
        PageResult<IotDeviceDO> pageResult = deviceService.getUnboundSubDevicePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 补充产品名称
        Map<Long, IotProductDO> productMap = convertMap(productService.getProductList(), IotProductDO::getId);
        PageResult<IotDeviceRespVO> result = BeanUtils.toBean(pageResult, IotDeviceRespVO.class, device ->
                MapUtils.findAndThen(productMap, device.getProductId(), product -> device.setProductName(product.getName())));
        return success(result);
    }

    @PutMapping("/update-group")
    @Operation(summary = "更新设备分组")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceGroup(@Valid @RequestBody IotDeviceUpdateGroupReqVO updateReqVO) {
        deviceService.updateDeviceGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除单个设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "删除多个设备")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDeviceList(@RequestParam("ids") Collection<Long> ids) {
        deviceService.deleteDeviceList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceRespVO> getDevice(@RequestParam("id") Long id) {
        IotDeviceDO device = deviceService.getDevice(id);
        return success(BeanUtils.toBean(device, IotDeviceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceRespVO>> getDevicePage(@Valid IotDevicePageReqVO pageReqVO) {
        PageResult<IotDeviceDO> pageResult = deviceService.getDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备 Excel")
    @PreAuthorize("@ss.hasPermission('iot:device:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceExcel(@Valid IotDevicePageReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        CommonResult<PageResult<IotDeviceRespVO>> result = getDevicePage(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "设备.xls", "数据", IotDeviceRespVO.class,
                result.getData().getList());
    }

    @GetMapping("/count")
    @Operation(summary = "获得设备数量")
    @Parameter(name = "productId", description = "产品编号", example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<Long> getDeviceCount(@RequestParam("productId") Long productId) {
        return success(deviceService.getDeviceCountByProductId(productId));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取设备的精简信息列表", description = "主要用于前端的下拉选项")
    @Parameters({
            @Parameter(name = "deviceType", description = "设备类型", example = "1"),
            @Parameter(name = "productId", description = "产品编号", example = "1024")
    })
    public CommonResult<List<IotDeviceRespVO>> getDeviceSimpleList(
            @RequestParam(value = "deviceType", required = false) Integer deviceType,
            @RequestParam(value = "productId", required = false) Long productId) {
        List<IotDeviceDO> list = deviceService.getDeviceListByCondition(deviceType, productId);
        return success(convertList(list, device ->  // 只返回 id、name、productId 字段
                new IotDeviceRespVO().setId(device.getId()).setDeviceName(device.getDeviceName())
                        .setProductId(device.getProductId()).setState(device.getState())));
    }

    @GetMapping("/location-list")
    @Operation(summary = "获取设备位置列表", description = "获取有经纬度信息的设备列表，用于地图展示")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotDeviceRespVO>> getDeviceLocationList() {
        // 1. 获取有位置信息的设备列表
        List<IotDeviceDO> devices = deviceService.getDeviceListByHasLocation();
        if (CollUtil.isEmpty(devices)) {
            return success(Collections.emptyList());
        }

        // 2. 转换并返回
        Map<Long, IotProductDO> productMap = convertMap(productService.getProductList(), IotProductDO::getId);
        return success(convertList(devices, device -> {
            IotDeviceRespVO respVO = BeanUtils.toBean(device, IotDeviceRespVO.class);
            MapUtils.findAndThen(productMap, device.getProductId(),
                    product -> respVO.setProductName(product.getName()));
            return respVO;
        }));
    }

    @PostMapping("/import")
    @Operation(summary = "导入设备")
    @PreAuthorize("@ss.hasPermission('iot:device:import')")
    public CommonResult<IotDeviceImportRespVO> importDevice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport)
            throws Exception {
        List<IotDeviceImportExcelVO> list = ExcelUtils.read(file, IotDeviceImportExcelVO.class);
        return success(deviceService.importDevice(list, updateSupport));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入设备模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<IotDeviceImportExcelVO> list = Arrays.asList(
                IotDeviceImportExcelVO.builder().deviceName("温度传感器001").parentDeviceName("gateway110")
                        .productKey("1de24640dfe").groupNames("灰度分组,生产分组").build(),
                IotDeviceImportExcelVO.builder().deviceName("biubiu").productKey("YzvHxd4r67sT4s2B")
                        .groupNames("").build());
        // 输出
        ExcelUtils.write(response, "设备导入模板.xls", "数据", IotDeviceImportExcelVO.class, list);
    }

    @GetMapping("/get-auth-info")
    @Operation(summary = "获得设备连接信息")
    @PreAuthorize("@ss.hasPermission('iot:device:auth-info')")
    public CommonResult<IotDeviceAuthInfoRespVO> getDeviceAuthInfo(@RequestParam("id") Long id) {
        return success(deviceService.getDeviceAuthInfo(id));
    }

    // TODO @haohao：可以使用 @RequestParam("productKey") String productKey, @RequestParam("deviceNames") List<String> deviceNames 来接收哇？
    @GetMapping("/list-by-product-key-and-names")
    @Operation(summary = "通过产品标识和设备名称列表获取设备")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotDeviceRespVO>> getDevicesByProductKeyAndNames(@Valid IotDeviceByProductKeyAndNamesReqVO reqVO) {
        List<IotDeviceDO> devices = deviceService.getDeviceListByProductKeyAndNames(reqVO.getProductKey(), reqVO.getDeviceNames());
        return success(BeanUtils.toBean(devices, IotDeviceRespVO.class));
    }

}

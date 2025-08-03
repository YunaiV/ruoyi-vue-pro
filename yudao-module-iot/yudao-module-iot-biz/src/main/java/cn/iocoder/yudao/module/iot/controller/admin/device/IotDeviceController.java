package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceDownstreamReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceUpstreamReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceDownstreamService;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceUpstreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 设备")
@RestController
@RequestMapping("/iot/device")
@Validated
public class IotDeviceController {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;
    @Resource
    private IotDeviceDownstreamService deviceDownstreamService;

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

    // TODO @芋艿：参考阿里云：1）绑定网关；2）解绑网关

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
    @Parameter(name = "deviceType", description = "设备类型", example = "1")
    public CommonResult<List<IotDeviceRespVO>> getSimpleDeviceList(
            @RequestParam(value = "deviceType", required = false) Integer deviceType) {
        List<IotDeviceDO> list = deviceService.getDeviceListByDeviceType(deviceType);
        return success(convertList(list, device ->  // 只返回 id、name 字段
                new IotDeviceRespVO().setId(device.getId()).setDeviceName(device.getDeviceName())));
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
                IotDeviceImportExcelVO.builder().deviceName("biubiu")
                        .productKey("YzvHxd4r67sT4s2B").groupNames("").build());
        // 输出
        ExcelUtils.write(response, "设备导入模板.xls", "数据", IotDeviceImportExcelVO.class, list);
    }

    @PostMapping("/upstream")
    @Operation(summary = "设备上行", description = "可用于设备模拟")
    @PreAuthorize("@ss.hasPermission('iot:device:upstream')")
    public CommonResult<Boolean> upstreamDevice(@Valid @RequestBody IotDeviceUpstreamReqVO upstreamReqVO) {
        deviceUpstreamService.upstreamDevice(upstreamReqVO);
        return success(true);
    }

    @PostMapping("/downstream")
    @Operation(summary = "设备下行", description = "可用于设备模拟")
    @PreAuthorize("@ss.hasPermission('iot:device:downstream')")
    public CommonResult<Boolean> downstreamDevice(@Valid @RequestBody IotDeviceDownstreamReqVO downstreamReqVO) {
        deviceDownstreamService.downstreamDevice(downstreamReqVO);
        return success(true);
    }

    // TODO @haohao：是不是默认详情接口，不返回 secret，然后这个接口，用于统一返回。然后接口名可以更通用一点。
    @GetMapping("/mqtt-connection-params")
    @Operation(summary = "获取 MQTT 连接参数")
    @PreAuthorize("@ss.hasPermission('iot:device:mqtt-connection-params')")
    public CommonResult<IotDeviceMqttConnectionParamsRespVO> getMqttConnectionParams(@RequestParam("deviceId") Long deviceId) {
        return success(deviceService.getMqttConnectionParams(deviceId));
    }

}
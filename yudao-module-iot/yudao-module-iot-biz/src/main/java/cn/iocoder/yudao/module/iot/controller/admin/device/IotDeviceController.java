package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备")
@RestController
@RequestMapping("/iot/device")
@Validated
public class IotDeviceController {

    @Resource
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('iot:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody IotDeviceSaveReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新设备状态")
    @Parameter(name = "id", description = "编号", required = true)
    @Parameter(name = "status", description = "状态", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceStatus(@RequestParam("id") Long id,
                                                    @RequestParam("status") Integer status) {
        deviceService.updateDeviceStatus(id, status);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody IotDeviceSaveReqVO updateReqVO) {
        deviceService.updateDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
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
    public void exportDeviceExcel(@Valid IotDevicePageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDeviceDO> list = deviceService.getDevicePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "IoT 设备.xls", "数据", IotDeviceRespVO.class,
                BeanUtils.toBean(list, IotDeviceRespVO.class));
    }

}
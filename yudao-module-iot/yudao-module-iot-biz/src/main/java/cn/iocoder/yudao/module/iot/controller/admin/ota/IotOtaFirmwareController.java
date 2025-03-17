package cn.iocoder.yudao.module.iot.controller.admin.ota;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaFirmwareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT OTA 固件")
@RestController
@RequestMapping("/iot/ota-firmware")
@Validated
public class IotOtaFirmwareController {

    @Resource
    private IotOtaFirmwareService otaFirmwareService;

    @PostMapping("/create")
    @Operation(summary = "创建 OTA 固件")
    @PreAuthorize("@ss.hasPermission('iot:ota-firmware:create')")
    public CommonResult<Long> createOtaFirmware(@Valid @RequestBody IotOtaFirmwareCreateReqVO createReqVO) {
        return success(otaFirmwareService.createOtaFirmware(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 OTA 固件")
    @PreAuthorize("@ss.hasPermission('iot:ota-firmware:update')")
    public CommonResult<Boolean> updateOtaFirmware(@Valid @RequestBody IotOtaFirmwareUpdateReqVO updateReqVO) {
        otaFirmwareService.updateOtaFirmware(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OTA 固件")
    @PreAuthorize("@ss.hasPermission('iot:ota-firmware:query')")
    public CommonResult<IotOtaFirmwareRespVO> getOtaFirmware(@RequestParam("id") Long id) {
        IotOtaFirmwareDO otaFirmware = otaFirmwareService.getOtaFirmware(id);
        return success(BeanUtils.toBean(otaFirmware, IotOtaFirmwareRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OTA 固件分页")
    @PreAuthorize("@ss.hasPermission('iot:ota-firmware:query')")
    public CommonResult<PageResult<IotOtaFirmwareRespVO>> getOtaFirmwarePage(@Valid IotOtaFirmwarePageReqVO pageReqVO) {
        PageResult<IotOtaFirmwareDO> pageResult = otaFirmwareService.getOtaFirmwarePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOtaFirmwareRespVO.class));
    }

}

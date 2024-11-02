package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备数据")
@RestController
@RequestMapping("/iot/device/data")
@Validated
public class IotDeviceDataController {

    @Resource
    private IotDeviceDataService deviceDataService;

    @GetMapping("/latest-data")
    @Operation(summary = "获取设备属性最新数据")
    public CommonResult<List<IotDeviceDataRespVO>> getDevicePropertiesLatestData(@Valid IotDeviceDataReqVO deviceDataReqVO) {
        List<IotDeviceDataDO> list = deviceDataService.getDevicePropertiesLatestData(deviceDataReqVO);
        return success(BeanUtils.toBean(list, IotDeviceDataRespVO.class));
    }

}
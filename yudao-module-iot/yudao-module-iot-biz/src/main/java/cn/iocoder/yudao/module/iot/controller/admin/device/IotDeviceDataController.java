package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotTimeDataRespVO;
import cn.iocoder.yudao.module.iot.service.device.IotDevicePropertyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备数据")
@RestController
@RequestMapping("/iot/device/data")
@Validated
public class IotDeviceDataController {

    @Resource
    private IotDevicePropertyDataService deviceDataService;

    // TODO @浩浩：这里的 /latest-list，包括方法名。
    @GetMapping("/latest")
    @Operation(summary = "获取设备属性最新数据")
    public CommonResult<List<IotDeviceDataRespVO>> getLatestDeviceProperties(@Valid IotDeviceDataPageReqVO deviceDataReqVO) {
        List<IotDeviceDataDO> list = deviceDataService.getLatestDeviceProperties(deviceDataReqVO);
        return success(BeanUtils.toBean(list, IotDeviceDataRespVO.class));
    }

    // TODO @浩浩：这里的 /history-page 包括方法名。
    @GetMapping("/history")
    @Operation(summary = "获取设备属性历史数据")
    public CommonResult<PageResult<IotTimeDataRespVO>> getHistoryDeviceProperties(@Valid IotDeviceDataPageReqVO deviceDataReqVO) {
        PageResult<Map<String, Object>> list = deviceDataService.getHistoryDeviceProperties(deviceDataReqVO);
        return success(BeanUtils.toBean(list, IotTimeDataRespVO.class));
    }

}
package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceLogDataService;
import cn.iocoder.yudao.module.iot.service.device.IotDevicePropertyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private IotDeviceLogDataService iotDeviceLogDataService;

    @Resource // TODO @super：service 之间，不用空行；原因是，这样更简洁；空行，主要是为了“间隔”，提升可读性
    private IotDeviceLogDataService deviceLogDataService;

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

    // TODO:功能权限
    @PostMapping("/simulator")
    @Operation(summary = "模拟设备")
    public CommonResult<Boolean> simulatorDevice(@Valid @RequestBody IotDeviceDataSimulatorSaveReqVO simulatorReqVO) {
        //TODO:先使用 IotDeviceDataSimulatorSaveReqVO  另外content里数据类型的效验前端也没做，后端应该要要效验一下，这块后续看看怎么安排
        // TODO @super：应该 deviceDataService 里面有个 simulatorDevice，然后里面去 insert 日志！
        deviceDataService.simulatorSend(simulatorReqVO);
        return success(true);
    }

    // TODO:功能权限
    @GetMapping("/log/page")
    @Operation(summary = "获得设备日志分页")
    public CommonResult<PageResult<IotDeviceLogRespVO>> getDeviceLogPage(@Valid IotDeviceLogPageReqVO pageReqVO) {
        PageResult<IotDeviceLogDO> pageResult = deviceLogDataService.getDeviceLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceLogRespVO.class));
    }

}
package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import cn.iocoder.yudao.module.iot.enums.ApiConstants;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 设备数据 Upstream 上行 API
 *
 * 目的：设备 -> 插件 -> 服务端
 *
 * @author haohao
 */
public interface IotDeviceUpstreamApi {

    String PREFIX = ApiConstants.PREFIX + "/device/upstream";

    /**
     * 更新设备状态
     *
     * @param updateReqDTO 更新设备状态 DTO
     */
    @PutMapping(PREFIX + "/update-status")
    CommonResult<Boolean> updateDeviceStatus(@Valid @RequestBody IotDeviceStatusUpdateReqDTO updateReqDTO);

    /**
     * 上报设备属性数据
     *
     * @param reportReqDTO 上报设备属性数据 DTO
     */
    @PostMapping(PREFIX + "/report-property")
    CommonResult<Boolean> reportDeviceProperty(@Valid @RequestBody IotDevicePropertyReportReqDTO reportReqDTO);

    /**
     * 上报设备事件数据
     *
     * @param reportReqDTO 设备事件
     */
    @PostMapping(PREFIX + "/report-event")
    CommonResult<Boolean> reportDeviceEvent(@Valid @RequestBody IotDeviceEventReportReqDTO reportReqDTO);

}
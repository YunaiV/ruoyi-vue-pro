package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import cn.iocoder.yudao.module.iot.enums.ApiConstants;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// TODO 芋艿：名字可能看情况改下
/**
 * 设备数据 API
 *
 * @author haohao
 */
public interface DeviceDataApi {

    // TODO @芋艿：可能会调整
    String PREFIX = ApiConstants.PREFIX + "/device-data";

    /**
     * 更新设备状态
     *
     * @param updateReqDTO 更新请求
     */
    @PutMapping(PREFIX + "/update-status")
    @PermitAll  // TODO 芋艿：后续看看怎么优化下
    CommonResult<Boolean> updateDeviceStatus(@Valid @RequestBody IotDeviceStatusUpdateReqDTO updateReqDTO);

    /**
     * 上报设备事件数据
     *
     * @param reportReqDTO 设备事件
     */
    @PostMapping(PREFIX + "/report-event")
    @PermitAll // TODO 芋艿：后续看看怎么优化下
    CommonResult<Boolean> reportDeviceEventData(@Valid @RequestBody IotDeviceEventReportReqDTO reportReqDTO);

    /**
     * 上报设备属性数据
     *
     * @param reportReqDTO 设备数据
     */
    @PostMapping(PREFIX + "/report-property")
    @PermitAll // TODO 芋艿：后续看看怎么优化下
    CommonResult<Boolean> reportDevicePropertyData(@Valid @RequestBody IotDevicePropertyReportReqDTO reportReqDTO);


}
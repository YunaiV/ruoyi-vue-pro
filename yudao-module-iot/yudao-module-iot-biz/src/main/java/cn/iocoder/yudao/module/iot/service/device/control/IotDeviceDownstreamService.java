package cn.iocoder.yudao.module.iot.service.device.control;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceDownstreamReqVO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

import javax.validation.Valid;

/**
 * IoT 设备下行 Service 接口
 *
 * 目的：服务端 -> 插件 -> 设备
 *
 * @author 芋道源码
 */
public interface IotDeviceDownstreamService {

    /**
     * 设备下行，可用于设备模拟
     *
     * @param downstreamReqVO 设备下行请求 VO
     * @return 下发消息
     */
    IotDeviceMessage downstreamDevice(@Valid IotDeviceDownstreamReqVO downstreamReqVO);

}

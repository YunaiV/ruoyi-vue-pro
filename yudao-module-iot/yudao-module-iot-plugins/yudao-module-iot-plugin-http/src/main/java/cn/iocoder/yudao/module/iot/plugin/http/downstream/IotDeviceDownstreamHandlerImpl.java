package cn.iocoder.yudao.module.iot.plugin.http.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertyGetReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertySetReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDeviceServiceInvokeReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import org.springframework.stereotype.Component;

/**
 * HTTP 插件的 {@link IotDeviceDownstreamHandler} 实现类
 *
 * 但是：由于设备通过 HTTP 短链接接入，导致其实无法下行指导给 device 设备，所以基本都是直接返回失败！！！
 * 类似 MQTT、WebSocket、TCP 插件，是可以实现下行指令的。
 *
 * @author 芋道源码
 */
@Component // TODO @芋艿：后续统一处理
public class IotDeviceDownstreamHandlerImpl implements IotDeviceDownstreamHandler {

    @Override
    public CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO) {
        // TODO @芋艿：待实现
        System.out.println();
        return null;
    }

    @Override
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        // TODO @芋艿：待实现
        System.out.println();
        return null;
    }

    @Override
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO) {
        // TODO @芋艿：待实现
        System.out.println();
        return null;
    }

}

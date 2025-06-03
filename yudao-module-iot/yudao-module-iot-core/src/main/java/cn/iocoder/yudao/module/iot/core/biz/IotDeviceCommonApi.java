package cn.iocoder.yudao.module.iot.core.biz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;

/**
 * IoT 设备通用 API
 *
 * @author haohao
 */
public interface IotDeviceCommonApi {

    CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO);

}

package cn.iocoder.yudao.module.iot.core.biz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;

/**
 * IoT 设备通用 API
 *
 * @author haohao
 */
public interface IotDeviceCommonApi {

    /**
     * 设备认证
     *
     * @param authReqDTO 认证请求
     * @return 认证结果
     */
    CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO);

    /**
     * 获取设备信息
     *
     * @param infoReqDTO 设备信息请求
     * @return 设备信息
     */
    CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO infoReqDTO);

    /**
     * 设备动态注册（一型一密）
     *
     * @param reqDTO 动态注册请求
     * @return 注册结果（包含 DeviceSecret）
     */
    CommonResult<IotDeviceRegisterRespDTO> registerDevice(IotDeviceRegisterReqDTO reqDTO);

}

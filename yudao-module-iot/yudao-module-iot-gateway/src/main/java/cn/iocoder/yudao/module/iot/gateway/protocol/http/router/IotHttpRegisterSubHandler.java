package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotSubDeviceRegisterFullReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 网关 HTTP 协议的【子设备动态注册】处理器
 * <p>
 * 用于子设备的动态注册，需要网关认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/register-devices">阿里云 - 动态注册子设备</a>
 */
public class IotHttpRegisterSubHandler extends IotHttpAbstractHandler {

    /**
     * 路径：/auth/register/sub-device/:productKey/:deviceName
     * <p>
     * productKey 和 deviceName 是网关设备的标识
     */
    public static final String PATH = "/auth/register/sub-device/:productKey/:deviceName";

    private final IotDeviceCommonApi deviceApi;

    public IotHttpRegisterSubHandler() {
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    public CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析通用参数
        String productKey = context.pathParam("productKey");
        String deviceName = context.pathParam("deviceName");

        // 2. 解析子设备列表
        JsonObject body = context.body().asJsonObject();
        if (body == null) {
            throw invalidParamException("请求体不能为空");
        }
        if (body.getJsonArray("params") == null) {
            throw invalidParamException("params 不能为空");
        }
        List<cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO> subDevices = JsonUtils.parseArray(
                body.getJsonArray("params").toString(), cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO.class);

        // 3. 调用子设备动态注册
        IotSubDeviceRegisterFullReqDTO reqDTO = new IotSubDeviceRegisterFullReqDTO()
                .setGatewayProductKey(productKey).setGatewayDeviceName(deviceName).setSubDevices(subDevices);
        CommonResult<List<IotSubDeviceRegisterRespDTO>> result = deviceApi.registerSubDevices(reqDTO);
        result.checkError();

        // 4. 返回结果
        return success(result.getData());
    }

}

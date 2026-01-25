package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 网关 HTTP 协议的【设备动态注册】处理器
 * <p>
 * 用于直连设备/网关的一型一密动态注册，不需要认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 */
public class IotHttpRegisterHandler extends IotHttpAbstractHandler {

    public static final String PATH = "/auth/register/device";

    private final IotDeviceCommonApi deviceApi;

    public IotHttpRegisterHandler() {
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    public CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析参数
        // TODO @AI：参数不太对，看看我写的建议
        JsonObject body = context.body().asJsonObject();
        String productKey = body.getString("productKey");
        if (StrUtil.isEmpty(productKey)) {
            throw invalidParamException("productKey 不能为空");
        }
        String deviceName = body.getString("deviceName");
        if (StrUtil.isEmpty(deviceName)) {
            throw invalidParamException("deviceName 不能为空");
        }
        String random = body.getString("random");
        if (StrUtil.isEmpty(random)) {
            throw invalidParamException("random 不能为空");
        }
        String sign = body.getString("sign");
        if (StrUtil.isEmpty(sign)) {
            throw invalidParamException("sign 不能为空");
        }

        // 2. 调用动态注册
        IotDeviceRegisterReqDTO reqDTO = new IotDeviceRegisterReqDTO()
                .setProductKey(productKey)
                .setDeviceName(deviceName)
                .setRandom(random)
                .setSign(sign);
        CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(reqDTO);
        result.checkError();

        // 3. 返回结果
        return success(result.getData());
    }

}

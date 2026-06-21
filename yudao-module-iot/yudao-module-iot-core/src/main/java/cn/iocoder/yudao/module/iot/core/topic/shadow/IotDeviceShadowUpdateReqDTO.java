package cn.iocoder.yudao.module.iot.core.topic.shadow;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * IoT 设备影子上行 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#SHADOW_UPDATE} 标准封套的 params 参数
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceShadowUpdateReqDTO {

    /**
     * 内层方法：get / update
     */
    private String method;

    /**
     * 影子状态
     */
    private ShadowState state;

    /**
     * 设备已知版本号，可缺省；-1 表示清空影子
     */
    private Long version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShadowState {

        /**
         * 上报状态
         */
        private Map<String, Object> reported;

    }

}

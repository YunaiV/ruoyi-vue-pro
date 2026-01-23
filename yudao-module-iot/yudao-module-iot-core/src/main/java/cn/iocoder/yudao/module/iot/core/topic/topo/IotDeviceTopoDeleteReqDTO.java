package cn.iocoder.yudao.module.iot.core.topic.topo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

// TODO @AI：是个数组；https://help.aliyun.com/zh/iot/user-guide/manage-topological-relationships?spm=a2c4g.11186623.help-menu-30520.d_2_2_7_3_2.540c390beZSDOG&scm=20140722.H_89299._.OR_help-T_cn~zh-V_1#section-rb1-wzw-y2b

// todo @AI：删除完，需要有个 reply 响应；

/**
 * IoT 设备拓扑删除 Request DTO
 * <p>
 * 用于 thing.topo.delete 消息的 params 参数
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceTopoDeleteReqDTO {

    /**
     * 子设备 ProductKey
     */
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    /**
     * 子设备 DeviceName
     */
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

}

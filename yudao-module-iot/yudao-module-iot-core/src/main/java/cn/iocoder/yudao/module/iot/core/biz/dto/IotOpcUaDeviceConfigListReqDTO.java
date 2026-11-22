package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * IoT OPC UA 设备配置列表查询 Request DTO
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class IotOpcUaDeviceConfigListReqDTO {

    /**
     * 状态
     */
    private Integer status;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 设备 ID 集合
     */
    private Set<Long> deviceIds;

}

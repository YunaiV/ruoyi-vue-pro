package cn.iocoder.yudao.module.iot.core.biz.dto;

import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import lombok.Data;

/**
 * IoT OPC UA 点位配置 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class IotOpcUaPointRespDTO {

    /**
     * 点位编号
     */
    private Long id;
    /**
     * 属性标识符（查询时关联物模型，不在点位表冗余）
     */
    private String identifier;

    // ========== OPC UA 协议配置 ==========

    /**
     * NodeId，例如 nsu=urn:example;s=Temperature、ns=2;i=1001
     */
    private String nodeId;
    /**
     * 采集方式
     *
     * 枚举 {@link IotOpcUaCollectModeEnum}
     */
    private Integer collectMode;
    /**
     * 采集间隔，单位：毫秒（轮询周期，或订阅的发布、采样间隔）
     */
    private Integer collectInterval;
    /**
     * 是否可写：为 true 时才允许 thing.property.set 写入该点位的 Value 属性
     */
    private Boolean writable;

}

package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT OPC UA Server 节点 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class IotOpcUaServerNodeRespDTO {

    /**
     * 节点编号
     */
    private Long id;
    /**
     * 浏览名称：同一配置下唯一
     */
    private String browseName;

    // ========== 关联的设备属性（网关按它匹配属性上报消息，更新节点值） ==========

    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 属性标识符（查询时关联物模型，不在节点表冗余）
     */
    private String identifier;
    /**
     * 属性数据类型
     *
     * 枚举 IotDataSpecsDataTypeEnum（biz 模块）
     */
    private String dataType;

}

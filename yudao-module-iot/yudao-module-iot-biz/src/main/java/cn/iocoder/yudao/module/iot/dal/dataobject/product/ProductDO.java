package cn.iocoder.yudao.module.iot.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * iot 产品 DO
 *
 * @author 芋道源码
 */
@TableName("iot_product")
@KeySequence("iot_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 产品标识
     */
    private String identification;
    /**
     * 设备类型：device、gatway、gatway_sub
     */
    private String deviceType;
    /**
     * 厂商名称
     */
    private String manufacturerName;
    /**
     * 产品型号
     */
    private String model;
    /**
     * 数据格式:1. 标准数据格式（JSON）2. 透传/自定义，脚本解析
     */
    private Integer dataFormat;
    /**
     * 设备接入平台的协议类型，默认为MQTT
     */
    private String protocolType;
    /**
     * 产品描述
     */
    private String description;
    /**
     * 产品状态 (0: 启用, 1: 停用)
     */
    private Integer status;
    /**
     * 物模型定义
     */
    private String metadata;
    /**
     * 消息协议ID
     */
    private Long messageProtocol;
    /**
     * 消息协议名称
     */
    private String protocolName;

}
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
     * 产品名称
     */
    private String name;
    /**
     * 产品ID
     */
    @TableId
    private Long id;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 协议编号（脚本解析 id）
     */
    private Long protocolId;
    /**
     * 产品所属品类标识符
     */
    private Long categoryId;
    /**
     * 产品描述
     */
    private String description;
    /**
     * 数据校验级别, 0: 强校验, 1: 弱校验, 2: 免校验
     */
    private Integer validateType;
    /**
     * 产品状态, 0: DEVELOPMENT_STATUS, 1: RELEASE_STATUS
     */
    private Integer status;
    /**
     * 设备类型, 0: 直连设备, 1: 网关子设备, 2: 网关设备
     */
    private Integer deviceType;
    /**
     * 联网方式, 0: Wi-Fi, 1: Cellular, 2: Ethernet, 3: 其他
     */
    private Integer netType;
    /**
     * 接入网关协议, 0: modbus, 1: opc-ua, 2: customize, 3: ble, 4: zigbee
     */
    private Integer protocolType;
    /**
     * 数据格式, 0: 透传模式, 1: Alink JSON
     */
    private Integer dataFormat;

}
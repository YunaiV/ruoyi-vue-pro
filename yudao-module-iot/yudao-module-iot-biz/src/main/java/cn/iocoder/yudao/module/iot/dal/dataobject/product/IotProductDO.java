package cn.iocoder.yudao.module.iot.dal.dataobject.product;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 产品 DO
 *
 * @author ahh
 */
@TableName("iot_product")
@KeySequence("iot_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotProductDO extends TenantBaseDO {

    /**
     * 产品 ID
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
    private String productKey;
    /**
     * 产品密钥，用于一型一密动态注册
     */
    private String productSecret;
    /**
     * 是否开启动态注册
     */
    private Boolean registerEnabled;
    /**
     * 产品分类编号
     * <p>
     * 关联 {@link IotProductCategoryDO#getId()}
     */
    private Long categoryId;
    /**
     * 产品图标
     */
    private String icon;
    /**
     * 产品图片
     */
    private String picUrl;
    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品状态
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum}
     */
    private Integer status;
    /**
     * 设备类型
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum}
     */
    private Integer deviceType;
    /**
     * 联网方式
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotNetTypeEnum}
     */
    private Integer netType;
    /**
     * 数据格式（编解码器类型）
     * <p>
     * 字典 {@link cn.iocoder.yudao.module.iot.enums.DictTypeConstants#CODEC_TYPE}
     *
     * 目的：用于 gateway-server 解析消息格式
     */
    private String codecType;

}
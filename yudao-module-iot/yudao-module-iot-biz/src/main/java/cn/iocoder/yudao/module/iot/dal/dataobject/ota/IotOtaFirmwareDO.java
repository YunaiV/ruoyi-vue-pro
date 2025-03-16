package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT OTA 固件 DO
 *
 * @see <a href="阿里云 IoT - OTA 升级">https://help.aliyun.com/zh/iot/user-guide/ota-upgrade-overview</a>
 *
 * @author 芋道源码
 */
@TableName(value = "iot_ota_firmware", autoResultMap = true)
@KeySequence("iot_ota_firmware_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOtaFirmwareDO extends BaseDO {

    /**
     * 固件编号
     */
    @TableField
    private Long id;
    /**
     * 固件名称
     */
    private String name;
    /**
     * 固件版本
     */
    private String description;
    /**
     * 版本号
     */
    private String version;

    /**
     * 产品编号
     *
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getId()}
     */
    // TODO @li：帮我改成 Long 哈，写错了
    private String productId;
    /**
     * 产品标识
     *
     * 冗余 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getProductKey()}
     */
    private String productKey;

    /**
     * 签名方式
     *
     * 例如说：MD5、SHA256
     */
    private String signMethod;
    /**
     * 固件文件签名
     */
    private String fileSign;
    /**
     * 固件文件大小
     */
    private Long fileSize;
    /**
     * 固件文件 URL
     */
    private String fileUrl;

    /**
     * 自定义信息，建议使用 JSON 格式
     */
    private String information;

}
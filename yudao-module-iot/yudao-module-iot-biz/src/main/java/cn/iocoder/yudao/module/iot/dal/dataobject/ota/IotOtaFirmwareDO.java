package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @TableId
    private Long id;
    /**
     * 固件名称
     */
    private String name;
    /**
     * 固件描述
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
    private Long productId;

    /**
     * 固件文件 URL
     */
    private String fileUrl;
    /**
     * 固件文件大小
     */
    private Long fileSize;
    /**
     * 固件文件签名算法
     *
     * 枚举 {@link DigestAlgorithm}，目前只使用 MD5
     */
    private String fileDigestAlgorithm;
    /**
     * 固件文件签名结果
     */
    private String fileDigestValue;

}
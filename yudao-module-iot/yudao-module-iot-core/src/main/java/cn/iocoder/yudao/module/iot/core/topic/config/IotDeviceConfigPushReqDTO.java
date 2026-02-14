package cn.iocoder.yudao.module.iot.core.topic.config;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备配置推送 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#CONFIG_PUSH} 下行消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/remote-configuration-1">阿里云 - 远程配置</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceConfigPushReqDTO {

    /**
     * 配置编号
     */
    private String configId;

    /**
     * 配置文件大小（字节）
     */
    private Long configSize;

    /**
     * 签名方法
     */
    private String signMethod;

    /**
     * 签名
     */
    private String sign;

    /**
     * 配置文件下载地址
     */
    private String url;

    /**
     * 获取类型
     * <p>
     * file: 文件
     * content: 内容
     */
    private String getType;

}

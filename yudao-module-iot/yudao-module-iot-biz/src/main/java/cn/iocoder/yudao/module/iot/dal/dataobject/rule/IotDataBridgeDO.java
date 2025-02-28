package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.Map;

/**
 * IoT 数据桥梁 DO
 *
 * @author 芋道源码
 */
@TableName("iot_data_bridge")
@KeySequence("iot_data_bridge_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDataBridgeDO extends BaseDO {

    /**
     * 桥梁编号
     */
    @TableId
    private Long id;
    /**
     * 桥梁名称
     */
    private String name;
    /**
     * 桥梁描述
     */
    private String description;
    /**
     * 桥梁状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 桥梁方向
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgDirectionEnum}
     */
    private Integer direction;

    /**
     * 桥梁类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum}
     */
    private Integer type;

    /**
     * 桥梁配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Config config;

    /**
     * 文件客户端的配置
     * 不同实现的客户端，需要不同的配置，通过子类来定义
     *
     * @author 芋道源码
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    // @JsonTypeInfo 注解的作用，Jackson 多态
    // 1. 序列化到时数据库时，增加 @class 属性。
    // 2. 反序列化到内存对象时，通过 @class 属性，可以创建出正确的类型
    public interface Config {
    }

    /**
     * HTTP 配置
     */
    @Data
    public static class HttpConfig implements Config {

        /**
         * 请求 URL
         */
        private String url;
        /**
         * 请求方法
         */
        private String method;
        /**
         * 请求头
         */
        private Map<String, String> headers;
        /**
         * 请求参数
         */
        private Map<String, String> query;
        /**
         * 请求体
         */
        private String body;

    }

    /**
     * MQTT 配置
     */
    @Data
    public static class MqttConfig implements Config {

        /**
         * MQTT 服务器地址
         */
        private String url;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
        /**
         * 客户端编号
         */
        private String clientId;
        /**
         * 主题
         */
        private String topic;

    }

    /**
     * RocketMQ 配置
     */
    @Data
    public static class RocketMQConfig implements Config {

        /**
         * RocketMQ 名称服务器地址
         */
        private String nameServer;
        /**
         * 访问密钥
         */
        private String accessKey;
        /**
         * 秘密钥匙
         */
        private String secretKey;

        /**
         * 生产者组
         */
        private String group;
        /**
         * 主题
         */
        private String topic;
        /**
         * 标签
         */
        private String tags;

    }

    /**
     * Kafka 配置
     */
    @Data
    public static class KafkaMQConfig implements Config {

        /**
         * Kafka 服务器地址
         */
        private String bootstrapServers;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
        /**
         * 是否启用 SSL
         */
        private Boolean ssl;

        /**
         * 生产者组 ID
         */
        private String groupId;
        /**
         * 主题
         */
        private String topic;

    }

}

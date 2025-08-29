package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRedisDataStructureEnum;
import lombok.Data;

/**
 * IoT Redis 配置 {@link IotAbstractDataSinkConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataSinkRedisConfig extends IotAbstractDataSinkConfig {

    /**
     * Redis 服务器地址
     */
    private String host;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 密码
     */
    private String password;
    /**
     * 数据库索引
     */
    private Integer database;

    /**
     * Redis 数据结构类型
     * <p>
     * 枚举 {@link IotRedisDataStructureEnum}
     */
    @InEnum(IotRedisDataStructureEnum.class)
    private Integer dataStructure;

    /**
     * 主题/键名
     * <p>
     * 对于不同的数据结构：
     * - Stream: 流的键名
     * - Hash: Hash 的键名
     * - List: 列表的键名
     * - Set: 集合的键名
     * - ZSet: 有序集合的键名
     * - String: 字符串的键名
     */
    private String topic;

    /**
     * Hash 字段名（仅当 dataStructure 为 HASH 时使用）
     */
    private String hashField;

    /**
     * ZSet 分数字段（仅当 dataStructure 为 ZSET 时使用）
     * 指定消息中哪个字段作为分数，如果不指定则使用当前时间戳
     */
    private String scoreField;

}

package cn.iocoder.yudao.module.iot.gateway.serialize;

import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.serialize.binary.IotBinarySerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.json.IotJsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;

/**
 * IoT 序列化器管理器
 *
 * 负责根据枚举创建和管理序列化器实例
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMessageSerializerManager {

    private final Map<IotSerializeTypeEnum, IotMessageSerializer> serializerMap = new EnumMap<>(IotSerializeTypeEnum.class);

    public IotMessageSerializerManager() {
        // 遍历枚举，创建对应的序列化器
        for (IotSerializeTypeEnum type : IotSerializeTypeEnum.values()) {
            IotMessageSerializer serializer = createSerializer(type);
            serializerMap.put(type, serializer);
            log.info("[IotSerializerManager][序列化器 {} 创建成功]", type);
        }
    }

    /**
     * 根据类型创建序列化器
     *
     * @param type 序列化类型
     * @return 序列化器实例
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private IotMessageSerializer createSerializer(IotSerializeTypeEnum type) {
        switch (type) {
            case JSON:
                return new IotJsonSerializer();
            case BINARY:
                return new IotBinarySerializer();
            default:
                throw new IllegalArgumentException("未知的序列化类型：" + type);
        }
    }

    /**
     * 获取序列化器
     *
     * @param type 序列化类型
     * @return 序列化器实例
     */
    public IotMessageSerializer get(IotSerializeTypeEnum type) {
        return serializerMap.get(type);
    }

}

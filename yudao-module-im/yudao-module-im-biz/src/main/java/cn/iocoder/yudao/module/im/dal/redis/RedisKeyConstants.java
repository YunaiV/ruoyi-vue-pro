package cn.iocoder.yudao.module.im.dal.redis;


/**
 * IM Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    // TODO @AI：这个 key 可能要改下；
    /**
     * 群已读位置
     * KEY 格式：  im:group:read:{groupId}
     * VALUE 数据类型： Hash (field: userId, value: maxReadMessageId)
     */
    String GROUP_READ_POSITION = "im:group:read:%s";

}

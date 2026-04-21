package cn.iocoder.yudao.module.im.dal.redis;


/**
 * IM Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 群消息已读位置
     * KEY 格式：  im:group:message:read:{groupId}
     * VALUE 数据类型： Hash (field: userId, value: maxReadMessageId)
     */
    String GROUP_MESSAGE_READ = "im:group:message:read:%s";

}

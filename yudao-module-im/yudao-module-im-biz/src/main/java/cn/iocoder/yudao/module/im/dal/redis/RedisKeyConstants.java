package cn.iocoder.yudao.module.im.dal.redis;


/**
 * IM Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 收件箱序号生成器
     * KEY 格式：  im:inbox:sequence:{userId}
     * VALUE 数据类型： String
     */
    String INBOX_SEQUENCE = "im_inbox_sequence:%s";

    /**
     * 收件箱的分布式锁
     * KEY 格式：  im:inbox:lock:{userId}
     * VALUE 数据类型： String
     */
    String INBOX_LOCK = "im_inbox_lock:%s";

    /**
     * 群已读位置
     * KEY 格式：  im:group:read:{groupId}
     * VALUE 数据类型： Hash (field: userId, value: maxReadMessageId)
     */
    String GROUP_READ_POSITION = "im:group:read:%s";

}

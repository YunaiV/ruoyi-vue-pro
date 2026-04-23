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

    /**
     * 好友关系是否存在的缓存
     * <p>
     * KEY 格式：friend:{userId}_{friendUserId}
     * VALUE 数据类型：Boolean
     */
    String FRIEND = "friend";

    /**
     * 群信息缓存
     * <p>
     * KEY 格式：group:{groupId}
     * VALUE 数据类型：ImGroupDO
     */
    String GROUP = "group";

    /**
     * 群有效成员 userId 列表缓存（仅 ENABLE 状态）
     * <p>
     * KEY 格式：group_member_ids:{groupId}
     * VALUE 数据类型：List<Long>
     * <p>
     * 说明：只缓存轻量的 userId 列表，适合"群消息推送目标"这类只关心 userId 的场景。
     */
    String GROUP_MEMBER_IDS = "group_member_ids";

}

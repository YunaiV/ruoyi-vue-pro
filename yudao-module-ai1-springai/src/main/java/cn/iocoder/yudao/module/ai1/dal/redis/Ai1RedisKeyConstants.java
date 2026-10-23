package cn.iocoder.yudao.module.ai1.dal.redis;

/**
 * AI1 Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface Ai1RedisKeyConstants {

    /**
     * 会话生成任务队列
     * <p>
     * KEY 格式：ai1:session:tasks
     * VALUE 数据类型：Stream 生成任务，字段为 tenantId、messageId、agentId、sessionId、content
     * <p>
     * 所有租户共用一个队列，KEY 不带 tenantId；worker 从任务的 tenantId 字段还原租户上下文
     */
    String SESSION_TASK_STREAM = "ai1:session:tasks";

    /**
     * 会话生成任务队列的消费组
     */
    String SESSION_TASK_GROUP = "ai1-session-workers";

    /**
     * 会话生成任务执行锁，按助手消息编号隔离；使用 Redisson 看门狗续期
     * <p>
     * KEY 格式：ai1:session:task-lock:{messageId}
     */
    String SESSION_TASK_LOCK = "ai1:session:task-lock:%d";

    /**
     * 会话结果流，按助手消息编号隔离
     * <p>
     * KEY 格式：ai1:session:result:{messageId}
     * VALUE 数据类型：Stream 生成事件，字段为 type、data
     * <p>
     * messageId 全局唯一，KEY 不带 tenantId；续传前已按租户、用户校验消息归属
     */
    String SESSION_RESULT_STREAM = "ai1:session:result:%d";

    /**
     * 会话停止意图，按助手消息编号隔离；覆盖排队中、其他节点执行中的任务
     * <p>
     * KEY 格式：ai1:session:stop:{messageId}
     * VALUE 数据类型：String，过期时间与结果流一致
     */
    String SESSION_STOP = "ai1:session:stop:%d";

    /**
     * 会话停止通知，消息内容为助手消息编号，用于即时唤醒执行节点
     */
    String SESSION_STOP_TOPIC = "ai1:session:stop";

    /**
     * 长期记忆归档锁，按租户互斥；使用 Redisson 看门狗续期
     * <p>
     * KEY 格式：ai1:memory:archive-lock:{tenantId}
     */
    String MEMORY_ARCHIVE_LOCK = "ai1:memory:archive-lock:%s";

    /**
     * 长期记忆快照锁：消息落库事务与归档读取互斥，避免低编号消息晚提交后落在水位线之前
     * <p>
     * KEY 格式：ai1:memory:snapshot-lock:{agentId}:{userId}；Agent 编号全局唯一，KEY 不带 tenantId
     */
    String MEMORY_SNAPSHOT_LOCK = "ai1:memory:snapshot-lock:%d:%d";

}

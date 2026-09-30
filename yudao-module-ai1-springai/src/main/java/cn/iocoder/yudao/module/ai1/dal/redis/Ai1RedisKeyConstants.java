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
     * 会话结果流，按助手消息编号隔离
     * <p>
     * KEY 格式：ai1:session:result:{messageId}
     * VALUE 数据类型：Stream 生成事件，字段为 type、data
     * <p>
     * messageId 全局唯一，KEY 不带 tenantId；续传前已按租户、用户校验消息归属
     */
    String SESSION_RESULT_STREAM = "ai1:session:result:%d";

}

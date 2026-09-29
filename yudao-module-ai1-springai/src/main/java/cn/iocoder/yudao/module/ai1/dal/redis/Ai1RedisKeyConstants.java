package cn.iocoder.yudao.module.ai1.dal.redis;

/**
 * AI1 Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface Ai1RedisKeyConstants {

    /**
     * 对话生成任务队列
     * <p>
     * KEY 格式：ai1:chat:tasks
     * VALUE 数据类型：Stream 生成任务，字段为 tenantId、messageId、agentId、conversationId、content
     */
    String CHAT_TASK_STREAM = "ai1:chat:tasks";

    /**
     * 对话生成任务队列的消费组
     */
    String CHAT_TASK_GROUP = "ai1-chat-workers";

    /**
     * 对话结果流，按助手消息编号隔离
     * <p>
     * KEY 格式：ai1:chat:result:{messageId}
     * VALUE 数据类型：Stream 生成事件，字段为 type、data
     */
    String CHAT_RESULT_STREAM = "ai1:chat:result:%d";

}

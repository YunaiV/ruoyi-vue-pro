package cn.iocoder.yudao.module.deepay.dal.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 聊天消息 Repository。
 */
public interface AiChatMessageRepository extends MongoRepository<AiChatMessageDocument, String> {

    /** 按会话 ID 查询消息（按时间升序） */
    List<AiChatMessageDocument> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /** 按会话 ID 分页查询消息 */
    Page<AiChatMessageDocument> findBySessionIdOrderByCreatedAtAsc(String sessionId, Pageable pageable);

    /** 统计会话消息数 */
    long countBySessionId(String sessionId);

    /** 按会话 ID 删除所有消息 */
    void deleteBySessionId(String sessionId);

    /** 按客户 ID 删除所有消息（合规清除） */
    void deleteByTenantIdAndCustomerId(Long tenantId, Long customerId);

}

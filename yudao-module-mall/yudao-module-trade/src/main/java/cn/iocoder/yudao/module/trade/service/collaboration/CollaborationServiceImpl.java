package cn.iocoder.yudao.module.trade.service.collaboration;

import cn.iocoder.yudao.module.pay.service.blockchain.BlockchainAsyncService;
import cn.iocoder.yudao.module.pay.service.blockchain.dto.OrderProofDTO;
import cn.iocoder.yudao.module.trade.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabParticipantBO;
import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabSessionBO;
import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabTaskBO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 多方协作服务实现
 * <p>
 * 使用 StringRedisTemplate 持久化会话（7 天 TTL）。
 * 对应 Python CollaborationWorkflow.create_collaboration_session()
 *
 * @author deepay
 */
@Slf4j
@Service
public class CollaborationServiceImpl implements CollaborationService {

    private static final String SESSION_KEY_PREFIX = "trade_collab:session:";
    private static final long SESSION_TTL_DAYS = 7;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private BlockchainAsyncService blockchainAsyncService;
    @Resource
    private ObjectMapper objectMapper;

    @Value("${b2b.collaboration.base-url:https://b2b.deepay.io/collaboration}")
    private String collabBaseUrl;

    @Override
    public CollabSessionBO createSession(String orderId, List<CollabParticipantBO> participants) {
        String sessionId = "collab_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 为每位参与者生成访问令牌和邀请链接
        for (CollabParticipantBO p : participants) {
            String token = UUID.randomUUID().toString().replace("-", "");
            p.setAccessToken(token);
            p.setInviteLink(collabBaseUrl + "/" + sessionId + "?token=" + token + "&role=" + p.getRole());
            p.setAccepted(false);
        }

        // 初始化文档列表
        List<Map<String, Object>> docs = new ArrayList<>();
        docs.add(buildDoc("purchase_order", orderId));
        docs.add(buildDoc("contract", orderId));
        docs.add(buildDoc("specification", orderId));

        // 创建初始任务
        List<CollabTaskBO> tasks = buildInitialTasks(participants);

        // 会话设置
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("requireSignature", true);
        settings.put("auditTrail", true);
        settings.put("blockchainBackup", true);

        CollabSessionBO session = new CollabSessionBO();
        session.setId(sessionId);
        session.setOrderId(orderId);
        session.setParticipants(participants);
        session.setDocuments(docs);
        session.setTasks(tasks);
        session.setSettings(settings);
        session.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        session.setStatus("active");

        // 存入 Redis（7 天 TTL）
        try {
            String json = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(
                    SESSION_KEY_PREFIX + sessionId, json,
                    Duration.ofDays(SESSION_TTL_DAYS));
        } catch (JsonProcessingException e) {
            log.error("协作会话序列化失败 sessionId={}: {}", sessionId, e.getMessage());
        }

        // 区块链存证（异步）
        String taskId = submitBlockchainAttestation(sessionId, orderId);
        session.setBlockchainTaskId(taskId);

        log.info("协作会话已创建 sessionId={} orderId={} participants={}", sessionId, orderId, participants.size());
        return session;
    }

    @Override
    public CollabSessionBO getSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CollabSessionBO.class);
        } catch (JsonProcessingException e) {
            log.error("协作会话反序列化失败 sessionId={}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    // ==================== 私有方法 ====================

    private Map<String, Object> buildDoc(String type, String orderId) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("type", type);
        doc.put("orderId", orderId);
        doc.put("url", "/trade/collaboration/document/" + type + "/" + orderId);
        doc.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        doc.put("status", "draft");
        return doc;
    }

    private List<CollabTaskBO> buildInitialTasks(List<CollabParticipantBO> participants) {
        List<CollabTaskBO> tasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 采购方任务
        CollabTaskBO t1 = new CollabTaskBO();
        t1.setId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        t1.setTitle("确认采购需求");
        t1.setAssigneeRole("purchaser");
        t1.setDeadline(now.plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        t1.setStatus("pending");
        t1.setRequiresApproval(true);
        tasks.add(t1);

        // 财务方任务
        CollabTaskBO t2 = new CollabTaskBO();
        t2.setId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        t2.setTitle("审核付款条件");
        t2.setAssigneeRole("finance");
        t2.setDeadline(now.plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        t2.setStatus("pending");
        t2.setRequiresSignature(true);
        tasks.add(t2);

        // 物流方任务
        CollabTaskBO t3 = new CollabTaskBO();
        t3.setId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        t3.setTitle("确认物流方案");
        t3.setAssigneeRole("logistics");
        t3.setDeadline(now.plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        t3.setStatus("pending");
        t3.setAttachmentsRequired(true);
        tasks.add(t3);

        return tasks;
    }

    private String submitBlockchainAttestation(String sessionId, String orderId) {
        try {
            String taskId = "COLLAB_" + sessionId + "_" + System.currentTimeMillis();
            OrderProofDTO dto = new OrderProofDTO();
            dto.setOrderId("COLLAB_" + orderId);
            dto.setTransactionId(taskId);
            dto.setAmount(BigDecimal.ZERO);
            dto.setCurrency("CNY");
            dto.setPaymentTime(LocalDateTime.now().toString());
            dto.setProductInfo("collaboration_session:" + sessionId);
            blockchainAsyncService.asyncStoreOrderProof(dto);
            return taskId;
        } catch (Exception e) {
            log.warn("协作会话区块链存证失败 sessionId={}: {}", sessionId, e.getMessage());
            return null;
        }
    }

}

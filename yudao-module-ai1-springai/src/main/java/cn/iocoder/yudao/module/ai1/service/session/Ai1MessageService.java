package cn.iocoder.yudao.module.ai1.service.session;

import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1MessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1MessageDO;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

/**
 * AI1 对话消息 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1MessageService {

    /**
     * 获得对话的消息列表，按编号升序
     *
     * @param sessionId 对话编号
     * @return 消息列表
     */
    List<Ai1MessageDO> getMessageListBySessionId(Long sessionId);

    /**
     * 获得对话中指定消息之前最近的若干条消息，按编号倒序
     *
     * @param sessionId 对话编号
     * @param id             消息编号（不含）
     * @param limit          数量上限
     * @return 消息列表
     */
    List<Ai1MessageDO> getMessageListBySessionIdAndIdLessThan(Long sessionId, Long id, Integer limit);

    /**
     * 发送消息（SSE 流式）：校验对话归属 → 落库用户消息与助手占位 → 投递生成任务 → 打开 SSE 连接
     *
     * 校验、落库失败时不抛出异常，而是返回只包含 error 事件的 SSE 连接
     *
     * @param userId  用户编号
     * @param sendReqVO 发送信息
     * @return SSE 连接
     */
    SseEmitter sendMessageStream(Long userId, @Valid Ai1MessageSendReqVO sendReqVO);

    /**
     * 断线续传（SSE 流式）：校验消息 → 对话 → 用户归属后，从结果流 lastEventId 之后继续转发，不重新生成
     *
     * @param userId      用户编号
     * @param messageId   助手消息编号，即结果流标识
     * @param lastEventId 已收到的最后一个事件编号，可为空
     * @return SSE 连接
     */
    SseEmitter resumeMessageStream(Long userId, Long messageId, String lastEventId);

    /**
     * 更新消息（worker 回填助手消息）
     *
     * @param updateObj 更新对象
     */
    void updateMessage(Ai1MessageDO updateObj);

    /**
     * 删除对话下的全部消息
     *
     * @param sessionIds 对话编号集合
     */
    void deleteMessageListBySessionIds(Collection<Long> sessionIds);

}

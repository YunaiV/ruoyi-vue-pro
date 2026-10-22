package cn.iocoder.yudao.module.ai1.service.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessagePageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

/**
 * AI1 会话消息 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1SessionMessageService {

    /**
     * 获得会话的消息列表，按编号升序
     *
     * @param sessionId 会话编号
     * @return 消息列表
     */
    List<Ai1SessionMessageDO> getSessionMessageListBySessionId(Long sessionId);

    /**
     * 获得会话消息分页，按编号倒序取最新页，页内按时间正序
     *
     * @param pageReqVO 分页查询
     * @return 消息分页
     */
    PageResult<Ai1SessionMessageDO> getSessionMessagePage(Ai1SessionMessagePageReqVO pageReqVO);

    /**
     * 获得会话中指定消息之前最近的若干条消息，按编号倒序
     *
     * @param sessionId 会话编号
     * @param id             消息编号（不含）
     * @param limit          数量上限
     * @return 消息列表
     */
    List<Ai1SessionMessageDO> getSessionMessageListBySessionIdAndIdLessThan(Long sessionId, Long id, Integer limit);

    /**
     * 获得会话中生成中的助手消息编号列表
     *
     * @param sessionId 会话编号
     * @return 消息编号列表
     */
    List<Long> getGeneratingAssistantMessageIdList(Long sessionId);

    /**
     * 获得多个会话中，编号大于 id 的第一条指定状态的消息
     *
     * @param sessionIds 会话编号集合
     * @param status     状态
     * @param id         消息编号（不含）
     * @return 消息
     */
    Ai1SessionMessageDO getFirstSessionMessageBySessionIdsAndStatusAndIdGt(Collection<Long> sessionIds, Integer status, Long id);

    /**
     * 获得多个会话中，编号在 (minId, maxId) 之间的指定状态消息，按编号升序
     *
     * @param sessionIds 会话编号集合
     * @param status     状态
     * @param minId      最小编号（不含）
     * @param maxId      最大编号（不含），为空时不限
     * @param limit      数量上限
     * @return 消息列表
     */
    List<Ai1SessionMessageDO> getSessionMessageListBySessionIdsAndStatusAndIdBetween(Collection<Long> sessionIds, Integer status,
                                                                                    Long minId, Long maxId, Integer limit);

    /**
     * 发送消息（SSE 流式）：校验会话归属 → 落库用户消息与助手占位 → 投递生成任务 → 打开 SSE 连接
     *
     * 校验、落库失败时不抛出异常，而是返回只包含 error 事件的 SSE 连接
     *
     * @param userId  用户编号
     * @param sendReqVO 发送信息
     * @return SSE 连接
     */
    SseEmitter sendSessionMessageStream(Long userId, @Valid Ai1SessionMessageSendReqVO sendReqVO);

    /**
     * 断线续传（SSE 流式）：校验消息 → 会话 → 用户归属后，从结果流 lastEventId 之后继续转发，不重新生成
     *
     * @param userId      用户编号
     * @param messageId   助手消息编号，即结果流标识
     * @param lastEventId 已收到的最后一个事件编号，可为空
     * @return SSE 连接
     */
    SseEmitter resumeSessionMessageStream(Long userId, Long messageId, String lastEventId);

    /**
     * 停止【我的】会话生成：校验会话归属后，终止会话中生成中的助手消息，跨节点生效
     *
     * @param userId    用户编号
     * @param sessionId 会话编号
     */
    void stopSessionMessageMy(Long userId, Long sessionId);

    /**
     * 获得消息
     *
     * @param id 编号
     * @return 消息
     */
    Ai1SessionMessageDO getSessionMessage(Long id);

    /**
     * 更新消息（worker 回填助手消息）
     *
     * @param updateObj 更新对象
     */
    void updateSessionMessage(Ai1SessionMessageDO updateObj);

    /**
     * 删除会话下的全部消息
     *
     * @param sessionIds 会话编号集合
     */
    void deleteSessionMessageListBySessionIds(Collection<Long> sessionIds);

}

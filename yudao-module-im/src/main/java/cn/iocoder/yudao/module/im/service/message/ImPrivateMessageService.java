package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;

import java.util.List;

/**
 * IM 私聊消息 Service 接口
 *
 * @author 芋道源码
 */
public interface ImPrivateMessageService {

    /**
     * 发送私聊消息
     *
     * @param senderId 发送人编号
     * @param reqVO    发送请求
     * @return 消息
     */
    ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendReqVO reqVO);

    /**
     * 拉取私聊消息（增量）
     *
     * @param userId 当前用户编号
     * @param minId  最小消息 id（不含）
     * @param size   拉取数量
     * @return 消息列表
     */
    List<ImPrivateMessageDO> pullPrivateMessageList(Long userId, Long minId, Integer size);

    /**
     * 标记私聊消息已读
     * <p>
     * 语义：将「对方发给当前用户、id <= messageId 的未读消息」一次性翻转为已读，
     * 与群聊 readGroupMessages 对称，避免"select-then-update"两步式带来的竞态。
     *
     * @param userId     当前用户编号
     * @param receiverId 接收方用户编号（对方）
     * @param messageId  已读位置（含），通常是前端会话内最大消息 id
     */
    void readPrivateMessages(Long userId, Long receiverId, Long messageId);

    /**
     * 查询对方已读到我发的最大消息 id
     * <p>
     * 用于多端 / 离线场景下的已读位置补齐：客户端进入会话或断线重连后，
     * 调用此接口拿到对方的 maxReadId，再按 id <= maxReadId 翻转本地自发消息为已读，弥补离线期间错过的 RECEIPT 推送事件。
     *
     * @param userId 当前用户编号
     * @param peerId 对方用户编号
     * @return 对方已读到的最大消息 id；对方一条都没读过时返回 null
     */
    Long getMaxReadMessageId(Long userId, Long peerId);

    /**
     * 撤回私聊消息
     *
     * @param userId    当前用户编号
     * @param messageId 消息编号
     * @return 撤回后的消息
     */
    ImPrivateMessageDO recallPrivateMessage(Long userId, Long messageId);

    /**
     * 查询私聊历史消息（游标拉取）
     *
     * @param userId 当前用户编号
     * @param reqVO  拉取请求
     * @return 消息列表（按 id 倒序）
     */
    List<ImPrivateMessageDO> getPrivateMessageList(Long userId, ImPrivateMessageListReqVO reqVO);

    /**
     * 发送系统提示消息（TIP_TEXT）
     * <p>
     * 跳过好友校验、敏感词过滤，用于系统自动生成的提示消息。
     *
     * @param senderId   发送人编号
     * @param receiverId 接收人编号
     * @param content    提示文本
     */
    void sendTipPrivateMessage(Long senderId, Long receiverId, String content);

}

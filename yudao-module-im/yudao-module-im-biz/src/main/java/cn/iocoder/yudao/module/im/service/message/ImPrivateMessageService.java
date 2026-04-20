package cn.iocoder.yudao.module.im.service.message;

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
     *
     * @param userId     当前用户编号
     * @param receiverId 接收方用户编号（对方）
     */
    void readPrivateMessages(Long userId, Long receiverId);

    /**
     * 撤回私聊消息
     *
     * @param userId    当前用户编号
     * @param messageId 消息编号
     */
    void recallPrivateMessage(Long userId, Long messageId);

}

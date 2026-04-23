package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * IM 群聊消息 Service 接口
 *
 * @author 芋道源码
 */
public interface ImGroupMessageService {

    /**
     * 发送群聊消息
     *
     * @param senderId 发送人编号
     * @param reqVO    发送请求
     * @return 消息
     */
    ImGroupMessageDO sendGroupMessage(Long senderId, ImGroupMessageSendReqVO reqVO);

    /**
     * 拉取群聊消息（增量）
     *
     * @param userId 当前用户编号
     * @param minId  最小消息 id（不含）
     * @param size   拉取数量
     * @return 消息列表
     */
    List<ImGroupMessageDO> pullGroupMessageList(Long userId, Long minId, Integer size);

    /**
     * 标记群聊消息已读
     *
     * @param userId    当前用户编号
     * @param groupId   群编号
     * @param messageId 已读到的消息编号
     */
    void readGroupMessages(Long userId, Long groupId, Long messageId);

    /**
     * 撤回群聊消息
     *
     * @param userId    当前用户编号
     * @param messageId 消息编号
     * @return 撤回后的提示消息
     */
    ImGroupMessageDO recallGroupMessage(Long userId, Long messageId);

    /**
     * 获取群消息的已读用户列表
     *
     * @param userId    当前用户编号
     * @param groupId   群编号
     * @param messageId 消息编号
     * @return 已读用户编号列表
     */
    List<Long> getGroupReadUsers(Long userId, Long groupId, Long messageId);

    /**
     * 查询群聊历史消息（游标拉取）
     *
     * @param userId 当前用户编号
     * @param reqVO  拉取请求
     * @return 消息列表（按 id 倒序）
     */
    List<ImGroupMessageDO> getGroupMessageList(Long userId, ImGroupMessageListReqVO reqVO);

    /**
     * 清理用户在某群的已读位置缓存
     * <p>
     * 用于成员退群场景
     *
     * @param groupId 群编号
     * @param userId  用户编号
     */
    void deleteReadMaxMessageId(Long groupId, Long userId);

    /**
     * 批量清理用户在某群的已读位置缓存
     * <p>
     * 用于批量踢出场景
     *
     * @param groupId 群编号
     * @param userIds 用户编号集合
     */
    void deleteReadMaxMessageIds(Long groupId, Collection<Long> userIds);

    /**
     * 清理某群所有用户的已读位置缓存
     * <p>
     * 用于群解散场景
     *
     * @param groupId 群编号
     */
    void deleteReadMaxMessageIdMap(Long groupId);

    /**
     * 发送群聊系统提示消息（TIP_TEXT）
     * <p>
     * 插入一条 TIP_TEXT 消息到群聊记录，并推送给指定用户
     *
     * @param senderId    发送人编号（操作者）
     * @param groupId     群编号
     * @param memberUserIds 推送目标用户编号集合
     * @param content     提示内容
     */
    void sendTipGroupMessage(Long senderId, Long groupId, Set<Long> memberUserIds, String content);

}


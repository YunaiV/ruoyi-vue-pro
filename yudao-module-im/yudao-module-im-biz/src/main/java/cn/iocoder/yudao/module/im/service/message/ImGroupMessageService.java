package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;

import java.util.List;

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
    List<ImGroupMessageDO> pullGroupMessages(Long userId, Long minId, Integer size);

    /**
     * 标记群聊消息已读
     *
     * @param userId  当前用户编号
     * @param groupId 群编号
     */
    void readGroupMessages(Long userId, Long groupId);

    /**
     * 撤回群聊消息
     *
     * @param userId    当前用户编号
     * @param messageId 消息编号
     */
    void recallGroupMessage(Long userId, Long messageId);

    /**
     * 获取群消息的已读用户列表
     *
     * @param userId    当前用户编号
     * @param groupId   群编号
     * @param messageId 消息编号
     * @return 已读用户编号列表
     */
    List<Long> getGroupReadUsers(Long userId, Long groupId, Long messageId);

}

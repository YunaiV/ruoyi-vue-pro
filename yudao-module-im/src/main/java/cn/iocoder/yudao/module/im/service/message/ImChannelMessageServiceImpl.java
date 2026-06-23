package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImChannelMessageMapper;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
import cn.iocoder.yudao.module.im.service.conversation.ImConversationReadService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImChannelMessageNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImMessageReadNotification;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.MaterialMessage;
import javax.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.IM_CHANNEL_MESSAGE_NOT_EXISTS;

/**
 * IM 频道消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImChannelMessageServiceImpl implements ImChannelMessageService {

    @Resource
    private ImChannelMessageMapper channelMessageMapper;

    @Resource
    private ImChannelMaterialService channelMaterialService;
    @Resource
    private ImWebSocketService webSocketService;

    @Resource
    private ImConversationReadService conversationReadService;

    // ==================== 用户端 ====================

    @Override
    public List<ImChannelMessageDO> pullChannelMessageList(Long userId, Long minId, Integer size) {
        return channelMessageMapper.selectListByUserAndMinId(userId, minId, size);
    }

    @Override
    public Map<Long, Long> getChannelReadMaxMessageIdMap(Long userId, Collection<Long> channelIds) {
        return conversationReadService.getConversationReadMessageIdMap(
                userId, ImConversationTypeEnum.CHANNEL.getType(), channelIds);
    }

    @Override
    public void readChannelMessages(Long userId, Long channelId, Long messageId) {
        Assert.notNull(channelId, "频道编号不能为空");
        Assert.notNull(messageId, "已读消息编号不能为空");
        // 1.1 校验消息真实存在且属于该频道，避免未来 / 伪造 messageId 污染读位置
        ImChannelMessageDO message = channelMessageMapper.selectById(messageId);
        if (message == null || ObjUtil.notEqual(message.getChannelId(), channelId)) {
            return;
        }
        // 1.2 定向消息校验对当前用户可见（receiver_user_ids 为空表示全员可见）
        if (CollUtil.isNotEmpty(message.getReceiverUserIds()) && !message.getReceiverUserIds().contains(userId)) {
            return;
        }

        // 2. 更新频道已读位置；读位置未前进则不推
        boolean advanced = conversationReadService.updateConversationReadPosition(
                userId, ImConversationTypeEnum.CHANNEL.getType(), channelId, messageId);
        if (!advanced) {
            return;
        }

        // 3. 异步推 READ 事件给自己多端同步
        getSelf().readChannelMessageEvent(userId, channelId, messageId);
    }

    /**
     * 发送频道已读 READ 事件给自己其他终端；频道无「给发送方刷回执」概念，不广播
     */
    @Async
    public void readChannelMessageEvent(Long userId, Long channelId, Long readId) {
        webSocketService.sendNotificationAsync(userId, ImConversationTypeEnum.CHANNEL.getType(),
                ImContentTypeEnum.READ.getType(), ImMessageReadNotification.ofChannel(channelId, readId));
    }

    private ImChannelMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    // ==================== 管理后台 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(ImChannelMessageSendReqVO reqVO) {
        // 1. 校验素材存在
        ImChannelMaterialDO material = channelMaterialService.validateMaterialExists(reqVO.getMaterialId());

        // 2.1 组装 payload（不带富文本正文）；字段同名直接 BeanUtils 拷贝，materialId 单独 set 以兼容转发场景
        MaterialMessage payload = BeanUtils.toBean(material, MaterialMessage.class).setMaterialId(material.getId());
        String payloadJson = JsonUtils.toJsonString(payload);
        // 2.2 落库 1 行 message；reqVO 同名字段（materialId / receiverUserIds）自动拷贝，剩余字段补 set
        ImChannelMessageDO message = BeanUtils.toBean(reqVO, ImChannelMessageDO.class).setChannelId(material.getChannelId())
                .setType(ImContentTypeEnum.MATERIAL.getType()).setContent(payloadJson).setSendTime(LocalDateTime.now());
        channelMessageMapper.insert(message);

        // 3. 异步推 WebSocket：指定用户走点对点；全员（receiverUserIds 为空）走广播
        ImChannelMessageNotification dto = ImChannelMessageNotification.ofSend(message);
        if (CollUtil.isNotEmpty(reqVO.getReceiverUserIds())) {
            webSocketService.sendNotificationAsync(reqVO.getReceiverUserIds(), ImConversationTypeEnum.CHANNEL.getType(),
                    dto.getType(), dto);
        } else {
            webSocketService.broadcastNotificationAsync(ImConversationTypeEnum.CHANNEL.getType(), dto.getType(), dto);
        }
        return message.getId();
    }

    @Override
    public PageResult<ImChannelMessageDO> getMessagePage(ImChannelMessagePageReqVO reqVO) {
        return channelMessageMapper.selectPage(reqVO);
    }

    @Override
    public void deleteMessage(Long id) {
        if (channelMessageMapper.selectById(id) == null) {
            throw exception(IM_CHANNEL_MESSAGE_NOT_EXISTS);
        }
        channelMessageMapper.deleteById(id);
    }

}

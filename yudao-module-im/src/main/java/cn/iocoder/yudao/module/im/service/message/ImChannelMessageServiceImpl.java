package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImChannelMessageMapper;
import cn.iocoder.yudao.module.im.dal.redis.message.ImChannelMessageReadRedisDAO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImChannelMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.MaterialMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
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
    private ImChannelMessageReadRedisDAO channelMessageReadRedisDAO;

    // ==================== 用户端 ====================

    @Override
    public List<ImChannelMessageDO> getMessageListForPull(Long userId, Long minId, Integer size) {
        return channelMessageMapper.selectListByUserAndMinId(userId, minId, size);
    }

    @Override
    public Map<Long, Long> getChannelReadMaxMessageIdMap(Long userId, Collection<Long> channelIds) {
        Map<Long, Long> result = new HashMap<>(channelIds.size());
        for (Long channelId : channelIds) {
            Long max = channelMessageReadRedisDAO.getReadMaxMessageId(channelId, userId);
            if (max != null) {
                result.put(channelId, max);
            }
        }
        return result;
    }

    @Override
    public void readChannelMessages(Long userId, Long channelId, Long messageId) {
        Assert.notNull(channelId, "频道编号不能为空");
        Assert.notNull(messageId, "已读消息编号不能为空");
        // 1. 已读位置未前进，直接返回
        Long prevMaxMessageId = channelMessageReadRedisDAO.getReadMaxMessageId(channelId, userId);
        if (prevMaxMessageId != null && prevMaxMessageId >= messageId) {
            return;
        }

        // 2. 更新 Redis 频道已读位置
        channelMessageReadRedisDAO.updateReadMaxMessageId(channelId, userId, messageId);

        // 3. 异步推 READ 事件给自己多端同步
        getSelf().readChannelMessageEvent(userId, channelId, messageId);
    }

    /**
     * 发送频道已读 READ 事件给自己其他终端；频道无「给发送方刷回执」概念，不广播
     */
    @Async
    public void readChannelMessageEvent(Long userId, Long channelId, Long readId) {
        webSocketService.sendChannelMessageAsync(userId, ImChannelMessageDTO.ofRead(channelId, readId));
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
                .setType(ImMessageTypeEnum.MATERIAL.getType()).setContent(payloadJson).setSendTime(LocalDateTime.now());
        channelMessageMapper.insert(message);

        // 3. 异步推 WebSocket：指定用户走点对点；全员（receiverUserIds 为空）走广播
        ImChannelMessageDTO dto = ImChannelMessageDTO.ofSend(message);
        if (CollUtil.isNotEmpty(reqVO.getReceiverUserIds())) {
            webSocketService.sendChannelMessageAsync(reqVO.getReceiverUserIds(), dto);
        } else {
            webSocketService.broadcastChannelMessageAsync(dto);
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

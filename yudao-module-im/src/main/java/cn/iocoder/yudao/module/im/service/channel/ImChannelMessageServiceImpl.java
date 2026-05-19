package cn.iocoder.yudao.module.im.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.message.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.message.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.channel.ImChannelMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImChannelMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.MaterialMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

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

    // ==================== 用户端 ====================

    @Override
    public List<ImChannelMessageDO> getMessageListForPull(Long userId, Long minId, Integer size) {
        return channelMessageMapper.selectListByUserAndMinId(userId, minId, size);
    }

    @Override
    public boolean isUserReceivedMaterial(Long userId, Long materialId) {
        return channelMessageMapper.existsByUserAndMaterial(userId, materialId);
    }

    // ==================== 管理后台 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(ImChannelMessageSendReqVO reqVO) {
        // 1. 校验素材存在
        ImChannelMaterialDO material = channelMaterialService.validateMaterialExists(reqVO.getMaterialId());

        // 2. 组装 payload（不带富文本正文）
        // TODO @AI：链式调用；想通在一行
        MaterialMessage payload = new MaterialMessage()
                .setTitle(material.getTitle())
                .setCoverUrl(material.getCoverUrl())
                .setSummary(material.getSummary())
                .setUrl(material.getUrl());
        String payloadJson = JsonUtils.toJsonString(payload);

        // 3. 落库 1 行 message
        // TODO @AI：链式调用；想通在一行
        ImChannelMessageDO message = new ImChannelMessageDO()
                .setChannelId(material.getChannelId())
                .setMaterialId(material.getId())
                .setType(ImMessageTypeEnum.MATERIAL.getType())
                .setContent(payloadJson)
                .setReceiverUserIds(reqVO.getReceiverUserIds())
                .setSendTime(LocalDateTime.now());
        channelMessageMapper.insert(message);

        // 4. 异步推 WebSocket；全员场景（receiverUserIds 为空）暂不实时广播，依靠客户端上线后 pull 兜底
        // TODO @AI：全院也要发；
        if (CollUtil.isNotEmpty(reqVO.getReceiverUserIds())) {
            webSocketService.sendChannelMessageAsync(reqVO.getReceiverUserIds(),
                    ImChannelMessageDTO.ofSend(message));
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

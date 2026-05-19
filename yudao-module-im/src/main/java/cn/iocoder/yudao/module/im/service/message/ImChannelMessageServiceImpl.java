package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImChannelMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
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

    // ==================== 管理后台 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(ImChannelMessageSendReqVO reqVO) {
        // 1. 校验素材存在
        ImChannelMaterialDO material = channelMaterialService.validateMaterialExists(reqVO.getMaterialId());

        // 2.1 组装 payload（不带富文本正文）；字段同名直接 BeanUtils 拷贝
        String payloadJson = JsonUtils.toJsonString(BeanUtils.toBean(material, MaterialMessage.class));
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

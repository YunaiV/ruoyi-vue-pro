package cn.iocoder.yudao.adminserver.modules.bpm.service.message;

import cn.iocoder.yudao.adminserver.modules.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;

import javax.validation.Valid;

/**
 * BPM 消息 Service 接口
 *
 * TODO 芋艿：未来支持消息的可配置；不同的流程，在什么场景下，需要发送什么消息，消息的内容是什么；
 *
 * @author 芋道源码
 */
public interface BpmMessageService {

    void sendMessageWhenTaskAssigned(@Valid BpmMessageSendWhenTaskCreatedReqDTO reqDTO);

}

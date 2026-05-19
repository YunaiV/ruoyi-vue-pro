package cn.iocoder.yudao.module.im.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.message.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.message.ImChannelMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMessageDO;
import jakarta.validation.Valid;

import java.util.List;

// TODO @AI：这块消息，我想挪到 message 里；后续，可能整合成一个表存储的；
/**
 * IM 频道消息 Service 接口
 *
 * @author 芋道源码
 */
public interface ImChannelMessageService {

    // ==================== 用户端 ====================

    /**
     * 拉取当前用户应收的频道消息（离线增量）
     *
     * @param userId 当前用户编号
     * @param minId  游标；返回大于此值的消息
     * @param size   返回条数
     * @return 频道消息列表；按 id 升序
     */
    List<ImChannelMessageDO> getMessageListForPull(Long userId, Long minId, Integer size);


    // ==================== 管理后台 ====================

    /**
     * 立即推送频道消息
     * <p>
     * 流程：读 material → 组装 payload → INSERT 1 行 im_channel_message → WebSocket 扇出
     *
     * @param reqVO 推送请求
     * @return 消息编号
     */
    Long sendMessage(@Valid ImChannelMessageSendReqVO reqVO);

    /**
     * 分页查询消息
     *
     * @param reqVO 分页查询条件
     * @return 消息分页
     */
    PageResult<ImChannelMessageDO> getMessagePage(ImChannelMessagePageReqVO reqVO);

    /**
     * 删除消息
     *
     * @param id 消息编号
     */
    void deleteMessage(Long id);

}
